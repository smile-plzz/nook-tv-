package com.ismail.nooktv;
import org.json.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

/** Shared import, browser and TV configuration boundary. */
public final class DashboardDocument {
    public static final int MAX_BYTES=900000;
    public static final String[] ROWS={"resume","apps","saved","collections","recent","samples"};
    private DashboardDocument() {}
    public static JSONObject defaults() throws JSONException {
        JSONObject s=new JSONObject().put("schemaVersion",2).put("revision",0).put("title","Your space. Your screen.").put("links",new JSONArray()).put("apps",new JSONArray()).put("collections",new JSONArray()).put("background","").put("accent","#9ce5c5");
        JSONArray rows=new JSONArray(); for(String id:ROWS) rows.put(new JSONObject().put("id",id).put("visible",true)); return s.put("sections",rows);
    }
    public static JSONObject migrate(JSONObject old) throws JSONException {
        JSONObject s=new JSONObject(old.toString());
        if(s.optInt("schemaVersion")==1) { if(s.length()!=5) throw new IllegalArgumentException("Unknown v1 fields"); JSONObject d=defaults(); for(String f:new String[]{"collections","background","accent","sections"}) s.put(f,d.get(f)); s.put("schemaVersion",2); }
        // Keep old saved links and collection identities without reviving a retired route.
        JSONArray links=s.optJSONArray("links");
        if(links!=null) for(int i=0;i<links.length();i++) {
            JSONObject link=links.optJSONObject(i);
            if(link!=null&&"catalog".equals(link.optString("route"))) link.put("route","browser");
        }
        validate(s); return s;
    }
    private static void require(boolean yes,String m) { if(!yes) throw new IllegalArgumentException(m); }
    private static String text(JSONObject o,String key,int max) throws JSONException { Object v=o.get(key); require(v instanceof String,"Expected text: "+key); String s=(String)v; require(s.length()<=max,"Text too long"); return s; }
    public static void validate(JSONObject s) throws JSONException {
        require(s.length()==9&&Integer.valueOf(2).equals(s.get("schemaVersion")),"Unsupported dashboard schema");
        require(s.get("revision") instanceof Number&&s.getDouble("revision")==s.getLong("revision")&&s.getLong("revision")>=0,"Invalid revision");
        require(s.toString().getBytes(StandardCharsets.UTF_8).length<=MAX_BYTES,"Dashboard too large");
        require(!text(s,"title",80).trim().isEmpty(),"Welcome line required"); require(text(s,"accent",7).matches("#[0-9a-fA-F]{6}"),"Invalid accent");
        String bg=text(s,"background",700000); require(bg.isEmpty()||bg.matches("data:image/jpeg;base64,[A-Za-z0-9+/=]+"),"Expected a bounded JPEG background");
        JSONArray links=s.getJSONArray("links"),apps=s.getJSONArray("apps"),groups=s.getJSONArray("collections"),rows=s.getJSONArray("sections");
        require(links.length()<=200&&apps.length()<=80&&groups.length()<=20,"Dashboard limit exceeded"); Set<String> ids=new HashSet<>(),refs=new HashSet<>();
        for(int i=0;i<links.length();i++) {
            JSONObject l=links.getJSONObject(i); String id=text(l,"id",80),route=text(l,"route",20),url=text(l,"url",2048);
            require(l.length()==4&&id.matches("[a-zA-Z0-9-]{1,80}")&&ids.add(id)&&!text(l,"label",100).trim().isEmpty(),"Invalid or duplicate link");
            require(Arrays.asList("native","web","browser","youtube").contains(route),"Invalid route"); Targets.https(url);
            if(route.equals("youtube")) require(Targets.youtube(url),"Invalid YouTube URL"); refs.add(id);
        }
        Set<String> pinned=new HashSet<>(); for(int i=0;i<apps.length();i++) { String c=apps.getString(i); require(c.matches("[a-zA-Z0-9_.]+/[a-zA-Z0-9_.$]+")&&pinned.add(c),"Invalid or duplicate app"); refs.add("app:"+c); }
        Set<String> usedGroups=new HashSet<>(); int count=0;
        for(int i=0;i<groups.length();i++) { JSONObject c=groups.getJSONObject(i); String id=text(c,"id",80); require(c.length()==3&&id.matches("[a-zA-Z0-9-]{1,80}")&&usedGroups.add(id)&&!text(c,"label",80).trim().isEmpty(),"Invalid collection"); Set<String> used=new HashSet<>(); JSONArray a=c.getJSONArray("items"); for(int j=0;j<a.length();j++) { String ref=a.getString(j); require(refs.contains(ref)&&used.add(ref),"Missing or duplicate collection reference"); count++; } }
        require(count<=400,"Too many memberships"); Set<String> rowIds=new HashSet<>(); require(rows.length()==ROWS.length,"Missing sections");
        for(int i=0;i<rows.length();i++) { JSONObject r=rows.getJSONObject(i); String id=r.getString("id"); require(r.length()==2&&Arrays.asList(ROWS).contains(id)&&rowIds.add(id)&&r.get("visible") instanceof Boolean,"Invalid section"); }
    }
    public static void removeReference(JSONObject config,String ref) throws JSONException { JSONArray groups=config.getJSONArray("collections"); for(int i=0;i<groups.length();i++) { JSONObject g=groups.getJSONObject(i); JSONArray next=new JSONArray(),old=g.getJSONArray("items"); for(int j=0;j<old.length();j++) if(!old.getString(j).equals(ref)) next.put(old.getString(j)); g.put("items",next); } }
}
