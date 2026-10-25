package de.playboy.archiv;

import android.content.res.AssetFileDescriptor;
import android.os.Debug;
import android.os.SystemClock;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.DataInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

/** Local synthetic and single-photo tests. No network, archive writes or saved results. */
@CapacitorPlugin(name = "Body3D")
public class Body3DPlugin extends Plugin {
    private static final int VERTICES = 18439;
    private final AtomicBoolean busy = new AtomicBoolean(false);
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicBoolean photoJob = new AtomicBoolean(false);
    private final ExecutorService worker = Executors.newSingleThreadExecutor();

    @PluginMethod
    public void analyzePhoto(PluginCall call) {
        String encoded = call.getString("tensor");
        JSObject input = call.getObject("input");
        boolean refineRequested = Boolean.TRUE.equals(call.getBoolean("refinePose"));
        boolean formRequested = Boolean.TRUE.equals(call.getBoolean("compareForm"));
        boolean includeParameters = Boolean.TRUE.equals(call.getBoolean("includeParameters"));
        if (encoded == null || encoded.length() > 850000 || input == null) {
            call.reject("Die lokalen Bilddaten fehlen oder sind zu groß."); return;
        }
        if (!busy.compareAndSet(false, true)) {
            call.reject("Ein 3D-Test läuft bereits."); return;
        }
        cancelled.set(false);
        photoJob.set(true);
        try {
            worker.execute(() -> {
                long start = SystemClock.elapsedRealtime();
                try {
                    checkActive();
                    float[] cliffValues = photoCondition(input);
                    byte[] raw = android.util.Base64.decode(encoded, android.util.Base64.DEFAULT);
                    if (raw.length != 3 * 224 * 224 * 4) throw new IllegalStateException("Ungültige Bildtensorgröße.");
                    float[] pixels = new float[3 * 224 * 224];
                    ByteBuffer.wrap(raw).order(java.nio.ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(pixels);
                    for (float value : pixels) if (!Float.isFinite(value) || value < -3 || value > 3) {
                        throw new IllegalStateException("Ungültige normalisierte Bilddaten.");
                    }
                    JSONObject manifest = readModelManifest();
                    if (manifest.optInt("photo_revision", 0) != 1 || manifest.optInt("decoder_points_revision", 0) != 1 || manifest.optInt("diagnostic_revision", 0) != 3
                            || !"float32".equals(manifest.optString("image_compute", ""))) {
                        throw new IllegalStateException("Für den Fototest fehlen die neuen Modellinformationen in dieser APK.");
                    }
                    android.system.Os.setenv("ORT_DISABLE_TELEMETRY", "1", true);
                    OrtEnvironment env = OrtEnvironment.getEnvironment();
                    env.setTelemetry(false);
                    float[] shape, pose, camera;
                    JSONArray joints = new JSONArray();
                    JSONArray joints3d = new JSONArray();
                    ByteBuffer imageModel = mapAsset("instanthmr.onnx");
                    try (OrtSession.SessionOptions opts = options();
                         OrtSession session = env.createSession(imageModel, opts);
                         OnnxTensor image = OnnxTensor.createTensor(env, FloatBuffer.wrap(pixels), new long[]{1, 3, 224, 224});
                         OnnxTensor cliff = OnnxTensor.createTensor(env, FloatBuffer.wrap(cliffValues), new long[]{1, 3})) {
                        Map<String, OnnxTensor> feeds = new HashMap<>(); feeds.put("image", image); feeds.put("cliff_cond", cliff);
                        checkActive();
                        try (OrtSession.Result result = session.run(feeds)) {
                            shape = vector(result, "shape_params", 45); pose = vector(result, "mhr_params", 204);
                            camera = vector(result, "cam_trans", 3);
                            Object value = result.get("joints_2d").orElseThrow(() -> new IllegalStateException("Bildpunkte fehlen.")).getValue();
                            if (!(value instanceof float[][][])) throw new IllegalStateException("Ungültige Bildpunkte.");
                            float[][][] points = (float[][][]) value;
                            if (points.length != 1 || points[0].length != 70) throw new IllegalStateException("Ungültige Bildpunktzahl.");
                            for (float[] point : points[0]) {
                                if (point.length != 2 || !Float.isFinite(point[0]) || !Float.isFinite(point[1])) throw new IllegalStateException("Ungültiger Bildpunkt.");
                                joints.put(new JSONArray().put((double) point[0]).put((double) point[1]));
                            }

                        }
                    }
                    imageModel = null;
                    checkActive();
                    if (camera[2] <= .01f) throw new IllegalStateException("Die Kamera-Schätzung ist ungültig. Bitte den Personenrahmen prüfen.");
                    JSObject poseReport = null;
                    if (refineRequested) {
                        if (manifest.optInt("pose_refine_revision", 0) != 1) throw new IllegalStateException("Für den Posentest fehlen die neuen Modellinformationen.");
                        ByteBuffer poseModel = mapAsset("mhr_pose.onnx");
                        try (OrtSession.SessionOptions opts = options(); OrtSession session = env.createSession(poseModel, opts)) {
                            PoseFit fit = new PoseRefiner(env, session, shape, pose, camera, joints, input, this::checkActive).fit();
                            pose = fit.pose; camera = fit.camera; poseReport = fit.report;
                        }
                    }
                    ByteBuffer meshModel = mapAsset("mhr_decoder.onnx");
                    ByteBuffer vertices = ByteBuffer.allocate(VERTICES * 3 * 4).order(java.nio.ByteOrder.LITTLE_ENDIAN);
                    byte[] canonical = null;
                    try (OrtSession.SessionOptions opts = options(); OrtSession session = env.createSession(meshModel, opts)) {
                        checkActive();
                        float[][][] mesh = decode(env, session, shape, pose, joints3d);
                        if (mesh.length != 1 || mesh[0].length != VERTICES) throw new IllegalStateException("Ungültige Netzgröße.");
                        for (float[] vertex : mesh[0]) {
                            if (vertex.length != 3) throw new IllegalStateException("Ungültiger Netzpunkt.");
                            for (float v : vertex) if (!Float.isFinite(v)) throw new IllegalStateException("Ungültiger Netzpunkt.");
                            // Official MHR frame: cm, Y-up/Z-back -> metres, Y-down/Z-forward.
                            vertices.putFloat(vertex[0] / 100f); vertices.putFloat(-vertex[1] / 100f); vertices.putFloat(-vertex[2] / 100f);
                        }
                        if (formRequested) {
                            checkActive();
                            canonical = normalizedForm(decode(env, session, shape, canonicalPose(pose)));
                        }
                    }
                    checkActive();
                    byte[] faces = verifiedFaceBytes(manifest);
                    JSObject result = new JSObject();
                    result.put("vertices", android.util.Base64.encodeToString(vertices.array(), android.util.Base64.NO_WRAP));
                    result.put("faces", android.util.Base64.encodeToString(faces, android.util.Base64.NO_WRAP));
                    if (canonical != null) {
                        result.put("canonicalVertices", android.util.Base64.encodeToString(canonical, android.util.Base64.NO_WRAP));
                        result.put("canonicalRevision", 1);
                        result.put("canonicalFrame", "vision_centroid_vertical_span");
                        result.put("canonicalTopology", manifest.getJSONObject("mesh-faces.u16").getString("sha256"));
                    }
                    result.put("vertexCount", VERTICES); result.put("faceCount", faces.length / 6);
                    result.put("camera", new JSONArray().put((double) camera[0]).put((double) camera[1]).put((double) camera[2]));
                    result.put("joints2d", joints); result.put("joints3d", joints3d); result.put("pointDiagnosticRevision", 1); result.put("pointSource", "decoder_skeleton"); result.put("poseRefineRevision", 1); result.put("poseRefinement", poseReport); result.put("coordinateFrame", "vision_metres");
                    if (includeParameters) {
                        result.put("identityParameters", jsonVector(shape));
                        result.put("poseParameters", jsonVector(pose));
                        result.put("parameterRevision", 1);
                    }
                    result.put("elapsedSeconds", (SystemClock.elapsedRealtime() - start) / 1000.0);
                    checkActive(); call.resolve(result);
                } catch (OutOfMemoryError error) {
                    call.reject("Für den Einfoto-Test war zu wenig freier Arbeitsspeicher verfügbar.");
                } catch (Exception error) {
                    String message = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
                    call.reject("3D-Foto: " + message.substring(0, Math.min(550, message.length())));
                } finally { photoJob.set(false); busy.set(false); }
            });
        } catch (RuntimeException error) {
            photoJob.set(false); busy.set(false); call.reject("Der Einfoto-Test konnte nicht gestartet werden.");
        }
    }

    @PluginMethod
    public void cancelPhoto(PluginCall call) {
        if (photoJob.get()) cancelled.set(true);
        call.resolve(new JSObject());
    }

    /** Prepare one fixed-pose linear body basis. Used only by the future multi-photo worker. */
    @PluginMethod
    public void prepareSharedFormBasis(PluginCall call) {
        final float[] shape, pose;
        try {
            JSONArray identity = call.getArray("commonShape"), bodyPose = call.getArray("photoPose");
            if (identity == null || bodyPose == null) throw new IllegalArgumentException("Gemeinsame Form oder Fotopose fehlt.");
            shape = readVector(identity, 45); pose = readVector(bodyPose, 204);
            for (float value : shape) if (Math.abs(value) > 20) throw new IllegalArgumentException("Ungültiger Formparameter.");
            for (float value : pose) if (Math.abs(value) > 20) throw new IllegalArgumentException("Ungültiger Posenparameter.");
        } catch (Exception error) { call.reject("Ungültige Mehrfoto-Eingabe: " + error.getMessage()); return; }
        if (!busy.compareAndSet(false, true)) { call.reject("Ein 3D-Test läuft bereits."); return; }
        cancelled.set(false); photoJob.set(true);
        try {
            worker.execute(() -> {
                try {
                    checkActive();
                    JSONObject manifest = readModelManifest();
                    if (manifest.optInt("decoder_points_revision", 0) != 1) throw new IllegalStateException("Mehrfoto-Decoder fehlt.");
                    android.system.Os.setenv("ORT_DISABLE_TELEMETRY", "1", true);
                    OrtEnvironment env = OrtEnvironment.getEnvironment(); env.setTelemetry(false);
                    ByteBuffer model = mapAsset("mhr_decoder.onnx");
                    ByteBuffer baseBytes, basisBytes;
                    try (OrtSession.SessionOptions opts = options(); OrtSession session = env.createSession(model, opts)) {
                        ByteBuffer[] prepared = prepareBasis(env, session, shape, pose, this::checkActive);
                        baseBytes = prepared[0]; basisBytes = prepared[1];
                    }
                    checkActive();
                    JSONArray fixed = new JSONArray();
                    for (int i = 20; i < 45; i++) fixed.put((double) shape[i]);
                    for (int i = 130; i < 204; i++) fixed.put((double) pose[i]);
                    JSObject result = new JSObject();
                    result.put("basisRevision", 1); result.put("topologySha256", manifest.getJSONObject("mesh-faces.u16").getString("sha256")); result.put("vertexCount", VERTICES); result.put("bodyCoefficientCount", 20);
                    result.put("coordinateFrame", "vision_metres"); result.put("fixedParameters", fixed);
                    result.put("commonShape", jsonVector(shape));
                    result.put("base", android.util.Base64.encodeToString(baseBytes.array(), android.util.Base64.NO_WRAP));
                    result.put("basis", android.util.Base64.encodeToString(basisBytes.array(), android.util.Base64.NO_WRAP));
                    checkActive(); call.resolve(result);
                } catch (OutOfMemoryError error) { call.reject("Zu wenig freier Speicher für die Mehrfoto-Formbasis."); }
                catch (Exception error) { call.reject("Mehrfoto-Formbasis: " + error.getMessage()); }
                finally { photoJob.set(false); busy.set(false); }
            });
        } catch (RuntimeException error) { photoJob.set(false); busy.set(false); call.reject("Mehrfoto-Auftrag konnte nicht gestartet werden."); }
    }

    private static ByteBuffer[] prepareBasis(OrtEnvironment env, OrtSession session, float[] shape, float[] pose, Runnable check) throws Exception {
        check.run(); float[] base = flattenVision(decode(env, session, shape, pose));
        ByteBuffer baseBytes = ByteBuffer.allocate(base.length * 4).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        baseBytes.asFloatBuffer().put(base);
        ByteBuffer basisBytes = ByteBuffer.allocate(base.length * 20 * 4).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        for (int k = 0; k < 20; k++) {
            check.run(); float[] shifted = shape.clone(); shifted[k] += 1f;
            float[] actual = flattenVision(decode(env, session, shifted, pose));
            for (int i = 0; i < base.length; i++) basisBytes.putFloat(4 * (i * 20 + k), actual[i] - base[i]);
        }
        check.run(); return new ByteBuffer[]{baseBytes, basisBytes};
    }

    private static JSONArray jsonVector(float[] values) {
        JSONArray result = new JSONArray(); for (float value : values) result.put((double) value); return result;
    }

    private static float[] flattenVision(float[][][] mesh) {
        if (mesh.length != 1 || mesh[0].length != VERTICES) throw new IllegalStateException("Ungültige Mehrfoto-Netzgröße.");
        float[] values = new float[VERTICES * 3]; int i = 0;
        for (float[] p : mesh[0]) {
            if (p == null || p.length != 3) throw new IllegalStateException("Ungültiger Mehrfoto-Netzpunkt.");
            for (int k = 0; k < 3; k++) {
                if (!Float.isFinite(p[k])) throw new IllegalStateException("Ungültiger Mehrfoto-Netzpunkt.");
                values[i++] = p[k] / 100f * (k == 0 ? 1f : -1f);
            }
        }
        return values;
    }

    /** Experimental bounded pose fit. Shape and all size/scale channels stay fixed. */
    private static final class PoseFit {
        float[] pose, camera;
        JSObject report;
    }

    private static final class PoseRefiner {
        private static final int[] TRAIN = {4,3,68,66,6,23,53,59,50,55,65,7,24,1,52,58,12,36,41,2,51,45,31,5,35,37,25,42,21,43,47,9,11,33,34,62,14,46,57,64,17,32,54,22,13,67,44,15,26};
        private static final int[] TEST = {39,20,63,61,18,8,16,38,49,60,27,10,48,29,0,69,19,30,40,28,56};
        private final OrtEnvironment env;
        private final OrtSession session;
        private final float[] shape, basePose, baseCamera;
        private final Runnable check;
        private final double[][] target = new double[70][2];
        private final double focal, cx, cy;
        private final long start = System.nanoTime();
        private int evaluations;
        private boolean timeLimit;
        private double[][] basePoints;
        private static final class Evaluation { double[][] projected; }

        PoseRefiner(OrtEnvironment env, OrtSession session, float[] shape, float[] pose, float[] camera, JSONArray joints, JSONObject input, Runnable check) throws Exception {
            this.env=env;this.session=session;this.shape=shape.clone();this.basePose=pose.clone();this.baseCamera=camera.clone();this.check=check;
            int width=input.getInt("width"),height=input.getInt("height");cx=width/2.0;cy=height/2.0;focal=Math.hypot(width,height);
            JSONArray box=input.getJSONArray("box");double side=Math.ceil(Math.max(box.getDouble(2)-box.getDouble(0),box.getDouble(3)-box.getDouble(1))*1.2);
            double left=Math.floor((box.getDouble(0)+box.getDouble(2))/2-side/2),top=Math.floor((box.getDouble(1)+box.getDouble(3))/2-side/2);
            if(joints.length()!=70)throw new IllegalStateException("Posentest: Bildpunkte fehlen.");
            for(int i=0;i<70;i++){JSONArray p=joints.getJSONArray(i);target[i][0]=left+(p.getDouble(0)+1)*.5*side;target[i][1]=top+(p.getDouble(1)+1)*.5*side;}
            basePoints=points(basePose);
        }
        private double[][] points(float[] pose) throws Exception {
            check.run();evaluations++;
            try(OnnxTensor p=OnnxTensor.createTensor(env,FloatBuffer.wrap(pose),new long[]{1,204});
                OnnxTensor identity=OnnxTensor.createTensor(env,FloatBuffer.wrap(shape),new long[]{1,45});
                OnnxTensor expression=OnnxTensor.createTensor(env,FloatBuffer.wrap(new float[72]),new long[]{1,72})){
                Map<String,OnnxTensor> feeds=new HashMap<>();
                if(session.getInputNames().contains("pose"))feeds.put("pose",p);
                if(session.getInputNames().contains("identity"))feeds.put("identity",identity);
                if(session.getInputNames().contains("expression"))feeds.put("expression",expression);
                try(OrtSession.Result result=session.run(feeds)){
                    Object raw=result.get("decoder_joints").orElseThrow(()->new IllegalStateException("Posentest-Punkte fehlen.")).getValue();
                    if(!(raw instanceof float[][][]))throw new IllegalStateException("Posentest-Punktformat ungültig.");
                    float[][][] value=(float[][][])raw;photoPoints3d(value);double[][] out=new double[70][3];
                    for(int i=0;i<70;i++)for(int k=0;k<3;k++)out[i][k]=value[0][i][k];return out;
                }
            }
        }
        private Evaluation evaluate(double[] q,boolean withPose) throws Exception {
            check.run();double[][] p=basePoints;
            if(withPose){float[] pose=basePose.clone();boolean changed=false;for(int i=0;i<67;i++){pose[i+3]+=(float)q[i+3];changed|=q[i+3]!=0;}if(changed)p=points(pose);}
            Evaluation e=new Evaluation();e.projected=new double[70][3];
            for(int i=0;i<70;i++){double z=p[i][2]+q[2];if(!Double.isFinite(z)||z<=.02)return null;e.projected[i][0]=focal*(p[i][0]+q[0])/z+cx;e.projected[i][1]=focal*(p[i][1]+q[1])/z+cy;e.projected[i][2]=z;if(!Double.isFinite(e.projected[i][0])||!Double.isFinite(e.projected[i][1]))return null;}return e;
        }
        private double[] residual(Evaluation e,double[] q,boolean withPose){
            double[] r=new double[98+(withPose?67:0)];
            for(int i=0;i<49;i++)for(int k=0;k<2;k++)r[2*i+k]=(e.projected[TRAIN[i]][k]-target[TRAIN[i]][k])/5;
            if(withPose)for(int i=0;i<67;i++)r[98+i]=q[3+i]/.05;return r;
        }
        private static double squared(double[] r){double v=0;for(double x:r)v+=x*x;return v;}
        private static double[] solve(double[][] a,double[] b){
            int n=b.length;double[][] m=new double[n][n+1];for(int i=0;i<n;i++){System.arraycopy(a[i],0,m[i],0,n);m[i][n]=b[i];}
            for(int c=0;c<n;c++){int pivot=c;for(int r=c+1;r<n;r++)if(Math.abs(m[r][c])>Math.abs(m[pivot][c]))pivot=r;if(Math.abs(m[pivot][c])<1e-14)return null;double[] temp=m[c];m[c]=m[pivot];m[pivot]=temp;double d=m[c][c];for(int k=c;k<=n;k++)m[c][k]/=d;for(int r=0;r<n;r++)if(r!=c){double x=m[r][c];for(int k=c;k<=n;k++)m[r][k]-=x*m[c][k];}}
            double[] out=new double[n];for(int i=0;i<n;i++){out[i]=m[i][n];if(!Double.isFinite(out[i]))return null;}return out;
        }
        private double[] optimize(boolean withPose,double[] cameraStart) throws Exception {
            int n=withPose?70:3;double[] q=new double[n];System.arraycopy(cameraStart,0,q,0,3);double damping=.001;
            Evaluation current=evaluate(q,withPose);if(current==null)return q;
            for(int iteration=0;iteration<12;iteration++){
                check.run();if(System.nanoTime()-start>25_000_000_000L){timeLimit=true;break;}
                double[] r=residual(current,q,withPose);double cost=squared(r);double[][] jac=new double[r.length][n];
                for(int i=0;i<49;i++){int id=TRAIN[i];double z=current.projected[id][2];jac[2*i][0]=focal/z/5;jac[2*i+1][1]=focal/z/5;jac[2*i][2]=-(current.projected[id][0]-cx)/z/5;jac[2*i+1][2]=-(current.projected[id][1]-cy)/z/5;}
                if(withPose)for(int col=3;col<n;col++){
                    double[] perturbed=q.clone();perturbed[col]+=.001;Evaluation shifted=evaluate(perturbed,true);if(shifted==null)continue;
                    for(int i=0;i<49;i++)for(int k=0;k<2;k++)jac[2*i+k][col]=(shifted.projected[TRAIN[i]][k]-current.projected[TRAIN[i]][k])/5/.001;
                    jac[98+col-3][col]=1/.05;
                }
                double[][] normal=new double[n][n];double[] rhs=new double[n];
                for(int row=0;row<r.length;row++)for(int i=0;i<n;i++){rhs[i]-=jac[row][i]*r[row];for(int k=0;k<n;k++)normal[i][k]+=jac[row][i]*jac[row][k];}
                for(int i=0;i<n;i++)normal[i][i]+=damping*Math.max(normal[i][i],1e-9);double[] step=solve(normal,rhs);if(step==null)break;
                double[] candidate=q.clone();double movement=0;
                for(int i=0;i<n;i++){double low=i<2?baseCamera[i]-1:i==2?baseCamera[2]*.5:-.15,high=i<2?baseCamera[i]+1:i==2?baseCamera[2]*2:.15;candidate[i]=Math.max(low,Math.min(high,q[i]+step[i]));movement+=Math.pow(candidate[i]-q[i],2);}
                Evaluation proposal=evaluate(candidate,withPose);double next=proposal==null?Double.POSITIVE_INFINITY:squared(residual(proposal,candidate,withPose));
                if(next<cost){q=candidate;current=proposal;damping=Math.max(1e-10,damping/3);if(movement<1e-14||cost-next<1e-5*Math.max(1,cost))break;}else{damping=Math.min(1e12,damping*10);if(damping==1e12)break;}
            }
            return q;
        }
        private double rms(Evaluation e,int[] ids){if(e==null)return Double.POSITIVE_INFINITY;double sum=0;for(int i:ids)for(int k=0;k<2;k++)sum+=Math.pow(e.projected[i][k]-target[i][k],2);return Math.sqrt(sum/ids.length);}
        PoseFit fit() throws Exception {
            double[] startCamera={baseCamera[0],baseCamera[1],baseCamera[2]};
            if(evaluate(startCamera,false)==null){PoseFit fallback=new PoseFit();fallback.pose=basePose.clone();fallback.camera=baseCamera.clone();fallback.report=new JSObject();fallback.report.put("applied",false);fallback.report.put("reason","invalid_depth");return fallback;}
            double[] baseline=optimize(false,startCamera),candidate=optimize(true,baseline);
            Evaluation before=evaluate(baseline,false),after=evaluate(candidate,true);double beforeTest=rms(before,TEST),afterTest=rms(after,TEST),beforeTrain=rms(before,TRAIN),afterTrain=rms(after,TRAIN);
            boolean applied=Double.isFinite(afterTest)&&afterTest<beforeTest*.98&&afterTrain<beforeTrain*.98&&beforeTest-afterTest>.05&&beforeTrain-afterTrain>.05;
            PoseFit out=new PoseFit();out.pose=basePose.clone();out.camera=baseCamera.clone();double max=0;
            if(applied){for(int i=0;i<67;i++){out.pose[i+3]+=(float)candidate[i+3];max=Math.max(max,Math.abs(candidate[i+3]));}for(int i=0;i<3;i++)out.camera[i]=(float)candidate[i];}
            out.report=new JSObject();out.report.put("applied",applied);out.report.put("testRmsBefore",beforeTest);out.report.put("testRmsAfter",Double.isFinite(afterTest)?afterTest:beforeTest);out.report.put("trainRmsBefore",beforeTrain);out.report.put("trainRmsAfter",Double.isFinite(afterTrain)?afterTrain:beforeTrain);out.report.put("maxAngleDegrees",max*180/Math.PI);out.report.put("modelEvaluations",evaluations);out.report.put("timeLimit",timeLimit);return out;
        }
    }

    private static JSONArray photoPoints3d(float[][][] points) throws Exception {
        if (points.length != 1 || points[0] == null || points[0].length != 70) throw new IllegalStateException("Ungültige 3D-Körperpunktzahl.");
        JSONArray result = new JSONArray();
        for (float[] point : points[0]) {
            if (point == null || point.length != 3) throw new IllegalStateException("Ungültiger 3D-Körperpunkt.");
            JSONArray row = new JSONArray();
            for (float v : point) {
                if (!Float.isFinite(v)) throw new IllegalStateException("Ungültiger 3D-Körperpunkt.");
                row.put((double) v);
            }
            result.put(row);
        }
        return result;
    }

    private static float[] photoCondition(JSONObject input) throws Exception {
        int width = input.getInt("width"), height = input.getInt("height");
        if (width < 16 || height < 16 || width > 1600 || height > 1600) throw new IllegalStateException("Ungültige Fotogröße.");
        JSONArray box = input.getJSONArray("box");
        if (box.length() != 4) throw new IllegalStateException("Personenrahmen fehlt.");
        double x1 = box.getDouble(0), y1 = box.getDouble(1), x2 = box.getDouble(2), y2 = box.getDouble(3);
        if (!Double.isFinite(x1) || !Double.isFinite(y1) || !Double.isFinite(x2) || !Double.isFinite(y2)
                || x1 < 0 || y1 < 0 || x2 > width || y2 > height || x2 - x1 < 8 || y2 - y1 < 8
                || Math.max(x2 - x1, y2 - y1) < 64) throw new IllegalStateException("Bitte eine einzelne Person mit ausreichend großem Rahmen wählen.");
        return new float[]{(float) ((x1 + x2) / width - 1), (float) ((y1 + y2) / height - 1),
            (float) (Math.max(x2 - x1, y2 - y1) / Math.max(width, height))};
    }

    private byte[] verifiedFaceBytes(JSONObject manifest) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (InputStream input = getContext().getAssets().open("body3d/mesh-faces.u16")) {
            byte[] chunk = new byte[8192]; int count;
            while ((count = input.read(chunk)) != -1) {
                if (bytes.size() + count > 300000) throw new IllegalStateException("Ungültige Netzflächengröße.");
                bytes.write(chunk, 0, count);
            }
        }
        byte[] result = bytes.toByteArray();
        JSONObject expected = manifest.getJSONObject("mesh-faces.u16");
        byte[] digest = java.security.MessageDigest.getInstance("SHA-256").digest(result);
        StringBuilder hex = new StringBuilder();
        for (byte b : digest) hex.append(String.format(Locale.ROOT, "%02x", b & 255));
        if (result.length != 36874 * 3 * 2 || result.length != expected.getInt("bytes")
                || !hex.toString().equals(expected.getString("sha256"))) throw new IllegalStateException("Netzflächen stimmen nicht mit dem Körpermodell überein.");
        java.nio.ShortBuffer indices = ByteBuffer.wrap(result).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        while (indices.hasRemaining()) if ((indices.get() & 65535) >= VERTICES) throw new IllegalStateException("Ungültiger Netzflächenindex.");
        return result;
    }

    @PluginMethod
    public void runSyntheticTest(PluginCall call) {
        if (!busy.compareAndSet(false, true)) {
            call.reject("Ein 3D-Test läuft bereits.");
            return;
        }
        cancelled.set(false);
        try {
            worker.execute(() -> {
                long start = SystemClock.elapsedRealtime();
                String stage = "Start";
                try {
                    checkActive();
                    // Full opt-out must precede ORT class/environment initialization.
                    android.system.Os.setenv("ORT_DISABLE_TELEMETRY", "1", true);
                    OrtEnvironment env = OrtEnvironment.getEnvironment();
                    env.setTelemetry(false);
                    float[] shape;
                    float[] pose;
                    long sampledPssKb;
                    // Close the image session before opening the mesh decoder.
                    stage = "Bildmodell";
                    ByteBuffer imageModel = mapAsset("instanthmr.onnx");
                    try (OrtSession.SessionOptions options = options();
                         OrtSession session = env.createSession(imageModel, options);
                         OnnxTensor image = OnnxTensor.createTensor(env,
                             FloatBuffer.wrap(new float[3 * 224 * 224]), new long[]{1, 3, 224, 224});
                         OnnxTensor cliff = OnnxTensor.createTensor(env,
                             FloatBuffer.wrap(new float[]{0, 0, .8f}), new long[]{1, 3})) {
                        Map<String, OnnxTensor> inputs = new HashMap<>();
                        inputs.put("image", image);
                        inputs.put("cliff_cond", cliff);
                        checkActive();
                        try (OrtSession.Result result = session.run(inputs)) {
                            shape = vector(result, "shape_params", 45);
                            pose = vector(result, "mhr_params", 204);
                            sampledPssKb = Debug.getPss();
                        }
                    }
                    imageModel = null;
                    checkActive();
                    stage = "Referenzdaten";
                    float[][] fixedInputs = readFixedInputs();
                    float[] expectedMesh = readReference();
                    double shapeDelta = maxDifference(shape, fixedInputs[0]);
                    double poseDelta = maxDifference(pose, fixedInputs[1]);
                    Comparison chain;
                    Comparison fixed;
                    ByteBuffer meshModel = mapAsset("mhr_decoder.onnx");
                    try (OrtSession.SessionOptions options = options();
                         OrtSession session = env.createSession(meshModel, options)) {
                        stage = "Gesamtkette";
                        checkActive();
                        chain = compareMesh(decode(env, session, shape, pose), expectedMesh);
                        sampledPssKb = Math.max(sampledPssKb, Debug.getPss());
                        stage = "Decoder mit festen Eingaben";
                        checkActive();
                        fixed = compareMesh(decode(env, session, fixedInputs[0], fixedInputs[1]), expectedMesh);
                        sampledPssKb = Math.max(sampledPssKb, Debug.getPss());
                    }
                    // Keep the original tolerance and both comparisons. Never turn a mismatch
                    // into success merely because the isolated decoder comparison passes.
                    String diagnostic = "Prüfung 3: Decoder/feste Eingaben " + fixed.describe()
                        + "; Bildparameter Form-Delta " + number(shapeDelta)
                        + ", Pose-Delta " + number(poseDelta)
                        + "; Gesamtkette " + chain.describe()
                        + ". Rechenvergleich, keine Körpermaße.";
                    if (fixed.mismatches != 0 || chain.mismatches != 0) {
                        throw new IllegalStateException(diagnostic);
                    }
                    checkActive();
                    JSObject result = new JSObject();
                    result.put("passed", true);
                    result.put("vertices", VERTICES);
                    result.put("elapsedSeconds", (SystemClock.elapsedRealtime() - start) / 1000.0);
                    result.put("sampledProcessPssKb", sampledPssKb);
                    result.put("maxExportDifferenceNativeUnits", fixed.maxError);
                    result.put("chainMaxDifferenceNativeUnits", chain.maxError);
                    result.put("shapeParameterMaxDifference", shapeDelta);
                    result.put("poseParameterMaxDifference", poseDelta);
                    result.put("diagnosticRevision", 2);
                    result.put("scope", "synthetic_cpu_only");
                    call.resolve(result);
                } catch (OutOfMemoryError error) {
                    call.reject("Für den 3D-Test war zu wenig freier Arbeitsspeicher verfügbar.");
                } catch (Exception error) {
                    String message = error.getMessage();
                    if (message == null) message = error.getClass().getSimpleName();
                    if (!message.startsWith("Prüfung 3:")) message = "Prüfung 3 / " + stage + ": " + message;
                    call.reject(message.substring(0, Math.min(message.length(), 600)));
                } finally {
                    busy.set(false);
                }
            });
        } catch (RuntimeException error) {
            busy.set(false);
            call.reject("Der 3D-Test konnte nicht gestartet werden.");
        }
    }


    private JSONObject readModelManifest() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (InputStream input = getContext().getAssets().open("body3d/manifest.json")) {
            byte[] chunk = new byte[4096];
            int count;
            while ((count = input.read(chunk)) != -1) {
                if (bytes.size() + count > 32768) throw new IllegalStateException("Referenzmetadaten sind zu groß.");
                bytes.write(chunk, 0, count);
            }
        }
        JSONObject manifest = new JSONObject(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
        return manifest;
    }

    private float[][] readFixedInputs() throws Exception {
        JSONObject manifest = readModelManifest();
        if (manifest.optInt("diagnostic_revision", 0) != 3
                || !"float32".equals(manifest.optString("image_compute", ""))) {
            throw new IllegalStateException("Für Prüfung 3 fehlt das neue FP32-Bildmodell im APK-Build.");
        }
        JSONObject inputs = manifest.getJSONObject("synthetic_inputs");
        return new float[][]{readVector(inputs.getJSONArray("identity"), 45), readVector(inputs.getJSONArray("pose"), 204)};
    }

    private static float[] readVector(JSONArray values, int length) throws Exception {
        if (values.length() != length) throw new IllegalStateException("Ungültige Referenzparameterzahl.");
        float[] result = new float[length];
        for (int i = 0; i < length; i++) {
            result[i] = (float) values.getDouble(i);
            if (!Float.isFinite(result[i])) throw new IllegalStateException("Ungültiger Referenzparameter.");
        }
        return result;
    }

    private float[] readReference() throws Exception {
        float[] values = new float[VERTICES * 3];
        try (DataInputStream input = new DataInputStream(getContext().getAssets().open("body3d/synthetic-reference.f32"))) {
            for (int i = 0; i < values.length; i++) {
                values[i] = Float.intBitsToFloat(Integer.reverseBytes(input.readInt()));
                if (!Float.isFinite(values[i])) throw new IllegalStateException("Ungültiger Referenznetzpunkt.");
            }
            if (input.read() != -1) throw new IllegalStateException("Ungültige Referenzgröße.");
        }
        return values;
    }

    private static double maxDifference(float[] actual, float[] expected) {
        double maximum = 0;
        for (int i = 0; i < actual.length; i++) maximum = Math.max(maximum, Math.abs((double) actual[i] - expected[i]));
        return maximum;
    }

    // Same learned shape and size channels, fixed zero-rotation decoder pose.
    private static float[] canonicalPose(float[] pose) {
        if (pose.length != 204) throw new IllegalStateException("Ungültige Formpose.");
        float[] neutral = pose.clone();
        java.util.Arrays.fill(neutral, 0, 130, 0f);
        return neutral;
    }

    // Dimensionless whole mesh; vertical span is NOT the person's measured height.
    private static byte[] normalizedForm(float[][][] mesh) {
        if (mesh.length != 1 || mesh[0].length != VERTICES) throw new IllegalStateException("Ungültiges Vergleichsnetz.");
        double[] center = new double[3];
        double low = Double.POSITIVE_INFINITY, high = Double.NEGATIVE_INFINITY;
        for (float[] p : mesh[0]) {
            if (p == null || p.length != 3) throw new IllegalStateException("Ungültiger Vergleichspunkt.");
            for (int k = 0; k < 3; k++) {
                if (!Float.isFinite(p[k])) throw new IllegalStateException("Ungültiger Vergleichspunkt.");
                center[k] += p[k];
            }
            low = Math.min(low, p[1]); high = Math.max(high, p[1]);
        }
        double span = high - low;
        if (!Double.isFinite(span) || span <= 1e-6) throw new IllegalStateException("Vergleichsnetz ohne nutzbare Höhe.");
        for (int k = 0; k < 3; k++) center[k] /= VERTICES;
        ByteBuffer out = ByteBuffer.allocate(VERTICES * 3 * 4).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        for (float[] p : mesh[0]) for (int k = 0; k < 3; k++) {
            float value = (float) ((p[k] - center[k]) / span * (k == 0 ? 1 : -1));
            if (!Float.isFinite(value)) throw new IllegalStateException("Ungültige Vergleichsnormalisierung.");
            out.putFloat(value);
        }
        return out.array();
    }

    private static float[][][] decode(OrtEnvironment env, OrtSession session, float[] shape, float[] pose) throws Exception {
        return decode(env, session, shape, pose, null);
    }

    private static float[][][] decode(OrtEnvironment env, OrtSession session, float[] shape, float[] pose, JSONArray decoderPoints) throws Exception {
        try (OnnxTensor identity = OnnxTensor.createTensor(env, FloatBuffer.wrap(shape), new long[]{1, 45});
             OnnxTensor bodyPose = OnnxTensor.createTensor(env, FloatBuffer.wrap(pose), new long[]{1, 204});
             OnnxTensor expression = OnnxTensor.createTensor(env, FloatBuffer.wrap(new float[72]), new long[]{1, 72})) {
            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("identity", identity);
            inputs.put("pose", bodyPose);
            inputs.put("expression", expression);
            try (OrtSession.Result result = session.run(inputs)) {
                if (decoderPoints != null) {
                    Object points = result.get("decoder_joints").orElseThrow(() -> new IllegalStateException("Decoder-Körperpunkte fehlen.")).getValue();
                    if (!(points instanceof float[][][])) throw new IllegalStateException("Ungültige Decoder-Körperpunkte.");
                    JSONArray checked = photoPoints3d((float[][][]) points);
                    for (int i = 0; i < checked.length(); i++) decoderPoints.put(checked.getJSONArray(i));
                }
                Object value = result.get("vertices").orElseThrow(
                    () -> new IllegalStateException("Decoder-Ausgabe fehlt.")).getValue();
                if (!(value instanceof float[][][])) throw new IllegalStateException("Unerwarteter Decoder-Datentyp.");
                return (float[][][]) value;
            }
        }
    }

    private static Comparison compareMesh(float[][][] mesh, float[] expected) {
        if (mesh.length != 1 || mesh[0].length != VERTICES) throw new IllegalStateException("Unerwartete Netzgröße.");
        Comparison result = new Comparison();
        int index = 0;
        for (float[] vertex : mesh[0]) {
            if (vertex.length != 3) throw new IllegalStateException("Ungültiger Netzpunkt.");
            for (float actual : vertex) {
                if (!Float.isFinite(actual)) throw new IllegalStateException("Nichtendlicher Decoder-Netzpunkt.");
                double error = Math.abs((double) actual - expected[index]);
                result.maxError = Math.max(result.maxError, error);
                if (error > .001 + .0001 * Math.abs(expected[index])) {
                    if (result.mismatches == 0) {
                        result.firstIndex = index;
                        result.firstActual = actual;
                        result.firstExpected = expected[index];
                    }
                    result.mismatches++;
                }
                index++;
            }
        }
        return result;
    }

    private static String number(double value) {
        return String.format(Locale.ROOT, "%.6g", value);
    }

    private static final class Comparison {
        int mismatches;
        int firstIndex;
        float firstActual;
        float firstExpected;
        double maxError;

        String describe() {
            if (mismatches == 0) return "OK (max. " + number(maxError) + ")";
            return mismatches + "/55317 abweichend (max. " + number(maxError)
                + ", erster Punkt " + (firstIndex / 3 + 1) + "/Achse " + (firstIndex % 3 + 1)
                + ": " + number(firstActual) + " statt " + number(firstExpected) + ")";
        }
    }

    private OrtSession.SessionOptions options() throws Exception {
        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        try {
            options.addConfigEntry("mlas.enable_gemm_fastmath_arm64_bfloat16", "0");
            options.addConfigEntry("optimization.enable_gelu_approximation", "0");
            options.setIntraOpNumThreads(2);
            options.setInterOpNumThreads(1);
            options.setExecutionMode(OrtSession.SessionOptions.ExecutionMode.SEQUENTIAL);
            options.setMemoryPatternOptimization(false);
            options.setCPUArenaAllocator(false);
            return options;
        } catch (Exception error) {
            options.close();
            throw error;
        }
    }

    private ByteBuffer mapAsset(String name) throws Exception {
        checkActive();
        AssetFileDescriptor asset = getContext().getAssets().openFd("body3d/" + name);
        try (FileInputStream input = asset.createInputStream()) {
            long length = asset.getDeclaredLength();
            if (length < 1024 || length > 200L * 1024 * 1024) {
                throw new IllegalStateException("Ungültige 3D-Modelldatei.");
            }
            // APK assets are uncompressed; mmap avoids a second full Java-heap copy.
            return input.getChannel().map(FileChannel.MapMode.READ_ONLY, asset.getStartOffset(), length);
        } finally {
            asset.close();
        }
    }

    private static float[] vector(OrtSession.Result result, String name, int length) throws Exception {
        Object value = result.get(name).orElseThrow(
            () -> new IllegalStateException("Modellausgabe fehlt: " + name)).getValue();
        if (!(value instanceof float[][])) throw new IllegalStateException("Ungültige Modellausgabe.");
        float[][] values = (float[][]) value;
        if (values.length != 1 || values[0].length != length) {
            throw new IllegalStateException("Unerwartete Parameterzahl.");
        }
        for (float v : values[0]) {
            if (!Float.isFinite(v)) throw new IllegalStateException("Ungültiger Modellparameter.");
        }
        return values[0];
    }

    private void checkActive() {
        if (cancelled.get()) throw new IllegalStateException("Test beim Verlassen der App unterbrochen.");
    }

    @Override
    protected void handleOnPause() {
        cancelled.set(true);
    }

    @Override
    protected void handleOnDestroy() {
        cancelled.set(true);
        worker.shutdown();
    }
}
