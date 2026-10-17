package com.ismail.nooktv;
import android.content.Context;
import android.util.AtomicFile;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** Config, receipts and recovery snapshot commit in one atomic envelope. */
public final class DashboardStore {
    private final AtomicFile file;
    private JSONObject state,receipts=new JSONObject(),recovery;
    private boolean damaged;
    public DashboardStore(Context context) {
        file=new AtomicFile(new File(context.getFilesDir(),"dashboard-v1.json"));
        try { JSONObject disk=new JSONObject(new String(file.readFully(),StandardCharsets.UTF_8));
            if(disk.has("config")) { state=DashboardDocument.migrate(disk.getJSONObject("config")); receipts=disk.optJSONObject("receipts"); if(receipts==null) receipts=new JSONObject(); recovery=disk.optJSONObject("recovery"); }
            else state=DashboardDocument.migrate(disk);
        } catch(FileNotFoundException e) { fresh(); } catch(Exception e) { damaged=true; fresh(); }
    }
    private void fresh() { try { state=DashboardDocument.defaults(); } catch(JSONException e) { throw new AssertionError(e); } }
    public synchronized boolean damaged() { return damaged; }
    public synchronized JSONObject read() throws JSONException { return new JSONObject(state.toString()); }
    public synchronized JSONObject save(JSONObject input,long expected,String mutation) throws Exception { return apply(input,expected,mutation,false); }
    public synchronized JSONObject restore(JSONObject input,long expected,String mutation) throws Exception { return apply(DashboardDocument.migrate(input),expected,mutation,true); }
    private JSONObject apply(JSONObject input,long expected,String mutation,boolean importing) throws Exception {
        if(damaged) throw new IllegalStateException("Saved dashboard could not be read; it has not been overwritten.");
        if(!mutation.matches("[a-zA-Z0-9-]{16,80}")) throw new IllegalArgumentException("Invalid mutation ID");
        String digest=digest(expected+":"+input.toString()+":"+importing);
        if(receipts.has(mutation)) { if(!receipts.getJSONObject(mutation).getString("digest").equals(digest)) throw new IllegalArgumentException("Reused mutation ID"); return read(); }
        if(expected!=state.getLong("revision")) throw new java.util.ConcurrentModificationException(); DashboardDocument.validate(input);
        JSONObject next=new JSONObject(input.toString()).put("revision",expected+1),newReceipts=new JSONObject(receipts.toString()); newReceipts.put(mutation,new JSONObject().put("digest",digest).put("revision",expected+1));
        while(newReceipts.length()>128) newReceipts.remove(newReceipts.keys().next()); JSONObject snapshot=importing?state:recovery;
        JSONObject envelope=new JSONObject().put("config",next).put("receipts",newReceipts); if(snapshot!=null) envelope.put("recovery",snapshot);
        FileOutputStream out=file.startWrite(); try { out.write(envelope.toString().getBytes(StandardCharsets.UTF_8)); file.finishWrite(out); } catch(Exception e) { file.failWrite(out); throw e; }
        state=next; receipts=newReceipts; recovery=snapshot; return read();
    }
    public synchronized boolean canRecover() { return recovery!=null; }
    public synchronized void undoImport() throws Exception { if(recovery==null) throw new IllegalStateException(); restore(recovery,state.getLong("revision"),java.util.UUID.randomUUID().toString()); }
    private static String digest(String s) throws Exception { byte[] bytes=java.security.MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)); StringBuilder r=new StringBuilder(); for(byte b:bytes) r.append(String.format(java.util.Locale.ROOT,"%02x",b&255)); return r.toString(); }
}
