package com.ismail.nooktv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.util.AtomicFile;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;

/** Retained user files. Never part of the image cache or dashboard backup. */
public final class ReceivedMediaStore {
    public static final long MAX_FILE=512L*1024*1024,QUOTA=1024L*1024*1024,HEADROOM=128L*1024*1024;
    private final File folder;
    private final AtomicFile index;
    private final ReentrantLock writer=new ReentrantLock();
    private JSONObject state;
    private boolean damaged;

    public ReceivedMediaStore(Context context){
        folder=new File(context.getFilesDir(),"received-media");folder.mkdirs();index=new AtomicFile(new File(context.getFilesDir(),"received-media-v1.json"));
        try{state=new JSONObject(new String(index.readFully(),StandardCharsets.UTF_8));validate(state);}
        catch(FileNotFoundException e){fresh();}catch(Exception e){damaged=true;fresh();}
        // Only our generated partials are disposable; unindexed completed files remain visible in usage.
        if(!damaged){
            File[] files=folder.listFiles();
            if(files!=null)for(File f:files)if(f.isFile()&&f.getName().matches("[a-f0-9]{32}\\.part"))f.delete();
            if(files!=null)for(File f:files)if(f.isFile()&&f.getName().matches("[a-f0-9]{32}\\.trash")){String id=f.getName().substring(0,32);if(find(id)!=null)f.renameTo(path(id,".media"));else f.delete();}
        }
    }

    private void fresh(){try{state=new JSONObject().put("schemaVersion",1).put("items",new JSONArray());}catch(JSONException e){throw new AssertionError(e);}}

    private static void validate(JSONObject s)throws Exception{
        if(s.getInt("schemaVersion")!=1||s.getJSONArray("items").length()>50)throw new IOException();
        Set<String> ids=new HashSet<>();
        JSONArray a=s.getJSONArray("items");
        for(int i=0;i<a.length();i++){
            JSONObject e=a.getJSONObject(i);
            if(!validId(e.getString("id"))||!ids.add(e.getString("id"))||e.getString("label").length()>120||e.getLong("size")<12||e.getLong("size")>MAX_FILE)throw new IOException();
        }
    }

    public static boolean validId(String id){return Targets.validMediaId(id);}

    private File path(String id,String suffix){if(!validId(id))throw new IllegalArgumentException("Invalid media identity");return new File(folder,id+suffix);}

    public synchronized JSONObject find(String id){
        try{JSONArray items=state.getJSONArray("items");for(int i=0;i<items.length();i++)if(items.getJSONObject(i).getString("id").equals(id))return new JSONObject(items.getJSONObject(i).toString());}catch(JSONException ignored){}
        return null;
    }

    public File playable(String id)throws IOException{
        JSONObject item=find(id);File file=path(id,".media");
        if(item==null||!file.isFile()||file.length()!=item.optLong("size"))throw new FileNotFoundException("The saved copy is unavailable.");
        return file;
    }

    public File thumbnail(String id){
        if(!validId(id))return null;
        File thumb=path(id,".thumb");
        if(thumb.isFile()&&thumb.length()>0)return thumb;
        File media=path(id,".media");
        if(media.isFile()){extractThumbnail(media,thumb);if(thumb.isFile()&&thumb.length()>0)return thumb;}
        return null;
    }

    public byte[] thumbnailBytes(String id){
        File t=thumbnail(id);
        if(t==null||!t.isFile())return null;
        try(FileInputStream in=new FileInputStream(t);ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buf=new byte[4096];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);return out.toByteArray();
        }catch(Exception e){return null;}
    }

    private void extractThumbnail(File videoFile,File thumbFile){
        try{
            MediaMetadataRetriever mmr=new MediaMetadataRetriever();
            try{
                mmr.setDataSource(videoFile.getAbsolutePath());
                Bitmap frame=mmr.getFrameAtTime(1000000,MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                if(frame==null)frame=mmr.getFrameAtTime();
                if(frame!=null){
                    String rot=mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                    int degrees=rot!=null?Integer.parseInt(rot):0;
                    Matrix matrix=new Matrix();
                    if(degrees!=0)matrix.postRotate(degrees);
                    float scale=Math.min(320f/frame.getWidth(),180f/frame.getHeight());
                    matrix.postScale(scale,scale);
                    Bitmap scaled=Bitmap.createBitmap(frame,0,0,frame.getWidth(),frame.getHeight(),matrix,true);
                    try(FileOutputStream fos=new FileOutputStream(thumbFile)){
                        scaled.compress(Bitmap.CompressFormat.JPEG,80,fos);
                        fos.getFD().sync();
                    }
                    if(scaled!=frame)scaled.recycle();
                    frame.recycle();
                }
            }finally{try{mmr.release();}catch(Exception ignored){}}
        }catch(Throwable ignored){
            // Graceful fallback: unreadable codec/frame leaves thumbFile unwritten; UI shows vector symbol
        }
    }

    public synchronized JSONObject snapshot()throws JSONException{
        JSONArray items=new JSONArray(),saved=state.getJSONArray("items");long indexed=0;
        for(int i=0;i<saved.length();i++){
            JSONObject e=new JSONObject(saved.getJSONObject(i).toString());
            File file=path(e.getString("id"),".media");
            File thumb=path(e.getString("id"),".thumb");
            e.put("available",file.isFile()&&file.length()==e.optLong("size"));
            e.put("hasThumbnail",thumb.isFile()&&thumb.length()>0);
            if(!e.has("pinned"))e.put("pinned",false);
            if(!e.has("collection"))e.put("collection","");
            if(!e.has("hidden"))e.put("hidden",false);
            if(file.isFile())indexed+=file.length();
            items.put(e);
        }
        long used=usage();
        return new JSONObject().put("items",items).put("usedBytes",used).put("unindexedBytes",Math.max(0,used-indexed)).put("quotaBytes",QUOTA).put("maxFileBytes",MAX_FILE).put("freeBytes",Math.max(0,folder.getUsableSpace()-HEADROOM)).put("damaged",damaged).put("receiving",writer.isLocked());
    }

    private long usage(){
        long bytes=0;File[] files=folder.listFiles();
        if(files!=null)for(File f:files)if(f.isFile()&&f.getName().matches("[a-f0-9]{32}\\.(media|part|trash|thumb)"))bytes+=f.length();
        return bytes;
    }

    private synchronized void commit(JSONObject next)throws Exception{
        byte[] bytes=next.toString().getBytes(StandardCharsets.UTF_8);FileOutputStream out=index.startWrite();
        try{out.write(bytes);index.finishWrite(out);}catch(Exception e){index.failWrite(out);throw e;}state=next;
    }

    private static EditorServer.RequestFailure failure(int status,String text){return new EditorServer.RequestFailure(status,text);}

    public synchronized JSONObject update(String id,JSONObject patch)throws Exception{
        if(!validId(id))throw failure(400,"Invalid media identity.");
        if(damaged)throw failure(503,"Saved media index is unreadable and has been preserved.");
        JSONArray items=state.getJSONArray("items");
        JSONObject target=null;int targetIdx=-1;
        for(int i=0;i<items.length();i++){
            if(items.getJSONObject(i).getString("id").equals(id)){target=items.getJSONObject(i);targetIdx=i;break;}
        }
        if(target==null)throw failure(404,"Saved video copy not found.");
        JSONObject updated=new JSONObject(target.toString());
        if(patch.has("label")){
            String name=patch.getString("label").replaceAll("[\\p{Cntrl}/\\\\]"," ").trim();
            if(name.isEmpty()||name.length()>120)throw failure(400,"Use a video title of 1-120 characters.");
            updated.put("label",name);
        }
        if(patch.has("pinned")){
            updated.put("pinned",patch.getBoolean("pinned"));
        }
        if(patch.has("collection")){
            String col=patch.optString("collection","").replaceAll("[\\p{Cntrl}/\\\\]"," ").trim();
            if(col.length()>80)throw failure(400,"Collection name must be 80 characters or fewer.");
            updated.put("collection",col);
        }
        if(patch.has("hidden")){
            updated.put("hidden",patch.getBoolean("hidden"));
        }
        JSONObject next=new JSONObject(state.toString());
        JSONArray nextItems=new JSONArray();
        for(int i=0;i<items.length();i++){
            if(i==targetIdx)nextItems.put(updated);
            else nextItems.put(items.getJSONObject(i));
        }
        next.put("items",nextItems);
        commit(next);
        return updated;
    }

    public JSONObject receive(InputStream input,long length,String suppliedName,String id,BooleanSupplier allowed)throws Exception{
        if(!validId(id))throw failure(400,"Invalid transfer identity. Select the video again.");
        String name=suppliedName==null?"":suppliedName.replaceAll("[\\p{Cntrl}/\\\\]"," ").trim();if(name.isEmpty()||name.length()>120)throw failure(400,"Use a video filename of 1-120 characters.");
        if(length<12||length>MAX_FILE)throw failure(413,"Choose an MP4 or MOV video up to 512 MiB.");
        if(!writer.tryLock())throw failure(409,"Another media operation is running. Wait and retry.");
        File part=path(id,".part"),complete=path(id,".media");boolean finalized=false,renamed=false;
        try{
            if(damaged)throw failure(503,"Saved media index is unreadable and has been preserved.");
            if(!allowed.getAsBoolean())throw failure(401,"Transfer permission expired. Pair again.");
            JSONObject existing=find(id);if(existing!=null){if(existing.optLong("size")!=length||!existing.optString("label").equals(name))throw failure(409,"Transfer identity was reused for a different file.");playable(id);return existing;}
            if(complete.exists())throw failure(409,"This transfer has an unindexed copy. Choose the file again or manage storage on TV.");
            synchronized(this){if(state.getJSONArray("items").length()>=50)throw failure(413,"The TV holds 50 saved videos. Delete a saved copy first.");}
            if(usage()+length>QUOTA||folder.getUsableSpace()<length+HEADROOM)throw failure(507,"Not enough TV storage. Delete saved copies or choose a smaller video.");
            byte[] buffer=new byte[65536],header=new byte[12];int headerCount=0;long written=0;
            try(FileOutputStream output=new FileOutputStream(part)){
                while(written<length){if(!allowed.getAsBoolean()||Thread.currentThread().isInterrupted())throw failure(401,"Transfer stopped. Reopen Nook and pair again.");int count=input.read(buffer,0,(int)Math.min(buffer.length,length-written));if(count<0)throw new EOFException("Incomplete transfer");if(count==0)continue;
                    if(headerCount<header.length){int copy=Math.min(count,header.length-headerCount);System.arraycopy(buffer,0,header,headerCount,copy);headerCount+=copy;if(headerCount==12&&!(header[4]=='f'&&header[5]=='t'&&header[6]=='y'&&header[7]=='p'))throw failure(415,"This file is not a supported MP4/MOV video container.");}
                    output.write(buffer,0,count);written+=count;if(written%(1024*1024)<buffer.length&&folder.getUsableSpace()<HEADROOM)throw failure(507,"TV storage became full. The incomplete copy will be removed.");
                }output.getFD().sync();
            }
            long duration=0;MediaMetadataRetriever metadata=new MediaMetadataRetriever();
            try{metadata.setDataSource(part.getAbsolutePath());if(!"yes".equals(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)))throw failure(415,"No readable video track was found. Try another MP4/MOV file.");String value=metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);if(value!=null)duration=Long.parseLong(value);}
            catch(EditorServer.RequestFailure e){throw e;}catch(Exception e){throw failure(415,"The TV could not read this video. Try a compatible MP4/MOV.");}finally{try{metadata.release();}catch(Exception ignored){}}
            if(!allowed.getAsBoolean())throw failure(401,"Transfer permission expired before saving.");
            if(!part.renameTo(complete))throw new IOException("Could not finalize video");renamed=true;
            extractThumbnail(complete,path(id,".thumb"));
            JSONObject item=new JSONObject().put("id",id).put("label",name).put("size",length).put("duration",duration).put("receivedAt",System.currentTimeMillis()).put("origin","browser-transfer").put("pinned",false).put("collection","").put("hidden",false);
            synchronized(this){JSONObject next=new JSONObject(state.toString());JSONArray list=new JSONArray().put(item),previous=next.getJSONArray("items");for(int i=0;i<previous.length();i++)list.put(previous.getJSONObject(i));next.put("items",list);commit(next);}finalized=true;return item;
        }finally{if(!finalized&&renamed)complete.delete();part.delete();writer.unlock();}
    }

    public void delete(String id)throws Exception{
        if(!writer.tryLock())throw failure(409,"Wait for the transfer to finish before deleting.");
        try{if(damaged)throw new IOException("Unreadable index preserved");File file=path(id,".media"),trash=path(id,".trash"),thumb=path(id,".thumb");if(file.exists()&&!file.renameTo(trash))throw new IOException("Could not remove video");
            try{synchronized(this){JSONObject next=new JSONObject(state.toString());JSONArray items=new JSONArray(),old=next.getJSONArray("items");for(int i=0;i<old.length();i++)if(!old.getJSONObject(i).getString("id").equals(id))items.put(old.getJSONObject(i));next.put("items",items);commit(next);}}catch(Exception e){trash.renameTo(file);throw e;}
            trash.delete();thumb.delete();
        }finally{writer.unlock();}
    }

    public void cleanUnindexed()throws Exception{
        if(!writer.tryLock())throw failure(409,"A transfer is running.");
        try{
            if(damaged)throw new IOException("Unreadable index preserved");
            File[] files=folder.listFiles();
            if(files!=null)for(File f:files)if(f.isFile()&&f.getName().matches("[a-f0-9]{32}\\.(media|part|trash|thumb)")&&find(f.getName().substring(0,32))==null)if(!f.delete())throw new IOException("Could not remove incomplete storage");
        }finally{writer.unlock();}
    }
}
