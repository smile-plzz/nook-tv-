package com.ismail.nooktv;
import android.content.Context;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/** OMDb only. Bounded disk cache; network calls execute on the caller's IO executor. */
public final class CatalogClient {
    private final Context context;
    private final File cache;
    public CatalogClient(Context context) { this.context=context; cache=new File(context.getCacheDir(),"metadata"); cache.mkdirs(); }
    public JSONObject search(String q,int page) throws Exception { if(q.trim().length()<2||q.length()>100||page<1||page>20) throw new IllegalArgumentException("Use 2-100 characters and a valid page."); return request("s="+encode(q.trim())+"&page="+page); }
    public JSONObject detail(String id,int season) throws Exception { if(!id.matches("tt[0-9]{7,10}")||season<0||season>100) throw new IllegalArgumentException("Invalid title or season"); return request("i="+id+"&plot=short"+(season>0?"&Season="+season:"")); }
    private JSONObject request(String query) throws Exception {
        String key=context.getSharedPreferences("secrets",0).getString("omdb","");
        if(!key.matches("[a-zA-Z0-9]{4,64}")) throw new IllegalStateException("Add your OMDb key in TV Settings to search movies and series.");
        String name=Integer.toHexString(query.hashCode())+".json"; File entry=new File(cache,name); JSONObject cached=null;
        if(entry.exists()) try { cached=new JSONObject(new String(java.nio.file.Files.readAllBytes(entry.toPath()),StandardCharsets.UTF_8)); if(cached.getString("query").equals(query)&&System.currentTimeMillis()-entry.lastModified()<6*60*60*1000) return cached.getJSONObject("body"); } catch(Exception ignored) {}
        HttpURLConnection connection=null;
        try {
            connection=(HttpURLConnection)new URL("https://www.omdbapi.com/?apikey="+encode(key)+"&r=json&"+query).openConnection(); connection.setConnectTimeout(8000); connection.setReadTimeout(10000); connection.setInstanceFollowRedirects(false);
            if(connection.getResponseCode()!=200) throw new IOException("Metadata service unavailable or quota reached.");
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(InputStream in=connection.getInputStream()) { byte[] buffer=new byte[4096]; int n; while((n=in.read(buffer))!=-1) { if(bytes.size()+n>512000) throw new IOException("Metadata response too large"); bytes.write(buffer,0,n); } }
            JSONObject body=new JSONObject(new String(bytes.toByteArray(),StandardCharsets.UTF_8)); if(body.optString("Response").equals("False")) throw new IOException(body.optString("Error","No matching titles."));
            java.nio.file.Files.write(entry.toPath(),new JSONObject().put("query",query).put("body",body).toString().getBytes(StandardCharsets.UTF_8)); trim(); return body;
        } catch(IOException e) {
            if(cached!=null&&cached.optString("query").equals(query)) return cached.getJSONObject("body").put("nookStale",true);
            throw new IOException("Could not load metadata. Check connection, API key or quota.");
        } finally { if(connection!=null) connection.disconnect(); }
    }
    private void trim() { File[] entries=cache.listFiles(); if(entries==null) return; java.util.Arrays.sort(entries,java.util.Comparator.comparingLong(File::lastModified)); long total=0; for(File f:entries) total+=f.length(); for(int i=0;i<entries.length&&(entries.length-i>150||total>8*1024*1024);i++) { long size=entries[i].length(); if(entries[i].delete()) total-=size; } }
    public void clear() { File[] files=cache.listFiles(); if(files!=null) for(File f:files) f.delete(); }
    private static String encode(String s) throws Exception { return URLEncoder.encode(s,"UTF-8"); }
}
