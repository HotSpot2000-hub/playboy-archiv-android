/* Experimental computation core. No DOM, network, media or archive writes.
 * Poses are supplied/fixed; this module does NOT solve uncertain real-photo poses.
 * Clothing approval and connected torso rows are required by the photo adapter.
 */
(function(root){
'use strict';
const K=20;
function assert(ok,message){if(!ok)throw Error(message)}
const finite=a=>{if(!a||!Number.isInteger(a.length))return false;for(const x of a)if(!Number.isFinite(x))return false;return true};
function norm(a){return Math.hypot(...a)}
function point(g,name){const p=g?.[name];return p&&[p.x,p.y,p.confidence].every(Number.isFinite)&&p.confidence>=.6&&p.x>=0&&p.x<=1&&p.y>=0&&p.y<=1?p:null}
function descriptor(sample){
 const g=sample.geometry||{},names=['leftShoulder','rightShoulder','leftHip','rightHip','leftKnee','rightKnee','leftAnkle','rightAnkle'],p=names.map(n=>point(g,n));
 const head=['nose','leftEye','rightEye','leftEar','rightEar'].map(n=>point(g,n)).find(Boolean);
 if(p.some(x=>!x)||!head||sample.fullBody!==true||sample.safelyFramed!==true)return null;
 const hip={x:(p[2].x+p[3].x)/2,y:(p[2].y+p[3].y)/2},shoulder={x:(p[0].x+p[1].x)/2,y:(p[0].y+p[1].y)/2},torso=Math.hypot(shoulder.x-hip.x,shoulder.y-hip.y);
 if(torso<.07)return null;
 const framed=[...p,head],top=Math.min(...framed.map(x=>x.y)),bottom=Math.max(...framed.map(x=>x.y));
 if(bottom-top<.35||framed.some(x=>x.x<.01||x.x>.99||x.y<.01||x.y>.99))return null;
 const vector=[];for(const name of [...names,'leftElbow','rightElbow','leftWrist','rightWrist']){const q=point(g,name);vector.push(q?(q.x-hip.x)/torso:null,q?(q.y-hip.y)/torso:null)}
 return {vector,quality:p.reduce((s,x)=>s+x.confidence,0)/p.length,torso};
}
function distance(a,b){let sum=0,n=0;for(let i=0;i<a.length;i++)if(a[i]!==null&&b[i]!==null){sum+=Math.min(4,(a[i]-b[i])**2);n++}return n>=12?Math.sqrt(sum/n):0}
function selectSources(samples,{groupKey,maxSources=6,minSources=4}={}){
 assert(typeof groupKey==='string'&&groupKey.length>0,'Confirmed shooting/body-state key required');
 assert(Number.isInteger(maxSources)&&maxSources>=minSources&&maxSources<=8&&minSources>=4,'Invalid source limit');
 const seen=new Set(),ids=new Set(),candidates=[],excluded=[];
 for(const sample of samples){
  const id=sample.mediaId,key=sample.fingerprint||id;
  if(!id||sample.groupKey!==groupKey){excluded.push({id,reason:'context'});continue}
  if(!['free_contour','bikini'].includes(sample.clothing)){excluded.push({id,reason:sample.clothing==='skirt_dress'?'skirt_dress':'clothing_unconfirmed'});continue}
  if(ids.has(id)||seen.has(key)){excluded.push({id,reason:'duplicate'});continue}ids.add(id);seen.add(key);
  const d=descriptor(sample);if(!d){excluded.push({id,reason:'geometry'});continue}candidates.push({...sample,descriptor:d.vector,quality:d.quality});
 }
 candidates.sort((a,b)=>b.quality-a.quality||String(a.mediaId).localeCompare(String(b.mediaId)));
 const chosen=[];if(candidates.length)chosen.push(candidates.shift());
 while(candidates.length&&chosen.length<maxSources){
  let index=-1,best=-1;
  for(let i=0;i<candidates.length;i++){const novelty=Math.min(...chosen.map(x=>distance(x.descriptor,candidates[i].descriptor))),score=novelty*(.5+.5*candidates[i].quality);if(score>best){best=score;index=i}}
  const next=candidates[index],novelty=Math.min(...chosen.map(x=>distance(x.descriptor,next.descriptor)));
  if(novelty<.10){for(const c of candidates)excluded.push({id:c.mediaId,reason:'near_pose'});candidates.length=0;break}
  chosen.push(next);candidates.splice(index,1);
 }
 const sources=chosen.map((c,i)=>({mediaId:c.mediaId,fingerprint:c.fingerprint||null,role:i===chosen.length-1?'control':'fit',quality:c.quality}));
 return {groupKey,ready:sources.length>=minSources,sources,excluded,unselected:candidates.map(x=>x.mediaId),
  limitation:'2D pose diversity is a heuristic, not confirmed camera/view coverage or usable segmentation.'};
}
function validate(group){
 assert(group&&typeof group.groupKey==='string'&&group.groupKey,'Missing confirmed group');
 assert(Array.isArray(group.views)&&group.views.length>=3&&group.views.length<=8,'Need 3..8 sources');
 const ids=new Set(),fingerprints=new Set();let train=0,control=0,reference=null,commonShape=null;
 for(const v of group.views){
  assert(v.basisRevision===1&&v.topologySha256==='f2ac1f031a34dc4fd475b5d735904a65c2cd1b76a37c77ff2c44cb77597a5c36','Unknown body basis/topology');
  assert(['free_contour','bikini'].includes(v.clothing),'Clothing must be approved; skirts/dresses forbidden');
  if(v.clothing==='bikini')assert(v.clothingWindow?.length===2&&finite(v.clothingWindow)&&v.clothingWindow[0]<v.clothingWindow[1]&&v.rows.every(y=>y>=v.clothingWindow[0]&&y<=v.clothingWindow[1]),'Bikini observations must stay in approved uncovered window');
  assert(v.groupKey===group.groupKey,'Mixed body states');assert(typeof v.sourceId==='string'&&v.sourceId&&!ids.has(v.sourceId),'Duplicate or absent source ID');ids.add(v.sourceId);
  assert(typeof v.fingerprint==='string'&&v.fingerprint.length>0&&!fingerprints.has(v.fingerprint),'Independent source fingerprints required');fingerprints.add(v.fingerprint);
  assert(v.role==='fit'||v.role==='control','Unknown source role');if(v.role==='fit')train++;else control++;
  assert(v.commonShape?.length===45&&finite(v.commonShape),'Missing common identity baseline');
  if(commonShape)assert(commonShape.every((x,i)=>x===v.commonShape[i]),'Identity baseline differs across photos');else commonShape=Array.from(v.commonShape);
  assert(v.fixedParameters?.length===99&&finite(v.fixedParameters),'Need shared 25 head/hand and 74 size parameters');
  assert(commonShape.slice(20).every((x,i)=>x===v.fixedParameters[i]),'Inconsistent shared head/hand parameters');
  if(reference)assert(reference.every((x,i)=>x===v.fixedParameters[i]),'Size/head/hand parameters differ across photos');else reference=Array.from(v.fixedParameters);
  assert(v.base?.length===18439*3&&v.basis?.length===18439*3*K&&finite(v.base)&&finite(v.basis),'Invalid 20-component mesh basis');
  if(v.referenceBodyDelta)assert(v.referenceBodyDelta.length===K&&finite(v.referenceBodyDelta),'Invalid independent photo-shape baseline');
  assert(v.camera?.length===3&&finite(v.camera)&&v.camera[2]>.05,'Invalid initial camera');
  assert(v.rows?.length>=6&&v.rows.length<=128&&v.target?.length===2*v.rows.length&&finite(v.rows)&&finite(v.target),'Invalid contour rows');
  assert(v.rows.every((x,i)=>i===0||x>v.rows[i-1]),'Rows must increase');
  if(v.torsoWidths)assert(v.torsoWidths.length===v.rows.length&&finite(v.torsoWidths)&&v.torsoWidths.every(x=>x>0),'Invalid initial torso widths');
  if(v.centers)assert(v.centers.length===v.rows.length&&finite(v.centers)&&v.triangles?.length===36874*3&&v.triangles.every(x=>Number.isInteger(x)&&x>=0&&x<18439),'Invalid connected torso observations');
  assert(v.focal>0&&Number.isFinite(v.focal)&&v.center?.length===2&&finite(v.center),'Invalid image projection');
  assert(v.edges?.length>0&&v.edges.length%2===0&&v.edges.every(x=>Number.isInteger(x)&&x>=0&&x<18439),'Invalid mesh edges');
 }
 assert(train>=2&&control>=1,'Need fit sources and independent control');
}
function projection(view,shape,camera){
 const n=view.base.length/3,xyz=new Float64Array(n*3),xy=new Float64Array(n*2);
 for(let i=0;i<n;i++){
  for(let a=0;a<3;a++){const p=3*i+a;let v=view.base[p]+camera[a];for(let k=0;k<K;k++)v+=view.basis[p*K+k]*shape[k];xyz[p]=v}
  assert(xyz[3*i+2]>.05,'Mesh behind camera');
  for(let a=0;a<2;a++)xy[2*i+a]=view.focal*xyz[3*i+a]/xyz[3*i+2]+view.center[a];
 }
 const cache=new Map();
 function jac(i){if(cache.has(i))return cache.get(i);const out=new Float64Array(46),z=xyz[3*i+2];for(let a=0;a<2;a++){
  for(let k=0;k<K;k++)out[a*23+k]=view.focal/z*(view.basis[(3*i+a)*K+k]-xyz[3*i+a]/z*view.basis[(3*i+2)*K+k]);
  out[a*23+20+a]=view.focal/z;out[a*23+22]=-view.focal*xyz[3*i+a]/z**2;
 }cache.set(i,out);return out}
 return {xy,jac};
}
function outline(view,shape,camera){
 const {xy,jac}=projection(view,shape,camera),values=[],matrix=[];
 for(let ri=0;ri<view.rows.length;ri++){const row=view.rows[ri];
  let lo=Infinity,hi=-Infinity,left=null,right=null;
  if(!view.centers)for(let i=0;i<view.edges.length;i+=2){const a=view.edges[i],b=view.edges[i+1],ay=xy[2*a+1],by=xy[2*b+1],dy=by-ay;
   if(Math.abs(dy)<=1e-8||Math.min(ay,by)>row||Math.max(ay,by)<=row)continue;
   const t=(row-ay)/dy,x=xy[2*a]+t*(xy[2*b]-xy[2*a]);if(x<lo){lo=x;left=[a,b,t]}if(x>hi){hi=x;right=[a,b,t]}
  }
  if(view.centers){
   const intervals=[];
   for(let f=0;f<view.triangles.length;f+=3){
    const ia=view.triangles[f],ib=view.triangles[f+1],ic=view.triangles[f+2],ya=xy[2*ia+1],yb=xy[2*ib+1],yc=xy[2*ic+1];if(Math.min(ya,yb,yc)>row||Math.max(ya,yb,yc)<=row)continue;const vertices=[ia,ib,ic];
    const crossings=[];for(let e=0;e<3;e++){const a=vertices[e],b=vertices[(e+1)%3],ay=xy[2*a+1],by=xy[2*b+1],dy=by-ay;if(Math.abs(dy)<=1e-8||Math.min(ay,by)>row||Math.max(ay,by)<=row)continue;const t=(row-ay)/dy;crossings.push({x:xy[2*a]+t*(xy[2*b]-xy[2*a]),edge:[a,b,t]})}
    if(crossings.length===2){crossings.sort((a,b)=>a.x-b.x);intervals.push({lo:crossings[0],hi:crossings[1]})}
   }
   intervals.sort((a,b)=>a.lo.x-b.lo.x);const components=[];
   for(const interval of intervals){const last=components.at(-1);if(last&&interval.lo.x<=last.hi.x+1e-6){if(interval.hi.x>last.hi.x)last.hi=interval.hi}else components.push({...interval})}
   const component=components.find(c=>c.lo.x<=view.centers[ri]&&c.hi.x>=view.centers[ri]);
   assert(component,'No connected model torso at photo center');lo=component.lo.x;hi=component.hi.x;if(view.torsoWidths)assert(hi-lo>=view.torsoWidths[ri]*.5&&hi-lo<=view.torsoWidths[ri]*2,'Candidate contour left initial torso range');left=component.lo.edge;right=component.hi.edge;
  }
  assert(left&&right,'No silhouette in comparison row');
  for(const [value,edge] of [[lo,left],[hi,right]]){const [a,b,t]=edge,ja=jac(a),jb=jac(b),slope=(xy[2*b]-xy[2*a])/(xy[2*b+1]-xy[2*a+1]),d=new Float64Array(23);
   for(let k=0;k<23;k++)d[k]=(1-t)*ja[k]+t*jb[k]-slope*((1-t)*ja[23+k]+t*jb[23+k]);values.push(value);matrix.push(d);
  }
 }
 return {values,matrix};
}
function solve(a,b){
 const n=b.length,m=a.map((r,i)=>[...r,b[i]]);
 for(let c=0;c<n;c++){let p=c;for(let r=c+1;r<n;r++)if(Math.abs(m[r][c])>Math.abs(m[p][c]))p=r;if(Math.abs(m[p][c])<1e-14)return null;[m[p],m[c]]=[m[c],m[p]];const d=m[c][c];for(let k=c;k<=n;k++)m[c][k]/=d;for(let r=0;r<n;r++)if(r!==c){const x=m[r][c];for(let k=c;k<=n;k++)m[r][k]-=x*m[c][k]}}
 return m.map(r=>r[n]);
}
function fit(group,{cancel=()=>false,onProgress=()=>{},maxMs=30000}={}){
 validate(group);assert(Number.isFinite(maxMs)&&maxMs>0&&maxMs<=120000,'Invalid time budget');
 const now=()=>typeof performance==='object'?performance.now():Date.now();
 const started=now(),train=group.views.filter(v=>v.role==='fit'),n=K+3*train.length;
 function check(){assert(!cancel(),'Cancelled');assert(now()-started<maxMs,'Time budget exceeded')}
 const initial=new Float64Array(n);train.forEach((v,i)=>initial.set(v.camera,K+3*i));
 function evaluate(q,shapeEnabled){
  check();const residual=[],jac=[],shape=shapeEnabled?q.subarray(0,K):new Float64Array(K);
  train.forEach((v,i)=>{check();const out=outline(v,shape,q.subarray(K+3*i,K+3*i+3)),weight=1/Math.sqrt(out.values.length);
   out.values.forEach((x,r)=>{residual.push((x-v.target[r])*weight);const row=new Float64Array(n);if(shapeEnabled)for(let k=0;k<K;k++)row[k]=out.matrix[r][k]*weight;for(let k=0;k<3;k++)row[K+3*i+k]=out.matrix[r][K+k]*weight;jac.push(row)})
  });
  if(shapeEnabled)for(let k=0;k<K;k++){residual.push(q[k]/12);const row=new Float64Array(n);row[k]=1/12;jac.push(row)}
  const cost=residual.reduce((s,r)=>s+2*.6**2*(Math.sqrt(1+(r/.6)**2)-1),0);return {residual,jac,cost};
 }
 function optimize(q,shapeEnabled){
  const active=Array.from({length:n-(shapeEnabled?0:K)},(_,i)=>i+(shapeEnabled?0:K));let current=evaluate(q,shapeEnabled),damping=.001,converged=false;
  for(let iteration=0;iteration<50;iteration++){
   check();onProgress({phase:shapeEnabled?'shared_shape':'cameras',iteration,cost:current.cost});const count=active.length,a=Array.from({length:count},()=>new Float64Array(count)),b=new Float64Array(count);
   current.residual.forEach((r,row)=>{const w=1/Math.sqrt(1+(r/.6)**2);for(let i=0;i<count;i++){const di=current.jac[row][active[i]];b[i]-=w*di*r;for(let k=0;k<count;k++)a[i][k]+=w*di*current.jac[row][active[k]]}});
   if(norm(Array.from(b))<1e-8){converged=true;break}
   for(let i=0;i<count;i++)a[i][i]+=damping*Math.max(a[i][i],1e-9);const step=solve(a,b);if(!step)break;
   const next=q.slice();let movement=0;active.forEach((index,i)=>{const source=index<K?null:train[Math.floor((index-K)/3)].camera,axis=(index-K)%3,low=index<K?-2:axis===2?Math.max(.05,source[2]*.5):source[axis]-1,high=index<K?2:axis===2?source[2]*2:source[axis]+1;next[index]=Math.max(low,Math.min(high,q[index]+step[i]));movement+=(next[index]-q[index])**2});
   let proposal;try{proposal=evaluate(next,shapeEnabled)}catch(error){check();proposal=null}
   if(proposal&&proposal.cost<current.cost){const improvement=current.cost-proposal.cost;q=next;current=proposal;damping=Math.max(1e-10,damping/3);if(movement<1e-14||improvement<1e-8*Math.max(1,current.cost)){converged=true;break}}
   else{damping=Math.min(1e12,damping*10);if(damping===1e12)break}
  }return {q,converged};
 }
 const baseFit=optimize(initial,false),baseline=baseFit.q,sharedFit=optimize(baseline.slice(),true),candidate=sharedFit.q,scores=[];
 for(const v of group.views){check();const i=train.indexOf(v),beforeCamera=i<0?v.camera:baseline.subarray(K+3*i,K+3*i+3),afterCamera=i<0?v.camera:candidate.subarray(K+3*i,K+3*i+3);
  const rms=out=>Math.sqrt(out.values.reduce((s,x,r)=>s+(x-v.target[r])**2,0)/out.values.length);
  let before=null,after=null,individual=null,reason=null,beforeOutline=null,afterOutline=null;try{const b=outline(v,new Float64Array(K),beforeCamera),a=outline(v,candidate.subarray(0,K),afterCamera);before=rms(b);after=rms(a);beforeOutline=b.values;afterOutline=a.values}catch(error){check();reason=String(error.message||error)}
  if(v.referenceBodyDelta)try{individual=rms(outline(v,v.referenceBodyDelta,v.camera))}catch(error){check()}
  scores.push({sourceId:v.sourceId,role:v.role,before,after,individual,reason,beforeOutline,afterOutline});
 }
 const accepted=baseFit.converged&&sharedFit.converged&&scores.every(s=>Number.isFinite(s.before)&&Number.isFinite(s.after)&&s.after<s.before*.98&&s.before-s.after>.05&&(s.role!=='control'||!Number.isFinite(s.individual)||s.after<=s.individual+.05));
 const rejectionReason=accepted?null:!baseFit.converged||!sharedFit.converged?'not_converged':scores.some(s=>!Number.isFinite(s.before)||!Number.isFinite(s.after))?'invalid_contour':scores.some(s=>s.role==='control'&&Number.isFinite(s.individual)&&s.after>s.individual+.05)?'independent_photo_regression':'insufficient_improvement';
 if(!accepted)for(const score of scores){score.beforeOutline=null;score.afterOutline=null}
 return {accepted,rejectionReason,converged:{cameras:baseFit.converged,sharedShape:sharedFit.converged},bodyDelta:accepted?Array.from(candidate.subarray(0,K)):null,shape:accepted?Array.from(candidate.subarray(0,K),(x,k)=>x+group.views[0].commonShape[k]):null,identity:accepted?Array.from(group.views[0].commonShape,(x,k)=>k<K?x+candidate[k]:x):null,cameras:accepted?train.map((v,i)=>Array.from(candidate.subarray(K+3*i,K+3*i+3))):null,scores,elapsedMs:now()-started,
  limitation:'Fixed supplied poses; experimental silhouette fit, no validated body measurements or physical scale.'};
}
const api={revision:2,selectSources,descriptor,distance,validate,projection,outline,fit};
if(typeof module==='object'&&module.exports)module.exports=api;else root.Body3DMultiViewCore=api;
})(typeof globalThis==='object'?globalThis:this);
