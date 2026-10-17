'use strict';
const assert=require('node:assert/strict'),fs=require('node:fs'),vm=require('node:vm'),crypto=require('node:crypto');
class Element{constructor(){this.children=[];this.files=[];this.value='';}append(...v){this.children.push(...v);}replaceChildren(...v){this.children=[...v];}}
const elements=new Map(),requests=[],uploads=[];let data;
class XHR{constructor(){this.upload={};this.headers={};uploads.push(this);}open(method,path){this.method=method;this.path=path;}setRequestHeader(k,v){this.headers[k]=v;}send(file){this.file=file;}abort(){this.onabort();}}
const context=vm.createContext({document:{getElementById:id=>{if(!elements.has(id))elements.set(id,new Element());return elements.get(id);},createElement:()=>new Element()},crypto:crypto.webcrypto,Uint8Array,Blob,URL,AbortSignal,XMLHttpRequest:XHR,localStorage:{getItem(){return null;},setItem(){},removeItem(){}},setInterval(){},clearInterval(){},setTimeout(){},confirm:()=>true,window:{addEventListener(){}},fetch:async(path,options)=>{requests.push({path,options});return{ok:true,status:200,json:async()=>data};}});
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
  data={...data,items:[{id:receipt,available:true,label:'Phone video.mov',size:100}]};await send();assert.equal(uploads.length,3,'server receipt prevents resending committed file');
  reset();choose();data.freeBytes=20;await send();assert.equal(uploads.length,0);assert.match(status(),/Not enough TV storage/);
  reset();choose();await send();run('window.nookMedia.disconnect();');assert.match(status(),/Pair again/);assert.equal(elements.get('video-file').value,'');
  console.log('PASS: binary upload auth, limits, progress, cancel, retry receipts and disconnect privacy');
})().catch(e=>{console.error(e);process.exitCode=1;});
