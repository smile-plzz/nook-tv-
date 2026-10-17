'use strict';
const assert=require('node:assert/strict'),fs=require('node:fs'),vm=require('node:vm'),crypto=require('node:crypto');
class Element{constructor(){this.children=[];this.files=[];this.value='';}append(...v){this.children.push(...v);}replaceChildren(...v){this.children=[...v];}}
const elements=new Map(),requests=[],uploads=[];let data;
class XHR{constructor(){this.upload={};this.headers={};uploads.push(this);}open(method,path){this.method=method;this.path=path;}setRequestHeader(k,v){this.headers[k]=v;}send(file){this.file=file;}abort(){this.onabort();}}
let nextPrompt="Updated Title";
const context=vm.createContext({
  document:{getElementById:id=>{if(!elements.has(id))elements.set(id,new Element());return elements.get(id);},createElement:()=>new Element()},
  crypto:crypto.webcrypto,Uint8Array,Blob,URL,AbortSignal,XMLHttpRequest:XHR,
  localStorage:{getItem(){return null;},setItem(){},removeItem(){}},
  setInterval(){},clearInterval(){},setTimeout(){},
  confirm:()=>true,
  prompt:()=>nextPrompt,
  window:{addEventListener(){}},
  fetch:async(path,options)=>{
    requests.push({path,options});
    if(options?.body){
      const body=JSON.parse(options.body);
      if(path==='/api/media/update'){
        const item=data.items.find(i=>i.id===body.id);
        if(item){
          if(body.label!==undefined)item.label=body.label;
          if(body.pinned!==undefined)item.pinned=body.pinned;
          if(body.collection!==undefined)item.collection=body.collection;
          if(body.hidden!==undefined)item.hidden=body.hidden;
        }
        return{ok:true,status:200,json:async()=>({status:'ok',item})};
      }
      if(path==='/api/media/delete'){
        data.items=data.items.filter(i=>i.id!==body.id);
        return{ok:true,status:200,json:async()=>({status:'deleted',id:body.id})};
      }
      if(path==='/api/media/reset'){
        return{ok:true,status:200,json:async()=>({status:'reset',id:body.id})};
      }
    }
    return{ok:true,status:200,json:async()=>data};
  }
});
for(const f of ['editor.js','media.js'])vm.runInContext(fs.readFileSync('android/app/src/main/assets/'+f,'utf8'),context);
const run=code=>vm.runInContext(code,context),status=()=>elements.get('media-status').textContent;
function reset(){run('window.nookMedia.disconnect();session={token:"tab-token",csrf:"tab-csrf"};');uploads.length=0;requests.length=0;data={items:[],usedBytes:0,quotaBytes:1073741824,freeBytes:2000000000,maxFileBytes:536870912,damaged:false,receiving:false};}
function choose(size=100,name='Phone video.mov'){elements.get('video-file').files=[{size,name}];elements.get('video-file').onchange();}
const send=()=>elements.get('send-video').onclick();
(async()=>{
  reset();choose(536870913);await send();assert.equal(uploads.length,0);assert.match(status(),/512 MiB/);
  reset();choose();run('session=null;');await send();assert.equal(uploads.length,0);assert.match(status(),/Pair/);
  reset();choose();await send();let xhr=uploads[0];assert.equal(xhr.path,'/api/media/upload');assert.equal(xhr.headers['Content-Type'],'application/octet-stream');assert.equal(xhr.headers.Authorization,'Bearer tab-token');assert.equal(xhr.headers['X-Nook-Filename'],'Phone%20video.mov');assert.equal(xhr.file.name,'Phone video.mov');
  xhr.upload.onprogress({lengthComputable:true,loaded:100,total:100});assert.match(status(),/checking and saving/);assert.doesNotMatch(status(),/^Saved on TV/);await send();assert.equal(uploads.length,1,'no overlapping browser upload');
  const receipt=xhr.headers['X-Nook-Upload-Id'];xhr.onerror();await send();xhr=uploads[1];assert.equal(xhr.headers['X-Nook-Upload-Id'],receipt,'uncertain retry retains transfer identity');
  elements.get('cancel-video').onclick();assert.match(status(),/cancelled/);assert.equal(elements.get('send-video').disabled,false);
  await send();xhr=uploads[2];xhr.status=200;xhr.responseText=JSON.stringify({item:{id:receipt}});xhr.onload();assert.match(status(),/^Saved on TV/);
  data={...data,items:[{id:receipt,available:true,label:'Phone video.mov',size:100,pinned:false,collection:'',hidden:false}]};await send();assert.equal(uploads.length,3,'server receipt prevents resending committed file');

  // Verify storage-before-send validation in video-file.onchange
  reset();
  await run('window.nookMedia.refresh();');
  data.freeBytes=50;
  choose(100);
  assert.equal(elements.get('send-video').disabled,true,'storage-before-send disables send button when free storage is exceeded');
  assert.match(status(),/Not enough TV storage/);

  // Verify rich media card rendering and management actions
  reset();
  data={...data,items:[{id:'0123456789abcdef0123456789abcdef',available:true,label:'Family trip.mov',size:20000000,duration:45000,pinned:false,collection:'Vacation',hidden:false}]};
  await run('window.nookMedia.refresh();');
  const list=elements.get('media-list');
  assert.equal(list.children.length,1,'media card rendered');
  const card=list.children[0];
  const fields=card.children[0];
  assert.equal(fields.children[0].textContent,'Family trip.mov');
  assert.match(fields.children[1].textContent,/Vacation/);
  assert.match(fields.children[1].textContent,/45s/);

  // Test action buttons on card
  const actions=card.children[1];
  const buttons=actions.children;
  assert.equal(buttons.length,6,'6 management buttons present: rename, pin, collection, hide, reset, delete');

  // Test Rename
  nextPrompt='Summer in Italy.mov';
  await buttons[0].onclick();
  assert.equal(data.items[0].label,'Summer in Italy.mov');
  assert.equal(status(),'Video renamed.');

  // Test Pin toggle
  await buttons[1].onclick();
  assert.equal(data.items[0].pinned,true);
  assert.equal(status(),'Pinned to Home.');

  // Test Collection change
  nextPrompt='Trips 2026';
  await buttons[2].onclick();
  assert.equal(data.items[0].collection,'Trips 2026');

  // Test Hide toggle (privacy)
  await buttons[3].onclick();
  assert.equal(data.items[0].hidden,true);
  assert.equal(status(),'Hidden from TV Home.');

  // Test Start over (reset progress)
  await buttons[4].onclick();
  assert.equal(status(),'Progress reset to start.');

  // Test Delete copy
  await buttons[5].onclick();
  assert.equal(data.items.length,0,'saved copy removed from media list after delete');
  assert.equal(status(),'Saved copy removed from TV.');

  reset();choose();run('window.nookMedia.disconnect();');assert.match(status(),/Pair again/);assert.equal(elements.get('video-file').value,'');
  console.log('PASS: binary upload auth, limits, progress, cancel, retry receipts, storage checks and companion media management');
})().catch(e=>{console.error(e);process.exitCode=1;});
