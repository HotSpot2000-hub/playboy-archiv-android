"""Synthetic shared-body-shape experiment. Not an Android feature or a photo measurement.

Uses only verified existing decoder assets. No media input, network or profile writes.
Poses and view rotations are KNOWN in this experiment; only 20 shared body coefficients
and separate camera translations are optimized. This does not solve real-photo pose,
segmentation, source selection, physical scale, or anatomical validation.
"""
from __future__ import annotations
import argparse
import hashlib
import json
import os
from pathlib import Path
import time
import numpy as np
from scipy.optimize import least_squares


def verified(path, manifest):
    data = path.read_bytes()
    entry = manifest[path.name]
    if len(data) != entry['bytes'] or hashlib.sha256(data).hexdigest() != entry['sha256']:
        raise ValueError('Asset hash/length mismatch: ' + path.name)


def vision(mesh):
    return np.asarray(mesh, dtype=np.float64) * np.array([.01, -.01, -.01])


def basis(session, pose, yaw):
    def decode(shape):
        return vision(session.run(['vertices'], {'identity': shape[None].astype(np.float32),
            'pose': pose[None].astype(np.float32), 'expression': np.zeros((1, 72), np.float32)})[0][0])
    zero = np.zeros(45, np.float32)
    base = decode(zero)
    directions = np.stack([decode(np.eye(45, dtype=np.float32)[i]) - base for i in range(20)], axis=2)
    angle = np.deg2rad(yaw)
    rotation = np.array([[np.cos(angle), 0, np.sin(angle)], [0, 1, 0], [-np.sin(angle), 0, np.cos(angle)]])
    # Validate actual decoder versus linear combination, not just the formula itself.
    check = np.zeros(45, np.float32)
    check[:20] = np.random.default_rng(71).uniform(-.8, .8, 20)
    error = np.max(np.abs(decode(check) - (base + np.einsum('vdk,k->vd', directions, check[:20]))))
    if error > 2e-6:
        raise ValueError('Decoder body-shape linearity check failed')
    return base @ rotation.T, np.einsum('ab,vbk->vak', rotation, directions), float(error)


def project(base, directions, shape, camera):
    p = base + np.einsum('vdk,k->vd', directions, shape) + camera
    if np.any(p[:, 2] <= .05):
        raise ValueError('Mesh behind camera')
    focal, center = 900., np.array([450., 550.])
    xy = focal * p[:, :2] / p[:, 2, None] + center
    jac = np.zeros((len(p), 2, 23))
    for axis in range(2):
        jac[:, axis, :20] = focal / p[:, 2, None] * (directions[:, axis] - p[:, axis, None] / p[:, 2, None] * directions[:, 2])
        jac[:, axis, 20 + axis] = focal / p[:, 2]
        jac[:, axis, 22] = -focal * p[:, axis] / p[:, 2]**2
    return xy, jac


def outline(base, directions, shape, camera, edges, rows):
    """Continuous outer silhouette intersection with fixed image rows, analytic Jacobian.

    Whole model including arms: poses/segmentation are intentionally not solved here.
    No point-to-vertex correspondence is assumed in the target silhouette.
    """
    xy, jac = project(base, directions, shape, camera)
    a, b = xy[edges[:, 0]], xy[edges[:, 1]]
    dy = b[:, 1] - a[:, 1]
    bounds, derivatives = [], []
    for row in rows:
        select = np.flatnonzero((np.minimum(a[:, 1], b[:, 1]) <= row) & (np.maximum(a[:, 1], b[:, 1]) > row) & (np.abs(dy) > 1e-8))
        if not len(select):
            raise ValueError('No silhouette in comparison row')
        t = (row - a[select, 1]) / dy[select]
        xx = a[select, 0] + t * (b[select, 0] - a[select, 0])
        for chosen in [np.argmin(xx), np.argmax(xx)]:
            e, v = select[chosen], t[chosen]
            ja, jb = jac[edges[e, 0]], jac[edges[e, 1]]
            slope = (b[e, 0] - a[e, 0]) / dy[e]
            derivatives.append((1-v)*ja[0] + v*jb[0] - slope*((1-v)*ja[1]+v*jb[1]))
            bounds.append(xx[chosen])
    return np.array(bounds), np.array(derivatives)


def validate_views(views):
    if len(views) < 3 or len({v['source_id'] for v in views}) != len(views):
        raise ValueError('Need independent source IDs, including held-out source')
    if len({v['group'] for v in views}) != 1:
        raise ValueError('Mixed body states forbidden')
    if sum(not v['held_out'] for v in views) < 2 or sum(v['held_out'] for v in views) < 1:
        raise ValueError('Need at least two fit sources and one held-out source')


def fit(views, edges):
    validate_views(views)
    train = [v for v in views if not v['held_out']]
    n = len(train)
    x0 = np.zeros(20+3*n)
    for i, view in enumerate(train):
        x0[20+3*i:23+3*i] = view['start_camera']

    def evaluate(q, use_shape):
        residual, matrices = [], []
        shape = q[:20] if use_shape else np.zeros(20)
        for i, view in enumerate(train):
            prediction, j = outline(view['base'], view['basis'], shape, q[20+3*i:23+3*i], edges, view['rows'])
            # Equal contribution per view regardless of row count. Fixed IDs prevent duplicate weighting.
            weight = 1 / np.sqrt(len(prediction))
            residual.extend((prediction-view['target'])*weight)
            block = np.zeros((len(prediction), len(q)))
            if use_shape:
                block[:, :20] = j[:, :20]*weight
            block[:, 20+3*i:23+3*i] = j[:, 20:]*weight
            matrices.append(block)
        if use_shape:
            residual.extend(q[:20]/12.)  # Weak fixed prior; no physical confidence claim.
            prior = np.zeros((20, len(q))); prior[:, :20] = np.eye(20)/12.
            matrices.append(prior)
        return np.array(residual), np.vstack(matrices)

    low = np.r_[np.full(20, -2.), np.tile([-.6, -.6, 2.5], n)]
    high = np.r_[np.full(20, 2.), np.tile([.6, .6, 5.5], n)]
    # Fair baseline: optimize each camera with body shape held at zero first.
    def cameras(c):
        q = x0.copy(); q[20:] = c
        r, j = evaluate(q, False)
        return r, j[:, 20:]
    baseline = least_squares(lambda c: cameras(c)[0], x0[20:], jac=lambda c: cameras(c)[1],
        bounds=(low[20:], high[20:]), loss='soft_l1', f_scale=.6, max_nfev=60)
    x0[20:] = baseline.x
    result = least_squares(lambda q: evaluate(q, True)[0], x0, jac=lambda q: evaluate(q, True)[1],
        bounds=(low, high), loss='soft_l1', f_scale=.6, max_nfev=100, ftol=1e-7, xtol=1e-7)
    scores = []
    train_indices = {id(view): i for i, view in enumerate(train)}
    for view in views:
        camera_before = view['camera'] if view['held_out'] else x0[20+3*train_indices[id(view)]:23+3*train_indices[id(view)]]
        camera_after = view['camera'] if view['held_out'] else result.x[20+3*train_indices[id(view)]:23+3*train_indices[id(view)]]
        before, _ = outline(view['base'], view['basis'], np.zeros(20), camera_before, edges, view['rows'])
        after, _ = outline(view['base'], view['basis'], result.x[:20], camera_after, edges, view['rows'])
        rms = lambda a: float(np.sqrt(np.mean((a-view['target'])**2)))
        scores.append({'id': view['source_id'], 'held_out': view['held_out'], 'before_px': rms(before), 'after_px': rms(after)})
    accepted = bool(result.success and all(s['after_px'] < s['before_px']*.98 and s['before_px']-s['after_px'] > .05 for s in scores))
    return result.x[:20], {'accepted': accepted, 'scores': scores, 'iterations': int(result.nfev), 'solver_success': bool(result.success)}


def run(args):
    started = time.monotonic()
    manifest = json.loads(args.manifest.read_text())
    for path in [args.decoder, args.faces]:
        verified(path, manifest)
    os.environ['ORT_DISABLE_TELEMETRY'] = '1'
    import onnxruntime as ort
    ort.disable_telemetry_events()
    options = ort.SessionOptions(); options.intra_op_num_threads=2; options.inter_op_num_threads=1
    options.add_session_config_entry('mlas.enable_gemm_fastmath_arm64_bfloat16', '0')
    options.add_session_config_entry('optimization.enable_gelu_approximation', '0')
    session = ort.InferenceSession(str(args.decoder), options, providers=['CPUExecutionProvider'])
    faces = np.fromfile(args.faces, dtype='<u2').reshape(-1, 3)
    edges = np.unique(np.sort(np.vstack([faces[:, [0,1]], faces[:, [1,2]], faces[:, [2,0]]]), axis=1), axis=0)
    truth = np.array([.8,-.6,.5,.35,-.4,.25,-.3,.15]+[0.]*12)
    views, parity = [], []
    rng = np.random.default_rng(17)
    for i, yaw in enumerate([0., -65., 70., 135.]):
        pose = np.zeros(204, np.float32)
        pose[6:70] = rng.normal(0, .025, 64)  # Known different poses, not optimized.
        base, directions, error = basis(session, pose, yaw); parity.append(error)
        camera = np.array([.04*(i-1), -.05+.015*i, 3.8+.15*i])
        true_xy, _ = project(base, directions, truth, camera)
        # Fixed comparison rows from the synthetic scene, not claimed anatomical levels.
        lo, hi = np.quantile(true_xy[:, 1], [.20, .64])
        rows = np.linspace(lo, hi, 28)
        target, j = outline(base, directions, truth, camera, edges, rows)
        # Probe analytic silhouette Jacobian against independent finite differences.
        if i == 0:
            eps = 1e-5
            for column in [0, 7, 19, 20, 21, 22]:
                plus_shape, minus_shape = truth.copy(), truth.copy()
                plus_camera, minus_camera = camera.copy(), camera.copy()
                if column < 20:
                    plus_shape[column] += eps; minus_shape[column] -= eps
                else:
                    plus_camera[column-20] += eps; minus_camera[column-20] -= eps
                finite = (outline(base,directions,plus_shape,plus_camera,edges,rows)[0]-outline(base,directions,minus_shape,minus_camera,edges,rows)[0])/(2*eps)
                np.testing.assert_allclose(j[:, column], finite, atol=1e-4, rtol=2e-4)
        target += rng.normal(0, .08, target.shape)
        views.append({'source_id':str(i), 'group':'synthetic-body-A', 'held_out':i==3,
            'base':base, 'basis':directions, 'rows':rows, 'target':target, 'camera':camera,
            'start_camera': camera+np.array([.025, -.02, .08])})
        print('Prepared synthetic view',i,flush=True)
    fitted, positive = fit(views, edges)
    # A contradictory independent held-out body must block adoption.
    wrong = dict(views[-1]); wrong['target'] = outline(wrong['base'],wrong['basis'],-truth,wrong['camera'],edges,wrong['rows'])[0]
    _, negative = fit(views[:-1]+[wrong], edges)
    exact_zero = [dict(view, target=outline(view['base'],view['basis'],np.zeros(20),view['camera'],edges,view['rows'])[0]) for view in views]
    _, unchanged = fit(exact_zero, edges)
    if unchanged['accepted']:
        raise AssertionError('An already matching body must not be presented as an improvement')
    guards = {}
    for name, sample in [('duplicate_source',views[:-1]+[dict(views[-1],source_id='0')]),
                         ('mixed_body_states',views[:-1]+[dict(views[-1],group='different')]),
                         ('missing_holdout',views[:-1])]:
        try:
            validate_views(sample)
            guards[name] = False
        except ValueError:
            guards[name] = True
    if not positive['accepted'] or negative['accepted'] or not all(guards.values()):
        raise AssertionError('Synthetic acceptance/rejection checks failed')
    zero_mesh = views[0]['base']; true_mesh=zero_mesh+np.einsum('vdk,k->vd',views[0]['basis'],truth)
    fitted_mesh=zero_mesh+np.einsum('vdk,k->vd',views[0]['basis'],fitted)
    span=np.ptp(true_mesh[:,1]); mesh_error=lambda m:float(100*np.sqrt(np.mean(np.sum((m-true_mesh)**2,axis=1)))/span)
    report={'scope':'Synthetic decoder experiment only; NOT real photos or Android feature',
        'pose_optimized':False,'camera_translation_optimized_per_fit_view':True,'shared_body_coefficients':20,
        'head_hand_and_size_channels':'fixed identically across all views',
        'linearity_max_metres':max(parity),'positive':positive,'contradictory_holdout':negative,'already_matching':unchanged,'guards':guards,
        'surface_rms_percent_vertical_span':{'before':mesh_error(zero_mesh),'after':mesh_error(fitted_mesh)},
        'coefficient_rmse':float(np.sqrt(np.mean((fitted-truth)**2))),'elapsed_host_seconds':time.monotonic()-started,
        'limits':['known synthetic poses and yaw; real-photo pose still unresolved','synthetic silhouettes contain no segmentation failures or occlusion',
          'no real photos, mobile benchmark, physical scale, measurement accuracy, source selector, or Android multi-photo workflow tested']}
    args.output.write_text(json.dumps(report,indent=2)+'\n')
    print(json.dumps(report,indent=2),flush=True)


if __name__ == '__main__':
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--decoder',type=Path,required=True);parser.add_argument('--faces',type=Path,required=True)
    parser.add_argument('--manifest',type=Path,required=True);parser.add_argument('--output',type=Path,required=True)
    run(parser.parse_args())
