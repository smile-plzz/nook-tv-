package com.ismail.nooktv;
import android.content.Context;
import android.util.AtomicFile;
import java.io.*;
import java.nio.charset.StandardCharsets;
import org.json.*;

public final class ViewingStore {
    private final AtomicFile file;
    public final ViewingLedger ledger;
    private boolean damaged;
    public ViewingStore(Context context) {
        file=new AtomicFile(new File(context.getFilesDir(),"viewing-v1.json")); ViewingLedger loaded;
        try { loaded=new ViewingLedger(new JSONObject(new String(file.readFully(),StandardCharsets.UTF_8))); }
        catch(Exception e) { damaged=!(e instanceof FileNotFoundException); try { loaded=new ViewingLedger(null); } catch(JSONException impossible) { throw new AssertionError(impossible); } }
        ledger=loaded;
        android.content.SharedPreferences old=context.getSharedPreferences("native-progress",Context.MODE_PRIVATE);
        if(!context.getSharedPreferences("nook",0).getBoolean("historyMigrated",false)) try {
            String url=old.getString("url",""); if(!url.isEmpty()) { ledger.opened("direct:"+url,old.getString("label","Video"),url,"native"); ledger.observe("direct:"+url,old.getInt("position",0),old.getInt("duration",0),false,false); }
            ledger.pause(old.getBoolean("paused",false)); persist(); context.getSharedPreferences("nook",0).edit().putBoolean("historyMigrated",true).apply();
        } catch(Exception ignored) {}
    }
    public synchronized void persist() throws Exception { if(damaged) throw new IOException("Unreadable viewing state was preserved"); byte[] bytes=ledger.snapshot().toString().getBytes(StandardCharsets.UTF_8); FileOutputStream out=file.startWrite(); try { out.write(bytes); file.finishWrite(out); } catch(Exception e) { file.failWrite(out); throw e; } }
}
