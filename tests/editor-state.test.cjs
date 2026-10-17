'use strict';
const assert=require('node:assert/strict'),fs=require('node:fs'),vm=require('node:vm'),crypto=require('node:crypto');
class Element {
  constructor(){this.children=[];this.value='';this.files=[];this.hidden=false;this.dataset={};}
  append(...values){this.children.push(...values);}
  replaceChildren(...values){this.children=[...values];}
  click(){}
}
const elements=new Map(),storage=new Map(),calls=[];
let reply={ok:true,status:200,data:null};
const context=vm.createContext({document:{getElementById:id=>{if(!elements.has(id))elements.set(id,new Element());return elements.get(id);},createElement:()=>new Element()},crypto:crypto.webcrypto,Uint8Array,Blob,URL:{createObjectURL:()=>'',revokeObjectURL(){}},AbortSignal,localStorage:{getItem:k=>storage.get(k)||null,setItem:(k,v)=>storage.set(k,v),removeItem:k=>storage.delete(k)},setInterval:()=>1,clearInterval(){},setTimeout(){},confirm:()=>true,window:{addEventListener(){}},fetch:async(path,options)=>{calls.push({path,options});return {ok:reply.ok,status:reply.status,json:async()=>reply.data};}});
vm.runInContext(fs.readFileSync('android/app/src/main/assets/editor.js','utf8'),context);
const evaluate=code=>vm.runInContext(code,context),base={schemaVersion:2,revision:7,title:'My TV',links:[],apps:[],collections:[],sections:[],accent:'#9ce5c5',background:''};
function reset(){evaluate(`config=${JSON.stringify(base)};session={token:'secret-token',csrf:'secret-csrf'};dirty=false;pending=null;saving=false;render();`);calls.length=0;}
async function click(id){await elements.get(id).onclick();}
(async()=>{
  reset();reply={ok:true,status:200,data:{...base,revision:8}};await click('save');const sent=JSON.parse(calls[0].options.body);assert.equal(sent.expectedRevision,7);assert.equal(sent.config.revision,7);assert.equal(sent.config.token,undefined);assert.equal(calls[0].options.headers.Authorization,'Bearer secret-token');assert.equal(evaluate('dirty'),false);
  reset();evaluate("config.title='My draft';changed();");reply={ok:false,status:409,data:{error:'Conflict'}};await click('save');assert.equal(evaluate('config.title'),'My draft');assert.equal(evaluate('config.revision'),7);assert.equal(evaluate('dirty'),true);assert.equal(evaluate('pending'),null);assert.ok(elements.get('status').textContent.includes('Conflict'));
  reset();evaluate("config.title='Retry draft';changed();");reply={ok:false,status:503,data:{error:'Disconnected'}};await click('save');const first=calls.at(-1).options.body;reply={ok:true,status:200,data:{...base,title:'Retry draft',revision:8}};await click('save');assert.equal(calls.at(-1).options.body,first,'uncertain save uses same mutation and payload');
  reset();evaluate("config.links=[{id:'one',label:'One',url:'https://example.com',route:'browser'}];config.collections=[{id:'group',label:'Learn',items:['one']}];removeRefs('one');");assert.equal(evaluate('config.links.length'),1);assert.equal(evaluate('config.collections[0].items.length'),0);
  reset();const backup={...base,schemaVersion:99};elements.get('import').files=[{size:200,text:async()=>JSON.stringify(backup)}];await elements.get('import').onchange();assert.equal(calls.length,0,'unsupported backup never sent');assert.equal(evaluate('config.revision'),7);
  reset();elements.get('import').files=[{size:200,text:async()=>JSON.stringify({...base,title:'Imported'})}];reply={ok:false,status:409,data:{error:'conflict'}};await elements.get('import').onchange();assert.equal(calls[0].path,'/api/import');assert.equal(JSON.parse(calls[0].options.body).expectedRevision,7);assert.equal(evaluate('config.title'),'My TV');
  reset();evaluate("config.title='Keep this draft';changed();");const draft=JSON.parse(storage.get('nook-dashboard-draft-v2'));assert.equal(draft.token,undefined);assert.equal(draft.title,'Keep this draft');await click('disconnect');assert.equal(evaluate('session'),null);assert.equal(JSON.parse(storage.get('nook-dashboard-draft-v2')).title,'Keep this draft');
  console.log('PASS: editor saves, conflicts, retries, imports, memberships and draft privacy');
})().catch(error=>{console.error(error);process.exitCode=1;});
