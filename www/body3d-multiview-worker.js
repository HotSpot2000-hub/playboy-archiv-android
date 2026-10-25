'use strict';
if(!self.Body3DMultiViewCore)importScripts('body3d-multiview.js');
self.onmessage=event=>{
 if(event.data?.type!=='fit')return;
 try{if(Body3DMultiViewCore.revision!==2)throw Error('Neue Mehrfoto-Programmdatei erforderlich.');const result=Body3DMultiViewCore.fit(event.data.group,{maxMs:90000,onProgress:progress=>self.postMessage({type:'progress',progress})});self.postMessage({type:'result',result})}
 catch(error){self.postMessage({type:'error',message:String(error?.message||error)})}
};
