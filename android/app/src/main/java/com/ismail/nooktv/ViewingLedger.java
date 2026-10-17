package com.ismail.nooktv;
import org.json.*;

/** Unobservable launches and measured native playback never share invented progress. */
public final class ViewingLedger {
    private final JSONObject data;
    public ViewingLedger(JSONObject persisted) throws JSONException {
        data=persisted==null?new JSONObject().put("schemaVersion",1).put("paused",false).put("entries",new JSONArray()):new JSONObject(persisted.toString());
        if(data.getInt("schemaVersion")!=1||data.getJSONArray("entries").length()>100) throw new IllegalArgumentException("Invalid viewing history");
    }
    public synchronized JSONObject snapshot() throws JSONException { return new JSONObject(data.toString()); }
    public synchronized boolean paused() { return data.optBoolean("paused"); }
    public synchronized void pause(boolean pause) throws JSONException { data.put("paused",pause); }
    public synchronized void clear() throws JSONException { data.put("entries",new JSONArray()); }
    public synchronized JSONObject find(String id) throws JSONException { JSONArray a=data.getJSONArray("entries"); for(int i=0;i<a.length();i++) if(a.getJSONObject(i).getString("id").equals(id)) return new JSONObject(a.getJSONObject(i).toString()); return null; }
    public synchronized void opened(String id,String label,String url,String route) throws JSONException { if(paused()) return; Targets.https(url); JSONObject e=find(id); if(e==null) e=new JSONObject().put("id",id).put("position",0).put("duration",0).put("status","opened"); e.put("label",label).put("url",url).put("route",route).put("updatedAt",System.currentTimeMillis()); put(e); }
    public synchronized void observe(String id,long position,long duration,boolean live,boolean ended) throws JSONException {
        if(paused()||position<0) return; JSONObject e=find(id); if(e==null||!e.optString("route").equals("native")) return;
        if(live||duration<=0) e.put("position",0).put("duration",0).put("status","live");
        else { long p=Math.min(position,duration); e.put("position",p).put("duration",duration).put("status",ended||p>=(long)(duration*.95)?"watched":"progress"); }
        e.put("updatedAt",System.currentTimeMillis()); put(e);
    }
    public synchronized void failed(String id) throws JSONException { if(paused()) return; JSONObject e=find(id); if(e!=null) { if(!e.optString("status").equals("progress")) e.put("status","failed"); put(e); } }
    public synchronized void mark(String id,boolean watched) throws JSONException { JSONObject e=find(id); if(e!=null) { e.put("status",watched?"watched":"opened").put("position",0); if(!watched) e.put("duration",0); put(e); } }
    public synchronized void remove(String id) throws JSONException { JSONArray a=data.getJSONArray("entries"),next=new JSONArray(); for(int i=0;i<a.length();i++) if(!a.getJSONObject(i).getString("id").equals(id)) next.put(a.getJSONObject(i)); data.put("entries",next); }
    private void put(JSONObject e) throws JSONException { remove(e.getString("id")); JSONArray old=data.getJSONArray("entries"),next=new JSONArray(); next.put(e); for(int i=0;i<Math.min(old.length(),99);i++) next.put(old.getJSONObject(i)); data.put("entries",next); }
    public static boolean resumable(JSONObject e) { return e.optString("route").equals("native")&&e.optString("status").equals("progress")&&e.optLong("duration")>0&&e.optLong("position")>0; }
}
