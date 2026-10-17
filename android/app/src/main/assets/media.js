'use strict';
// Binary file upload is separate from dashboard drafts and never stored in localStorage.
(() => {
  let transfer=null, selected=null, transferId=null, preparing=false;
  const say=text=>{$('media-status').textContent=text;}, bytes=n=>`${(n/1048576).toFixed(1)} MiB`;
  async function refresh(){
    if(!session)return;
    try{const data=(await request('/api/media')).data;$('media-limits').textContent=`${bytes(data.usedBytes)} used of ${bytes(data.quotaBytes)} · up to 50 videos · ${bytes(data.maxFileBytes)} per video. Keep Nook open on TV.`;
      const list=$('media-list');list.replaceChildren();for(const item of data.items)list.append(element('p',`${item.label} · ${bytes(item.size)} · ${item.available?'Saved on TV':'Unavailable'}`));
      if(data.damaged)say('The TV media index could not be read. New transfers are blocked; existing files were preserved.');
      return data;
    }catch(error){say(error.status===401?'Pair this browser again to send a video.':'Cannot reach TV storage. Reopen Nook and refresh.');}
  }
  function idle(){transfer=null;$('send-video').disabled=false;$('video-file').disabled=false;$('cancel-video').disabled=true;}
  $('video-file').onchange=()=>{selected=$('video-file').files[0]||null;transferId=selected?id():null;$('video-progress').value=0;say(selected?`${selected.name} · ${bytes(selected.size)}. Send keeps a copy on your TV.`:'Choose an MP4 or MOV video.');};
  $('send-video').onclick=async()=>{
    if(transfer||preparing)return;preparing=true;try{if(!session){say('Pair this browser on TV first.');return;}if(!selected){say('Choose an MP4 or MOV video first.');return;}
    const credentials=session,file=selected;
    if(selected.size<12||selected.size>512*1048576){say('Choose a video up to 512 MiB. Larger files are not supported in this preview.');return;}
    const state=await refresh();if(!state||session!==credentials||selected!==file)return;
    const prior=state.items.find(item=>item.id===transferId);
    if(prior){say(prior.available?'Saved on TV. Open Shared & Recent to play.':'The saved copy is unavailable; choose the video again.');return;}
    if(state.damaged||state.receiving){say('TV storage is unavailable or another transfer is running. Try again after it finishes.');return;}
    if(selected.size>Math.min(state.quotaBytes-state.usedBytes,state.freeBytes)||state.items.length>=50){say('Not enough TV storage. Delete a saved copy in TV Settings or choose a smaller video.');return;}
    if(!confirm(`Send ${selected.name} (${bytes(selected.size)}) and keep a copy on this TV? This is local HTTP on your home network, not an encrypted transfer. Playback will not start automatically.`))return;
    const xhr=new XMLHttpRequest();transfer=xhr;$('send-video').disabled=true;$('video-file').disabled=true;$('cancel-video').disabled=false;$('video-progress').value=0;
    xhr.open('POST','/api/media/upload');xhr.timeout=30*60*1000;
    xhr.setRequestHeader('X-Nook-Client','editor-v1');xhr.setRequestHeader('Authorization',`Bearer ${session.token}`);xhr.setRequestHeader('X-Nook-CSRF',session.csrf);xhr.setRequestHeader('Content-Type','application/octet-stream');xhr.setRequestHeader('X-Nook-Filename',encodeURIComponent(selected.name));xhr.setRequestHeader('X-Nook-Upload-Id',transferId);
    xhr.upload.onprogress=e=>{if(e.lengthComputable){const percent=Math.min(100,Math.round(e.loaded/e.total*100));$('video-progress').value=percent;say(percent===100?'Upload sent. TV is checking and saving the video…':`Sending ${percent}% · Keep Nook open on TV.`);}};
    xhr.onload=()=>{let result;try{result=JSON.parse(xhr.responseText);}catch(_){}idle();if(xhr.status===200&&result?.item){$('video-progress').value=100;say('Saved on TV. Open Shared & Recent to play; your phone can disconnect.');refresh();}else say(result?.error||'Transfer was not confirmed. Refresh the saved list before retrying.');};
    xhr.onerror=()=>{idle();say('Connection lost. Refresh the saved list to check whether saving finished, then retry. Incomplete copies are removed.');};
    xhr.ontimeout=()=>{idle();say('Transfer timed out. Refresh the saved list before retrying.');};
    xhr.onabort=()=>{idle();say('Transfer cancelled. If saving had already completed, the copy stays listed on TV; refresh to check.');};
    try{xhr.send(selected);say('Sending video… Keep Nook open on TV.');}catch(_){idle();say('Could not start the transfer. Choose the video again.');}
    }finally{preparing=false;}
  };
  $('cancel-video').onclick=()=>transfer?.abort();$('refresh-media').onclick=refresh;
  window.addEventListener('beforeunload',e=>{if(transfer){e.preventDefault();e.returnValue='';}});
  window.nookMedia={refresh,disconnect(){if(transfer)transfer.abort();selected=null;transferId=null;$('video-file').value='';$('media-list').replaceChildren();say('Pair again to send media.');}};
})();
