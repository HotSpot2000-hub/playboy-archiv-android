/* Local shooting-only pilot. Explicit clothing approval, no archive/profile writes. */
(()=>{
'use strict';
let active=null;
const approvals=new Map(),$=(s,id)=>s.panel.querySelector(id),esc=value=>String(value??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const own=s=>active===s;
const imageMedia=m=>['image/jpeg','image/jpg','image/png','image/webp','image/avif'].includes((m.mimeType||'').toLowerCase())||/\.(jpe?g|png|webp|avif)$/i.test(m.fileName||'');
function photos(shoot){return shootingMedia(shoot).filter(imageMedia)}
function signature(shoot){return JSON.stringify([shootingModelIds(shoot),photos(shoot).map(m=>[m.id,m.fingerprint||'',m.size||0,m.fileName||''])])}
function check(s){if(!own(s)||s.cancelled)throw Error('Verarbeitung abgebrochen.');const sh=(state.archive||[]).find(x=>x.id===s.shootId&&x.type==='shoot');if(state.archive!==s.archive||!sh||signature(sh)!==s.signature)throw Error('Archivzuordnung oder Fotobestand geändert. Bitte den Mehrfoto-Test neu öffnen.')}
function status(s,text,error=false){if(!own(s))return;$(s,'#multiStatus').textContent=text;$(s,'#multiStatus').style.color=error?'#a12e38':'#60466e'}
function dispose(s){s.review=null;const photo=$(s,'#multiPhotoCanvas');if(photo)photo.width=photo.height=1;const review=$(s,'#multiPhotoReview');if(review)review.hidden=true;s.view?.dispose();s.view=null;const canvas=$(s,'#multiOrbit');if(canvas){canvas.width=canvas.height=1}}
function interrupt(s){if(s.cancelled)return;s.cancelled=true;s.cancelImage?.();s.abortWorker?.();window.Capacitor?.Plugins?.Body3D?.cancelPhoto?.().catch(()=>{});status(s,'Abgebrochen. Es wurden keine Archiv- oder Profilwerte verändert.')}
function close(){const s=active;if(!s)return false;interrupt(s);active=null;dispose(s);s.samples=[];s.panel.remove();window.Capacitor?.Plugins?.ArchiveDirectory?.setVideoImmersive?.({enabled:false}).catch(()=>{});return true}
window.closeBodyMultiPhotoTest=close;
window.pauseBodyMultiPhotoTest=()=>{if(active?.busy)interrupt(active)};
function busy(s,value){s.busy=value;$(s,'#multiScan').disabled=value;$(s,'#multiCancel').hidden=!value;$(s,'#multiApprove').disabled=value;$(s,'#multiEditClothing').disabled=value;$(s,'#multiReviewPhoto').disabled=value;for(const input of s.panel.querySelectorAll('[data-multi-clothing],[data-multi-select],#multiSameBody'))input.disabled=value;updateStart(s)}
function updateStart(s){const count=s.proposal?s.proposal.sources.filter(x=>s.selected.has(x.mediaId)).length:0;$(s,'#multiRun').disabled=s.busy||count<4||!$(s,'#multiSameBody').checked}
function blockedClothing(shoot,mediaId){
 if(shoot.bodyMeasurementAssessmentSignature!==shootingBodyMeasurementSignature(shoot))return false;
 const measures=shoot.bodyMeasurementPhotoAssessments?.[mediaId]?.measures||{};
 return measures.waist==='exclude-clothing'&&measures.hips==='exclude-clothing';
}
async function loadPhoto(s,media){
 check(s);const blob=await getMedia(media.id);check(s);if(!blob||typeof blob.arrayBuffer!=='function')throw Error('Foto konnte nicht gelesen werden: '+(media.fileName||media.id));
 if(!crypto?.subtle)throw Error('Die lokale Dateiprüfung ist auf diesem Gerät nicht verfügbar.');
 if(blob.size>64*1024*1024)throw Error('Foto ist für diesen Pilot zu groß.');
 const bytes=await blob.arrayBuffer(),head=new Uint8Array(bytes,0,Math.min(12,bytes.byteLength)),ascii=(a,b)=>String.fromCharCode(...head.slice(a,b));
 const raster=(head[0]===255&&head[1]===216&&head[2]===255)||(head[0]===137&&ascii(1,4)==='PNG')||(ascii(0,4)==='RIFF'&&ascii(8,12)==='WEBP')||(ascii(4,8)==='ftyp'&&['avif','avis'].includes(ascii(8,12)));if(!raster)throw Error('Kein unterstütztes Rasterfoto.');
 const digest=await crypto.subtle.digest('SHA-256',bytes),fingerprint='sha256:'+Array.from(new Uint8Array(digest),x=>x.toString(16).padStart(2,'0')).join('');check(s);
 const url=URL.createObjectURL(blob),image=new Image();
 try{
  await new Promise((resolve,reject)=>{const stop=()=>reject(Error('Verarbeitung abgebrochen.'));s.cancelImage=stop;image.onload=resolve;image.onerror=()=>reject(Error('Bild konnte nicht dekodiert werden.'));image.src=url});check(s);
  const factor=Math.min(1,1280/Math.max(image.naturalWidth,image.naturalHeight)),canvas=document.createElement('canvas');canvas.width=Math.round(image.naturalWidth*factor);canvas.height=Math.round(image.naturalHeight*factor);
  if(canvas.width<64||canvas.height<64)throw Error('Foto ist zu klein.');const ctx=canvas.getContext('2d');ctx.fillStyle='#000';ctx.fillRect(0,0,canvas.width,canvas.height);ctx.drawImage(image,0,0,canvas.width,canvas.height);return {canvas,fingerprint};
 }finally{s.cancelImage=null;image.onload=image.onerror=null;image.src='';URL.revokeObjectURL(url)}
}
function bikiniWindow(g){const sy=(g.leftShoulder.y+g.rightShoulder.y)/2,hy=(g.leftHip.y+g.rightHip.y)/2;return [sy+.45*(hy-sy),hy-.12*(hy-sy)]}
function markBikiniWindow(canvas,g){const [lo,hi]=bikiniWindow(g),ctx=canvas.getContext('2d');ctx.fillStyle='rgba(20,184,156,.12)';ctx.fillRect(canvas.width*.1,lo*canvas.height,canvas.width*.8,(hi-lo)*canvas.height);ctx.strokeStyle='#14b89c';ctx.lineWidth=Math.max(1,canvas.width/200);ctx.strokeRect(canvas.width*.1,lo*canvas.height,canvas.width*.8,(hi-lo)*canvas.height)}
function displayThumb(sample,clothing){return clothing==='bikini'?sample.bikiniThumb:sample.thumb}
function thumbnail(canvas,g=null){const t=document.createElement('canvas'),f=Math.min(1,240/Math.max(canvas.width,canvas.height));t.width=Math.max(1,Math.round(canvas.width*f));t.height=Math.max(1,Math.round(canvas.height*f));t.getContext('2d').drawImage(canvas,0,0,t.width,t.height);if(g)markBikiniWindow(t,g);const data=t.toDataURL('image/jpeg',.78);t.width=t.height=1;return data}
function frame(geometry,width,height){const p=Object.values(geometry||{}).filter(x=>x.confidence>=.6&&[x.x,x.y].every(Number.isFinite)),xs=p.map(x=>x.x*width),ys=p.map(x=>x.y*height),span=Math.max(...ys)-Math.min(...ys);if(p.length<12||span<height*.35)throw Error('Kein vollständiger Personenrahmen.');const box=[Math.max(0,Math.min(...xs)-span*.04),Math.max(0,Math.min(...ys)-span*.10),Math.min(width,Math.max(...xs)+span*.04),Math.min(height,Math.max(...ys)+span*.03)];BodyPhotoMath.crop(box,width,height);return box}
function observations(mask,clothing){
 const safe=BodyPhotoContour.safeRows(mask);if(safe.rows.length<12)throw Error('Zu wenig freie, sichere Rumpfkontur.');let rows=safe.rows,window=null;
 if(clothing==='bikini'){
  window=bikiniWindow(mask.geometry).map(y=>y*mask.height);rows=rows.filter(r=>r.y+.5>=window[0]&&r.y+.5<=window[1]);
 }
 if(rows.length<12)throw Error('Zu wenig freie, sichere Rumpfkontur.');return {rows,window};
}
function showClothing(s){
 dispose(s);s.expandedImage=null;$(s,'#multiResult').hidden=true;
 $(s,'#multiClothing').hidden=false;$(s,'#multiProposal').hidden=true;s.proposal=null;s.selected=new Set();
 $(s,'#multiClothingGrid').innerHTML=s.samples.map((x,i)=>`<label class="bodyMultiCard"><img src="${x.thumb}" alt="Foto zur Kleidungsprüfung"><span>${esc(x.name)}</span><button type="button" data-multi-enlarge="${i}">Größer ansehen</button><select data-multi-clothing="${i}" aria-label="Kleidungsfreigabe für ${esc(x.name)}"><option value="unknown">Nicht freigegeben</option><option value="free_contour">Freie Rumpfkontur · kein Rock/Kleid</option><option value="bikini">Bikini · markierter Bereich frei</option></select></label>`).join('');
 for(const select of s.panel.querySelectorAll('[data-multi-clothing]')){const sample=s.samples[Number(select.dataset.multiClothing)];select.value=approvals.get(sample.fingerprint)||'unknown';select.closest('.bodyMultiCard').querySelector('img').src=displayThumb(sample,select.value);select.onchange=()=>{if(s.busy)return;if(select.value==='unknown')approvals.delete(sample.fingerprint);else approvals.set(sample.fingerprint,select.value);const card=select.closest('.bodyMultiCard');card.querySelector('img').src=displayThumb(sample,select.value);card.classList.remove('bodyMultiExpanded');card.querySelector('[data-multi-enlarge]').textContent='Größer ansehen';if(s.expandedImage?.card===card)s.expandedImage=null}}
 for(const button of s.panel.querySelectorAll('[data-multi-enlarge]'))button.onclick=async event=>{
  event.preventDefault();if(s.busy)return;const card=button.closest('.bodyMultiCard'),image=card.querySelector('img'),sample=s.samples[Number(button.dataset.multiEnlarge)];
  if(card.classList.contains('bodyMultiExpanded')){image.src=displayThumb(sample,card.querySelector('select').value);card.classList.remove('bodyMultiExpanded');button.textContent='Größer ansehen';s.expandedImage=null;return}
  s.cancelled=false;busy(s,true);let canvas;
  try{const loaded=await loadPhoto(s,sample.media);canvas=loaded.canvas;if(loaded.fingerprint!==sample.fingerprint)throw Error('Foto geändert. Bitte Vorausprüfung neu starten.');
   if(s.expandedImage){const old=s.expandedImage;old.image.src=old.thumb;old.card.classList.remove('bodyMultiExpanded');old.button.textContent='Größer ansehen'}
   if(card.querySelector('select').value==='bikini')markBikiniWindow(canvas,sample.geometry);image.src=canvas.toDataURL('image/jpeg',.85);card.classList.add('bodyMultiExpanded');button.textContent='Verkleinern';s.expandedImage={image,card,button,thumb:displayThumb(sample,card.querySelector('select').value)};
  }catch(error){if(own(s))status(s,error.message,true)}finally{if(canvas)canvas.width=canvas.height=1;if(own(s))busy(s,false)}
 }
}
function proposal(s){
 if(s.busy)return;s.cancelled=false;check(s);for(const select of s.panel.querySelectorAll('[data-multi-clothing]')){const sample=s.samples[Number(select.dataset.multiClothing)];sample.clothing=select.value;if(select.value!=='unknown')approvals.set(sample.fingerprint,select.value)}
 const usable=[];for(const sample of s.samples){try{if(!['free_contour','bikini'].includes(sample.clothing))continue;observations(sample.mask,sample.clothing);usable.push(sample)}catch(_){}}
 const chosen=Body3DMultiViewCore.selectSources(usable,{groupKey:s.groupKey,maxSources:6,minSources:4});
 if(!chosen.ready){status(s,'Es fehlen mindestens vier ausreichend unterschiedliche, freigegebene Fotos mit freier Kontur. Bitte wenige weitere geeignete Fotos freigeben. Röcke/Kleider bleiben ausgeschlossen.',true);return}
 s.proposal=chosen;s.selected=new Set(chosen.sources.map(x=>x.mediaId));$(s,'#multiClothing').hidden=true;$(s,'#multiProposal').hidden=false;$(s,'#multiSameBody').checked=false;
 $(s,'#multiProposalGrid').innerHTML=chosen.sources.map(source=>{const x=s.samples.find(x=>x.mediaId===source.mediaId);return `<label class="bodyMultiCard"><img src="${displayThumb(x,x.clothing)}" alt="Vorgeschlagenes Foto"><span><input type="checkbox" data-multi-select="${esc(x.mediaId)}" checked> ${esc(x.name)}</span><small data-multi-role="${esc(x.mediaId)}"></small></label>`}).join('');
 const refresh=()=>{const selected=chosen.sources.filter(x=>s.selected.has(x.mediaId));for(const el of s.panel.querySelectorAll('[data-multi-role]'))el.textContent=!s.selected.has(el.dataset.multiRole)?'Nicht ausgewählt':selected.at(-1)?.mediaId===el.dataset.multiRole?'Kontrollaufnahme · nicht für die Formanpassung':'Formanpassung';updateStart(s)};
 for(const input of s.panel.querySelectorAll('[data-multi-select]'))input.onchange=()=>{if(input.checked)s.selected.add(input.dataset.multiSelect);else s.selected.delete(input.dataset.multiSelect);refresh()};refresh();status(s,'Vorschlag bereit. Mindestens vier Fotos auswählen und den gemeinsamen Körperstand einmal bestätigen. Die letzte ausgewählte Aufnahme bleibt Formkontrolle.');
}
async function scan(s){
 if(s.busy)return;s.cancelled=false;dispose(s);s.samples=[];s.proposal=null;$(s,'#multiResult').hidden=true;$(s,'#multiClothing').hidden=true;$(s,'#multiProposal').hidden=true;busy(s,true);
 try{
  const plugin=window.Capacitor?.Plugins?.ArchiveDirectory;if(!plugin?.analyzePose)throw Error('Lokale Fotoanalyse fehlt in dieser APK.');
  const all=photos(s.shoot),pool=all.length<=40?all:Array.from({length:40},(_,i)=>all[Math.round(i*(all.length-1)/39)]);let failures=0,blocked=0;
  for(let i=0;i<pool.length;i++){
   check(s);status(s,`Lokale Vorausprüfung ${i+1}/${pool.length}. Originalfotos bleiben auf dem Handy.`);const media=pool[i];if(blockedClothing(s.shoot,media.id)){blocked++;continue}
   let canvas;try{
    const loaded=await loadPhoto(s,media);canvas=loaded.canvas;const result=await plugin.analyzePose({dataUrl:canvas.toDataURL('image/jpeg',.8),includePhotoMask:true});check(s);
    const sample={mediaId:media.id,groupKey:s.groupKey,fingerprint:loaded.fingerprint,name:media.fileName||media.title||'Foto',fullBody:result.fullBody===true,safelyFramed:result.safelyFramed===true,geometry:result.geometry||{},clothing:approvals.get(loaded.fingerprint)||'unknown',media};
    if(!Body3DMultiViewCore.descriptor(sample))continue;sample.mask=BodyPhotoContour.read(result,canvas.width,canvas.height);if(BodyPhotoContour.safeRows(sample.mask).rows.length<12)continue;
    sample.width=canvas.width;sample.height=canvas.height;sample.box=frame(sample.geometry,canvas.width,canvas.height);sample.thumb=thumbnail(canvas);sample.bikiniThumb=thumbnail(canvas,sample.geometry);s.samples.push(sample);
   }catch(error){check(s);failures++}finally{if(canvas)canvas.width=canvas.height=1}
  }
  check(s);showClothing(s);status(s,`${s.samples.length} Fotos für die Kleidungsprüfung verfügbar; ${blocked} bereits wegen Kleidung ausgeschlossen, ${failures} nicht lesbar oder ohne passende Kontur. ${s.samples.filter(x=>approvals.has(x.fingerprint)).length?'Bereits in dieser Sitzung freigegebene unveränderte Dateien sind vorausgewählt.':'Noch kein Foto als Quelle freigegeben.'} Markiere nur wenige geeignete Fotos ohne Rock oder Kleid. Bikinis sind für freie Teilbereiche möglich.`);
 }catch(error){if(own(s))status(s,error.message,true)}finally{if(own(s))busy(s,false)}
}
function medians(arrays,start,end){return Array.from({length:end-start},(_,k)=>{const a=arrays.map(x=>x[k+start]).sort((a,b)=>a-b),m=Math.floor(a.length/2);return Math.fround(a.length%2?a[m]:(a[m-1]+a[m])/2)})}
function edgesFrom(faces){const set=new Set(),edges=[];for(let f=0;f<faces.length;f+=3)for(let k=0;k<3;k++){const a=faces[f+k],b=faces[f+(k+1)%3],lo=Math.min(a,b),hi=Math.max(a,b),key=lo*18439+hi;if(!set.has(key)){set.add(key);edges.push(lo,hi)}}return new Uint32Array(edges)}
function modelGeometry(points,camera,width,height){const indices={leftShoulder:5,rightShoulder:6,leftElbow:7,rightElbow:8,leftWrist:62,rightWrist:41,leftHip:9,rightHip:10},g={};for(const [name,i] of Object.entries(indices)){const p=BodyPhotoMath.project(points[i],camera,width,height);if(p)g[name]={x:p[0]/width,y:p[1]/height,confidence:1}}return g}
function targets(sample,mesh,points,camera){
 const observed=observations(sample.mask,sample.clothing),model={width:sample.mask.width,height:sample.mask.height,data:BodyPhotoContour.projectedMask(mesh.vertices,mesh.faces,camera,sample.width,sample.height,sample.mask.width,sample.mask.height),geometry:modelGeometry(points,camera,sample.width,sample.height)};
 const allowed=new Map(BodyPhotoContour.safeRows(model).rows.map(r=>[r.y,r])),safe=observed.rows.filter(r=>{const m=allowed.get(r.y);return m&&m.left<=r.center&&m.right>=r.center});if(safe.length<12)throw Error(sample.name+': zu wenig freie Kontur nach der Modell-/Armprüfung. Keine gemeinsame Form übernommen.');
 const stride=Math.max(1,Math.ceil(safe.length/24)),rows=safe.filter((_,i)=>i%stride===0),sx=sample.width/sample.mask.width,sy=sample.height/sample.mask.height;
 const safeY=new Set(safe.map(r=>r.y)),links=rows.map((r,i)=>{if(i===0)return false;for(let y=rows[i-1].y;y<=r.y;y++)if(!safeY.has(y))return false;return true});
 return {links,rows:rows.map(r=>(r.y+.5)*sy),torsoWidths:rows.map(r=>{const m=allowed.get(r.y);return (m.right-m.left)*sx}),centers:rows.map(r=>r.center*sx),target:rows.flatMap(r=>[r.left*sx,r.right*sx]),clothingWindow:observed.window?.map(y=>y*sy)};
}
async function showComparison(s,id){
 const item=s.review?.find(x=>x.sample.mediaId===id);if(!item)return;let canvas;
 try{
  const loaded=await loadPhoto(s,item.sample.media);canvas=loaded.canvas;if(loaded.fingerprint!==item.sample.fingerprint)throw Error('Foto geändert. Bitte Vorausprüfung neu starten.');
  const target=$(s,'#multiPhotoCanvas');target.width=canvas.width;target.height=canvas.height;const ctx=target.getContext('2d');ctx.drawImage(canvas,0,0);
  const scale=canvas.width/Math.max(1,target.clientWidth||canvas.width);
  for(const [values,color] of [[item.observation.target,'#14b89c'],[item.score.beforeOutline,'#dc7800'],[item.score.afterOutline,'#7543a0']])for(let side=0;side<2;side++){
   ctx.beginPath();ctx.strokeStyle=color;ctx.lineWidth=2.1*scale;let last=false;
   item.observation.rows.forEach((y,i)=>{const x=values?.[2*i+side];if(!Number.isFinite(x)){last=false;return}if(last&&item.observation.links[i])ctx.lineTo(x,y);else ctx.moveTo(x,y);last=true});ctx.stroke();
  }
  $(s,'#multiPhotoReview').hidden=false;
 }finally{if(canvas)canvas.width=canvas.height=1}
}
async function workerFit(s,group){
 const scripts=await Promise.all(['body3d-multiview.js','body3d-multiview-worker.js'].map(async path=>{const response=await fetch(path+'?v=group1',{cache:'no-store'});if(!response.ok)throw Error('Lokale Mehrfoto-Programmdatei fehlt.');return await response.text()}));check(s);
 const workerUrl=URL.createObjectURL(new Blob([scripts.join('\n')],{type:'application/javascript'}));let worker;
 try{worker=new Worker(workerUrl)}catch(error){URL.revokeObjectURL(workerUrl);throw Error('Lokaler Mehrfoto-Worker nicht verfügbar.')}
 s.worker=worker;
 return await new Promise((resolve,reject)=>{
  let done=false;const finish=(error,result)=>{if(done)return;done=true;worker.terminate();URL.revokeObjectURL(workerUrl);s.worker=null;s.abortWorker=null;error?reject(error):resolve(result)};s.abortWorker=()=>finish(Error('Verarbeitung abgebrochen.'));
  worker.onerror=()=>finish(Error('Der lokale Mehrfoto-Worker konnte nicht ausgeführt werden.'));
  worker.onmessage=event=>{if(!own(s)||s.cancelled)return finish(Error('Verarbeitung abgebrochen.'));const data=event.data;if(data.type==='progress')status(s,(data.progress.phase==='cameras'?'Kameras abgleichen':'Gemeinsame Form anpassen')+` · Schritt ${data.progress.iteration+1}.`);else if(data.type==='error'){const message=/Time budget/.test(data.message)?'Die gemeinsame Anpassung hat das Zeitbudget überschritten und wurde verworfen.':/connected model torso|silhouette|torso range/.test(data.message)?'Foto- und Modellrumpf lassen sich nicht sicher gemeinsam vergleichen. Keine Form übernommen.':'Die lokale Mehrfoto-Anpassung konnte nicht sicher berechnet werden. Keine Form übernommen.';finish(Error(message))}else if(data.type==='result')finish(null,data.result)};
  const transfer=new Set();for(const v of group.views)for(const key of ['base','basis','edges','triangles'])if(ArrayBuffer.isView(v[key]))transfer.add(v[key].buffer);
  worker.onmessageerror=()=>finish(Error('Die lokalen Mehrfoto-Daten konnten nicht übertragen werden.'));
  try{worker.postMessage({type:'fit',group},[...transfer])}catch(error){finish(Error('Die lokalen Mehrfoto-Daten konnten nicht übertragen werden.'))}
 });
}
async function run(s){
 if(s.busy||!s.proposal||!$(s,'#multiSameBody').checked)return;const chosen=s.proposal.sources.filter(x=>s.selected.has(x.mediaId));if(chosen.length<4)return;
 s.cancelled=false;dispose(s);$(s,'#multiResult').hidden=true;busy(s,true);
 try{
  check(s);const plugin=window.Capacitor?.Plugins?.Body3D;if(!plugin?.analyzePhoto||!plugin?.prepareSharedFormBasis)throw Error('Für Mehrfoto wird die neue Android-APK benötigt.');
  const sources=chosen.map((x,i)=>({...s.samples.find(a=>a.mediaId===x.mediaId),role:i===chosen.length-1?'control':'fit'})),analyses=[];let faces;
  for(let i=0;i<sources.length;i++){
   const sample=sources[i];check(s);status(s,`Foto ${i+1}/${sources.length} lokal analysieren · ${sample.name}`);let canvas;
   try{
    const loaded=await loadPhoto(s,sample.media);canvas=loaded.canvas;if(loaded.fingerprint!==sample.fingerprint||canvas.width!==sample.width||canvas.height!==sample.height)throw Error('Fotodatei wurde seit der Auswahl geändert. Bitte neu prüfen.');
    const crop=BodyPhotoMath.crop(sample.box,canvas.width,canvas.height),patch=document.createElement('canvas');patch.width=patch.height=224;const ctx=patch.getContext('2d');ctx.fillStyle='#000';ctx.fillRect(0,0,224,224);ctx.imageSmoothingEnabled=true;ctx.imageSmoothingQuality='low';const scale=224/crop.side;ctx.drawImage(canvas,-crop.left*scale,-crop.top*scale,canvas.width*scale,canvas.height*scale);const tensor=BodyPhotoMath.base64(BodyPhotoMath.tensor(ctx.getImageData(0,0,224,224).data));patch.width=patch.height=1;
    const result=await plugin.analyzePhoto({tensor,includeParameters:true,refinePose:false,compareForm:false,input:{width:sample.width,height:sample.height,box:sample.box}});check(s);
    if(result.parameterRevision!==1||result.identityParameters?.length!==45||result.poseParameters?.length!==204||![...result.identityParameters,...result.poseParameters,...(result.camera||[])].every(Number.isFinite)||result.camera?.length!==3||result.joints2d?.length!==70)throw Error('Latente Fotoparameter fehlen oder sind ungültig. Neue APK erforderlich.');
    if(!faces){faces=BodyPhotoMath.unpack(result.faces,'short',36874*3);s.previewFaces=faces.slice()}
    analyses.push({sample,shape:result.identityParameters,pose:result.poseParameters,camera:result.camera,joints2d:result.joints2d});
   }finally{if(canvas)canvas.width=canvas.height=1}
  }
  const fitSources=analyses.filter(x=>x.sample.role==='fit'),commonShape=medians(fitSources.map(x=>x.shape),0,45),commonSizes=medians(fitSources.map(x=>x.pose),130,204),edges=edgesFrom(faces),views=[];
  for(let i=0;i<analyses.length;i++){
   check(s);const a=analyses[i],sample=a.sample,pose=a.pose.slice();pose.splice(130,74,...commonSizes);status(s,`Gemeinsame Formbasis ${i+1}/${analyses.length} · ${sample.name}. Pose und Kamera werden für dieses Foto initialisiert.`);
   const prepared=await plugin.prepareSharedFormBasis({commonShape,photoPose:pose,refinePose:true,camera:a.camera,joints2d:a.joints2d,input:{width:sample.width,height:sample.height,box:sample.box}});check(s);
   if(prepared.preparationRevision!==2||prepared.basisRevision!==1||prepared.coordinateFrame!=='vision_metres'||prepared.joints3d?.length!==70||prepared.camera?.length!==3)throw Error('Die aktuelle Mehrfoto-Formbasis benötigt die neue APK.');
   const base=BodyPhotoMath.unpack(prepared.base,'float',55317),basis=BodyPhotoMath.unpack(prepared.basis,'float',55317*20);let camera=prepared.camera;
   if(!prepared.poseRefinement?.applied){const candidate=BodyPhotoMath.fitCamera({pointDiagnosticRevision:1,joints2d:a.joints2d,joints3d:prepared.joints3d,camera},BodyPhotoMath.crop(sample.box,sample.width,sample.height),sample.width,sample.height);if(candidate)camera=candidate.camera}
   const observation=targets(sample,{vertices:base,faces},prepared.joints3d,camera);
   views.push({sourceId:sample.mediaId,fingerprint:sample.fingerprint,groupKey:s.groupKey,role:sample.role,clothing:sample.clothing,referenceBodyDelta:a.shape.slice(0,20).map((x,k)=>x-commonShape[k]),...observation,base,basis,camera,edges,triangles:faces,commonShape:prepared.commonShape,fixedParameters:prepared.fixedParameters,basisRevision:prepared.basisRevision,topologySha256:prepared.topologySha256,focal:Math.hypot(sample.width,sample.height),center:[sample.width/2,sample.height/2]});
  }
  check(s);status(s,'Gemeinsame Form wird lokal im Hintergrund-Worker angepasst. Das kann länger dauern; die App bitte geöffnet lassen.');const result=await workerFit(s,{groupKey:s.groupKey,views});check(s);
  const rejected={not_converged:'Keine gemeinsame Form übernommen: Die Berechnung konnte nicht stabil abgeschlossen werden.',invalid_contour:'Keine gemeinsame Form übernommen: Mindestens eine Kontur ist nicht sicher vergleichbar.',independent_photo_regression:'Keine gemeinsame Form übernommen: Das Kontrollfoto wird gegenüber seiner Einzelbild-Körperform schlechter erklärt.',insufficient_improvement:'Keine gemeinsame Form übernommen: Anpassung oder Kontrollaufnahme besteht die Verbesserungskriterien nicht.'};
  $(s,'#multiResult').hidden=false;$(s,'#multiReport').innerHTML='<p>'+esc(result.accepted?'Gemeinsamer Formkandidat besteht die interne Konturkontrolle. Keine bestätigte Körperform oder Maßgenauigkeit.':(rejected[result.rejectionReason]||'Keine gemeinsame Form übernommen. Die Konturkontrolle wurde nicht bestanden.'))+'</p><table><thead><tr><th>Foto</th><th>Kontur-RMS im Arbeitsbild · vorher → nachher</th></tr></thead><tbody>'+result.scores.map(score=>{const sample=sources.find(x=>x.mediaId===score.sourceId);return `<tr><td>${esc(sample.name)}${score.role==='control'?' · Kontrolle':''}</td><td>${Number.isFinite(score.before)?score.before.toFixed(1):'nicht vergleichbar'} → ${Number.isFinite(score.after)?score.after.toFixed(1):'nicht vergleichbar'} Bildpixel</td></tr>`}).join('')+'</tbody></table><p>Die Ausgangswerte beziehen sich auf die gemeinsame Startform. Bei vergleichbarer Einzelbild-Körperform darf der gemeinsame Kandidat das Kontrollfoto zusätzlich nicht verschlechtern. Bikinifotos prüfen nur freie Teilbereiche.</p><p>Posen wurden vorab geschätzt und bleiben im Formfit fest. Kontrollkontur nicht für die Anpassung verwendet; deren Pose/Kamera wurde vorab aus Bildpunkten geschätzt. Masken, Kleidung und Perspektive können weiterhin falsch sein. Bikinifotos prüfen nur freie Teilbereiche; verdeckte Bereiche bleiben Modellannahme. Noch keine Maße und keine Profiländerungen.</p>';
  $(s,'#multiPreview').hidden=!result.accepted;
  if(result.accepted){
   s.review=sources.map(sample=>({sample,observation:views.find(v=>v.sourceId===sample.mediaId),score:result.scores.find(v=>v.sourceId===sample.mediaId)}));
   $(s,'#multiReviewPhoto').innerHTML=sources.map(sample=>`<option value="${esc(sample.mediaId)}">${esc(sample.name)}${sample.role==='control'?' · Kontrollaufnahme':''}</option>`).join('');const control=sources.find(x=>x.role==='control');$(s,'#multiReviewPhoto').value=control.mediaId;
   const pose=Array(130).fill(0).concat(commonSizes);status(s,'Gemeinsamen neutralen Formkandidaten berechnen …');const neutral=await plugin.prepareSharedFormBasis({commonShape:result.identity,photoPose:pose,baseOnly:true});check(s);if(neutral.preparationRevision!==2||neutral.bodyCoefficientCount!==0)throw Error('Neutrale Vorschau fehlt.');const vertices=BodyPhotoMath.unpack(neutral.base,'float',55317),data={vertices,faces:s.previewFaces,normals:BodyPhotoMath.normals(vertices,s.previewFaces),camera:[0,0,3]};s.view=createBodyPhotoRenderer($(s,'#multiOrbit'),data,'orbit',{width:600,height:720});$(s,'#multiYaw').value='0';s.view.draw(0,1);await showComparison(s,control.mediaId);check(s);
  }
  status(s,result.accepted?'Mehrfoto-Test abgeschlossen. Ergebnis nur in dieser Ansicht; keine Maße oder Profilwerte übernommen.':'Mehrfoto-Test abgeschlossen. Kein gemeinsames Ergebnis übernommen. Der Prüfbericht bleibt sichtbar.');
 }catch(error){if(own(s)){dispose(s);$(s,'#multiPreview').hidden=true;status(s,error.message,true)}}finally{if(own(s))busy(s,false)}
}
function open(shootId){
 if(!window.Body3DMultiViewCore||Body3DMultiViewCore.revision!==2){toast('Neue Mehrfoto-Programmdateien fehlen. Bitte die geprüfte neue APK verwenden.');return}
 const shoot=(state.archive||[]).find(x=>x.id===shootId&&x.type==='shoot');if(!shoot||shootingModelIds(shoot).length!==1||photos(shoot).length<4){toast('Für den Pilot sind mindestens vier Fotos eines einzelnen Models im selben Shooting nötig.');return}
 close();window.closeBodyPhotoTest?.();if(typeof stopLightboxSlideshow==='function')stopLightboxSlideshow();const panel=document.createElement('section');panel.className='bodyPhotoPanel';panel.setAttribute('role','dialog');panel.setAttribute('aria-modal','true');panel.setAttribute('aria-label','Mehrfoto-3D-Test');
 panel.innerHTML=`<div class="bodyPhotoInner"><div class="bodyPhotoHead"><h2>Mehrfoto-3D · Test</h2><button type="button" class="bodyPhotoClose" id="multiClose" aria-label="Mehrfoto-Test schließen">×</button></div><p>${esc(shootingDisplayLabel(shoot))}</p><p>Lokaler Pilot mit vier bis sechs Fotos eines Shootings. Bei größeren Shootings werden zunächst bis zu 40 gleichmäßig verteilte Fotos geprüft. Eine gemeinsame Körperform, eigene vorab geschätzte Pose und Kamera je Foto. Noch keine Maße oder Profiländerungen. Verarbeitung kann einige Minuten dauern.</p><p id="multiStatus" role="status" aria-live="polite"></p><div class="bodyPhotoControls bodyPhotoFrameControls"><button type="button" id="multiScan">Vorausprüfung neu starten</button><button type="button" id="multiCancel" hidden>Abbrechen</button></div><section id="multiClothing" hidden><h3>Kleidung zuerst prüfen</h3><p>Diese Bilder sind noch keine Quellenvorschläge. Gib nur wenige geeignete Fotos mit genau einer vollständig erkennbaren Person frei. <b>Keine Röcke oder Kleider.</b> „Freie Rumpfkontur“ nur wählen, wenn die Kontur nicht durch Kleidung verdeckt wird. Bei Bikinis wird der genutzte mittlere Rumpfabschnitt markiert. Gib das Foto nur frei, wenn dieser Bereich wirklich unbedeckt ist. Unklare Bilder bleiben ausgeschlossen. Freigaben gelten nur für diese App-Sitzung und dieselbe unveränderte Datei.</p><div class="bodyMultiGrid" id="multiClothingGrid"></div><button type="button" id="multiApprove">Aus freigegebenen Fotos Vorschlag bilden</button></section><section id="multiProposal" hidden><h3>Gemeinsamer Vorschlag</h3><p>Mindestens vier ausgewählt lassen. Die letzte ausgewählte Aufnahme bleibt Kontrollfoto. Poseverschiedenheit garantiert noch keine ergänzende Seiten-/Rückansicht.</p><div class="bodyMultiGrid" id="multiProposalGrid"></div><label><input type="checkbox" id="multiSameBody"> Die ausgewählten Fotos zeigen dieselbe Person im selben Körperstand.</label><div class="bodyPhotoControls"><button type="button" id="multiRun" disabled>Auswahl bestätigen und gemeinsam berechnen</button><button type="button" id="multiEditClothing">Freigaben ändern</button></div></section><section id="multiResult" hidden><h3>Prüfergebnis</h3><div id="multiReport"></div><div id="multiPhotoReview" hidden><h3>Am Foto gegenprüfen</h3><label>Foto <select id="multiReviewPhoto"></select></label><p>Grün: erkannte Personenkontur · Orange: gemeinsame Startform vor der Anpassung · Violett: gemeinsamer Formkandidat. Ausgelassene Abschnitte bleiben ungeprüft. Bei der Kontrollaufnahme bleibt die zuvor geschätzte Kamera für beide Formen gleich.</p><canvas id="multiPhotoCanvas" style="display:block;width:100%;height:auto;border-radius:12px" role="img" aria-label="Foto mit erkannter Kontur und gemeinsamem Formkandidaten"></canvas></div><div id="multiPreview" hidden><h3>Gemeinsamer neutraler Formkandidat</h3><canvas id="multiOrbit" class="bodyPhotoOrbit" aria-label="Gemeinsamer Formkandidat"></canvas><label>Drehen <input type="range" id="multiYaw" min="-180" max="180" value="0"></label></div></section></div>`;
 document.body.appendChild(panel);const s={panel,shoot,shootId,archive:state.archive,signature:signature(shoot),groupKey:JSON.stringify(['shoot',shootId,shootingModelIds(shoot)[0]]),samples:[],selected:new Set(),busy:false,cancelled:false};active=s;
 $(s,'#multiClose').onclick=close;$(s,'#multiScan').onclick=()=>scan(s);$(s,'#multiCancel').onclick=()=>interrupt(s);$(s,'#multiApprove').onclick=()=>{try{proposal(s)}catch(error){status(s,error.message,true)}};$(s,'#multiRun').onclick=()=>run(s);$(s,'#multiReviewPhoto').onchange=async()=>{if(s.busy)return;s.cancelled=false;busy(s,true);try{status(s,'Fotovergleich laden …');await showComparison(s,$(s,'#multiReviewPhoto').value);status(s,'Fotovergleich angezeigt. Noch keine Maße oder Profiländerungen.')}catch(error){if(own(s)){dispose(s);status(s,error.message,true)}}finally{if(own(s))busy(s,false)}};$(s,'#multiSameBody').onchange=()=>updateStart(s);$(s,'#multiEditClothing').onclick=()=>{if(!s.busy)showClothing(s)};$(s,'#multiYaw').oninput=()=>s.view?.draw(Number($(s,'#multiYaw').value),1);$(s,'#multiClose').focus({preventScroll:true});window.Capacitor?.Plugins?.ArchiveDirectory?.setVideoImmersive?.({enabled:false}).catch(()=>{});scan(s);
}
window.openBodyMultiPhotoTest=open;
document.addEventListener('keydown',event=>{if(active&&event.key==='Escape'){event.preventDefault();event.stopImmediatePropagation();close()}},true);
document.addEventListener('visibilitychange',()=>{if(document.hidden&&active?.busy)interrupt(active)});
window.addEventListener('resize',()=>{if(active?.view)active.view.draw(Number($(active,'#multiYaw').value),1)});
})();
