package com.ismail.nooktv;
import android.content.Context;
import java.io.*;
import java.util.*;
import org.json.*;

/** Real Android AtomicFile/media extractor checks using a generated, permitted video fixture. */
final class ReceivedMediaChecks {
    static final String ID="0123456789abcdef0123456789abcdef";
    private static int count;
    private static void check(boolean pass,String message){count++;if(!pass)throw new AssertionError(message);}
    static int run(Context context,byte[] video)throws Exception{
        count=0;ReceivedMediaStore store=new ReceivedMediaStore(context);
        JSONObject item=store.receive(new ByteArrayInputStream(video),video.length,"Phone sample.mp4",ID,()->true);
        check(item.getString("label").equals("Phone sample.mp4")&&store.playable(ID).length()==video.length,"complete transfer retained with exact size");
        check(new ReceivedMediaStore(context).playable(ID).isFile(),"retained file and metadata survive store recreation");
        store.receive(new ByteArrayInputStream(new byte[0]),video.length,"Phone sample.mp4",ID,()->true);
        check(store.snapshot().getJSONArray("items").length()==1,"same transfer receipt avoids duplicate files");
        boolean rejected=false;try{store.receive(new ByteArrayInputStream(video),video.length,"Different.mp4",ID,()->true);}catch(EditorServer.RequestFailure e){rejected=e.status==409;}check(rejected,"transfer ID cannot silently change file metadata");
        String incomplete="11111111111111111111111111111111";rejected=false;try{store.receive(new ByteArrayInputStream(Arrays.copyOf(video,16)),video.length,"Interrupted.mp4",incomplete,()->true);}catch(IOException e){rejected=true;}check(rejected&&store.find(incomplete)==null&&!new File(context.getFilesDir(),"received-media/"+incomplete+".part").exists(),"interrupted transfer removes partial and publishes nothing");
        String invalid="22222222222222222222222222222222";rejected=false;try{store.receive(new ByteArrayInputStream(new byte[32]),32,"Invalid.mp4",invalid,()->true);}catch(EditorServer.RequestFailure e){rejected=e.status==415;}check(rejected&&store.find(invalid)==null,"invalid container rejected before finalization");
        rejected=false;try{store.receive(new ByteArrayInputStream(video),ReceivedMediaStore.MAX_FILE+1,"Large.mp4",invalid,()->true);}catch(EditorServer.RequestFailure e){rejected=e.status==413;}check(rejected,"oversized declaration rejected without buffering body");
        rejected=false;try{store.receive(new ByteArrayInputStream(video),video.length,"Denied.mp4",invalid,()->false);}catch(EditorServer.RequestFailure e){rejected=e.status==401;}check(rejected&&store.find(invalid)==null,"revoked permission prevents file publication");
        rejected=false;try{store.playable("../../outside");}catch(IllegalArgumentException e){rejected=true;}check(rejected,"external paths cannot become local media identities");
        ViewingLedger ledger=new ViewingLedger(null);ledger.openedLocal(ID,"Phone sample.mp4");ledger.observe("local:"+ID,1000,3000,false,false);check(ViewingLedger.resumable(ledger.find("local:"+ID)),"local video supports measured resume");ledger.clear();check(store.playable(ID).length()==video.length,"clearing history preserves retained copy");
        JSONObject updated=store.update(ID,new JSONObject().put("label","Renamed sample.mp4").put("pinned",true).put("collection","Family").put("hidden",false));
        check(updated.getString("label").equals("Renamed sample.mp4")&&updated.getBoolean("pinned")&&updated.getString("collection").equals("Family"),"store update applies rename, pin and collection");
        check(store.find(ID).getBoolean("pinned"),"re-read item confirms pin persistence");
        File thumb=store.thumbnail(ID);check(thumb==null||thumb.getName().endsWith(".thumb"),"thumbnail accessor returns null or valid thumb file");
        store.update(ID,new JSONObject().put("label","Phone sample.mp4").put("pinned",false).put("collection",""));
        String deleted="33333333333333333333333333333333";store.receive(new ByteArrayInputStream(video),video.length,"Delete.mp4",deleted,()->true);store.delete(deleted);check(store.find(deleted)==null&&!new File(context.getFilesDir(),"received-media/"+deleted+".media").exists()&&!new File(context.getFilesDir(),"received-media/"+deleted+".thumb").exists(),"explicit deletion removes file, thumbnail and index entry");
        File index=new File(context.getFilesDir(),"received-media-v1.json");byte[] saved=read(index);try(FileOutputStream out=new FileOutputStream(index)){out.write("broken".getBytes("UTF-8"));}
        ReceivedMediaStore damaged=new ReceivedMediaStore(context);rejected=false;try{damaged.receive(new ByteArrayInputStream(video),video.length,"Blocked.mp4",deleted,()->true);}catch(EditorServer.RequestFailure e){rejected=e.status==503;}check(rejected&&Arrays.equals(read(index),"broken".getBytes("UTF-8"))&&new File(context.getFilesDir(),"received-media/"+ID+".media").exists(),"damaged index blocks writes and preserves file bytes");
        try(FileOutputStream out=new FileOutputStream(index)){out.write(saved);out.getFD().sync();}return count;
    }
    static byte[] read(File file)throws IOException{try(InputStream in=new FileInputStream(file);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);return out.toByteArray();}}
}
