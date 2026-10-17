'use strict';
const $ = id => document.getElementById(id);
let session = null, config = null, dirty = false, pendingMutation = null, saving = false, ticket = null, poll = null;
const draftKey = 'nook-dashboard-draft-v1';
function status(message) { $('status').textContent = message; }
async function request(path, method = 'GET', body) {
  const headers = {'X-Nook-Client':'editor-v1'};
  if (session) { headers.Authorization = `Bearer ${session.token}`; headers['X-Nook-CSRF'] = session.csrf; }
  if (body) headers['Content-Type'] = 'application/json';
  const response = await fetch(path, {method, headers, body: body ? JSON.stringify(body) : undefined, signal: AbortSignal.timeout(8000), cache:'no-store'});
  const data = await response.json();
  if (!response.ok) { const e = new Error(data.error || 'TV request failed.'); e.status = response.status; throw e; }
  return {data, status:response.status};
}
function draft() {
  if (!config) return;
  config.title = $('title').value;
  config.links = [...$('links').children].map(row => ({id:row.dataset.id,label:row.querySelector('[data-field="label"]').value,url:row.querySelector('[data-field="url"]').value,route:row.querySelector('[data-field="route"]').value}));
  dirty = true; pendingMutation = null;
  try { localStorage.setItem(draftKey, JSON.stringify(config)); } catch (_) { /* Storage can be disabled. Editing still works. */ }
  status('Draft changes • Not yet saved to TV.');
}
function button(label, click) { const b=document.createElement('button'); b.type='button'; b.className='secondary'; b.textContent=label; b.onclick=click; return b; }
function render() {
  $('title').value = config.title; $('revision').textContent = `TV revision ${config.revision}`; $('links').replaceChildren();
  config.links.forEach((link,index) => {
    const row=document.createElement('div'); row.className='link'; row.dataset.id=link.id;
    const fields=document.createElement('div'); fields.className='fields';
    for (const [field,label] of [['label','Tile label'],['url','HTTPS URL'],['route','Open using']]) {
      const holder=document.createElement('label'); holder.textContent=label;
      const input=document.createElement(field==='route'?'select':'input'); input.dataset.field=field;
      if(field==='route') for(const [value,label] of [['browser','External browser'],['youtube','YouTube app'],['web','Web player'],['native','Native media']]) { const o=document.createElement('option'); o.value=value; o.textContent=label; input.append(o); }
      else { input.maxLength=field==='url'?2048:100; input.required=true; if(field==='url') input.type='url'; }
      input.value=link[field]; input.addEventListener('input',draft); holder.append(input); fields.append(holder);
    }
    row.append(fields); const actions=document.createElement('div'); actions.className='toolbar';
    actions.append(button('Move up',()=>{ draft(); if(index>0) [config.links[index-1],config.links[index]]=[config.links[index],config.links[index-1]]; render(); draft(); }),button('Move down',()=>{ draft(); if(index<config.links.length-1) [config.links[index+1],config.links[index]]=[config.links[index],config.links[index+1]]; render(); draft(); }),button('Remove',()=>{ draft(); config.links.splice(index,1); render(); draft(); })); row.append(actions); $('links').append(row);
  });
  $('pair').hidden=true; $('editor').hidden=false;
}
async function loadLatest(offerDraft = false) {
  const latest=(await request('/api/config')).data;
  let saved=null; try { saved=JSON.parse(localStorage.getItem(draftKey)); } catch (_) {}
  config=latest; dirty=false; pendingMutation=null;
  if(offerDraft && saved && Array.isArray(saved.links) && confirm('Restore the unsaved dashboard draft in this browser? The current TV revision will still be checked before saving.')) { config=saved; dirty=true; }
  render(); status(dirty?'Draft restored. Review it; if TV data changed, save will report a conflict.':`Connected • Revision ${config.revision}.`);
}
$('pair-form').onsubmit=async event=> {
  event.preventDefault(); clearInterval(poll); session=null;
  try {
    ticket=(await request('/api/pair','POST',{code:$('code').value,name:$('device-name').value})).data.ticket; status('Check your TV and approve this browser.');
    const until=Date.now()+120000;
    poll=setInterval(async()=>{ if(Date.now()>until) { clearInterval(poll); status('Pairing expired. Generate a new code on TV.'); return; } try { const result=await request('/api/claim','POST',{ticket}); if(result.status===200) { clearInterval(poll); session=result.data; ticket=null; await loadLatest(true); } } catch(e) { clearInterval(poll); status('Disconnected. Open Nook on TV and pair again.'); } },2000);
  } catch(e) { status(e.message || 'Cannot reach the TV. Open Nook and check your home network.'); }
};
$('title').addEventListener('input',draft);
$('add').onclick=()=>{ draft(); if(config.links.length>=40) { status('M1 supports up to 40 saved links.'); return; } config.links.push({id:cryptoId(),label:'',url:'https://',route:'browser'}); render(); draft(); };
function cryptoId() { const b=new Uint8Array(16); crypto.getRandomValues(b); return [...b].map(x=>x.toString(16).padStart(2,'0')).join(''); }
$('save').onclick=async()=> {
  if(saving) return;
  // An uncertain save keeps its mutation ID and exact payload for an explicit retry.
  if(!pendingMutation) { draft(); pendingMutation={mutationId:cryptoId(),expectedRevision:config.revision,config:JSON.parse(JSON.stringify(config))}; }
  if(!config.title.trim() || config.links.some(l=>!l.label.trim() || !/^https:\/\//i.test(l.url))) { status('Enter a welcome line, tile labels and HTTPS URLs.'); pendingMutation=null; return; }
  saving=true; $('save').disabled=true;
  try { const result=(await request('/api/config','POST',pendingMutation)).data; config=result; dirty=false; pendingMutation=null; localStorage.removeItem(draftKey); render(); status(`Saved to TV • Revision ${config.revision}.`); }
  catch(e) { status(e.status===409?'Conflict: your TV changed elsewhere. Your draft is retained. Export it, then reload latest and reapply changes.':e.status===401?'Session revoked or TV restarted. Pair again; your draft is retained.':'Disconnected or save failed. Your draft is retained. Reopen Nook; Reload latest checks whether the TV received your save.'); if(e.status===409) pendingMutation=null; }
  finally { saving=false; $('save').disabled=false; }
};
$('reload').onclick=async()=>{ if(dirty&&!confirm('Reload current TV data? Export your draft first if you want to keep it.')) return; try { await loadLatest(); } catch(e) { status('Disconnected. Reopen Nook on TV. Your draft is retained.'); } };
$('export').onclick=()=>{ draft(); const blob=new Blob([JSON.stringify(config,null,2)],{type:'application/json'}); const a=document.createElement('a'); a.href=URL.createObjectURL(blob); a.download='nook-dashboard-v1.json'; a.click(); setTimeout(()=>URL.revokeObjectURL(a.href),1000); status('Exported this dashboard draft. No pairing credentials or viewing history included.'); };
$('disconnect').onclick=()=>{ session=null; config=null; $('pair').hidden=false; $('editor').hidden=true; status('Credentials forgotten in this tab. Revoke browsers on TV to invalidate existing sessions.'); };
window.addEventListener('beforeunload',e=>{ if(dirty) { e.preventDefault(); e.returnValue=''; } });
