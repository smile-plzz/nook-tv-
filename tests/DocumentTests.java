package com.ismail.nooktv;
import org.json.*;
public final class DocumentTests {
    static int count;
    static void check(boolean pass,String why) { count++; if(!pass) throw new AssertionError(why); }
    interface Edit { void apply(JSONObject s) throws Exception; }
    static void invalid(Edit edit,String why) throws Exception { JSONObject s=DashboardDocument.defaults(); edit.apply(s); boolean rejected=false; try { DashboardDocument.validate(s); } catch(Exception e) { rejected=true; } check(rejected,why); }
    static JSONObject link(String id,String route,String url) throws Exception { return new JSONObject().put("id",id).put("label","বাংলা title").put("route",route).put("url",url); }
    public static void main(String[] args) throws Exception {
        JSONObject base=DashboardDocument.defaults(); DashboardDocument.validate(base); check(base.getJSONArray("sections").length()==6,"default sections");
        JSONObject old=new JSONObject("{\"schemaVersion\":1,\"revision\":7,\"title\":\"My TV\",\"links\":[],\"apps\":[]}"); JSONObject migrated=DashboardDocument.migrate(old); check(migrated.getInt("schemaVersion")==2&&migrated.getLong("revision")==7,"v1 revision retained"); check(!old.has("collections"),"migration does not mutate input");
        invalid(s->s.put("schemaVersion",3),"future schema rejected"); invalid(s->s.put("schemaVersion",2.5),"fractional schema rejected"); invalid(s->s.put("revision",-1),"negative revision"); invalid(s->s.put("revision",1.2),"fractional revision"); invalid(s->s.put("title",12),"strict string typing"); invalid(s->s.put("title",""),"empty title"); invalid(s->s.put("apiKey","secret"),"unknown credential fields"); invalid(s->s.put("accent","red"),"accent format"); invalid(s->s.put("background","data:image/svg+xml;base64,PHN2Zz4="),"active image types rejected");
        invalid(s->s.getJSONArray("links").put(link("a","native","file:///system/secret")),"file URL rejected"); invalid(s->{s.getJSONArray("links").put(link("same","browser","https://example.com"));s.getJSONArray("links").put(link("same","browser","https://example.com/2"));},"duplicate identity rejected");
        invalid(s->s.getJSONArray("links").put(link("a","youtube","https://youtube.com.evil.example/watch?v=1")),"YouTube spoof"); invalid(s->s.getJSONArray("links").put(link("a","catalog","https://example.com/tt1234567")),"catalog identity must be IMDb");
        invalid(s->s.getJSONArray("collections").put(new JSONObject().put("id","group").put("label","Group").put("items",new JSONArray().put("missing"))),"dangling references");
        invalid(s->s.getJSONArray("sections").getJSONObject(1).put("id","resume"),"duplicate sections"); invalid(s->s.getJSONArray("sections").getJSONObject(0).put("visible","true"),"strict visibility boolean");
        JSONObject grouped=DashboardDocument.defaults(); grouped.getJSONArray("links").put(link("title-a","browser","https://example.com")); grouped.getJSONArray("collections").put(new JSONObject().put("id","group").put("label","Learn").put("items",new JSONArray().put("title-a"))); DashboardDocument.validate(grouped); DashboardDocument.removeReference(grouped,"title-a"); check(grouped.getJSONArray("links").length()==1&&grouped.getJSONArray("collections").getJSONObject(0).getJSONArray("items").length()==0,"membership removal preserves saved item");
        check("tt1234567".equals(Targets.imdb("https://www.imdb.com/title/tt1234567/")),"canonical IMDb extraction");
        check(SourceTemplate.build("https://example.com/tv/{imdb}/{season}/{episode}","tt1234567",2,3).equals("https://example.com/tv/tt1234567/2/3"),"episode URL contract");
        boolean rejected=false; try { SourceTemplate.build("https://example.com/tmdb/{tmdb}","tt1234567",0,0); } catch(Exception e) { rejected=true; } check(rejected,"never substitute IMDb into TMDB contract");
        rejected=false; try { SourceTemplate.build("https://example.com/{imdb}","tt1234567",2,3); } catch(Exception e) { rejected=true; } check(rejected,"series placeholder requirements");
        ViewingLedger ledger=new ViewingLedger(null); ledger.opened("one","One","https://example.com/movie.mp4","native"); check(!ViewingLedger.resumable(ledger.find("one")),"open is not progress"); ledger.observe("one",30000,100000,false,false); check(ViewingLedger.resumable(ledger.find("one")),"observed native VOD resumable");
        ledger.opened("web","Web","https://example.com/embed","web"); ledger.observe("web",50000,100000,false,false); check(ledger.find("web").getLong("position")==0&&!ViewingLedger.resumable(ledger.find("web")),"opaque website cannot invent progress");
        ledger.opened("episode2","Episode 2","https://example.com/episode2.mp4","native"); ledger.observe("episode2",70000,100000,false,false); check(ledger.find("one").getLong("position")==30000,"episode isolation");
        ledger.pause(true); ledger.observe("one",60000,100000,false,false); ledger.opened("hidden","Hidden","https://example.com/hidden","browser"); check(ledger.find("hidden")==null&&ledger.find("one").getLong("position")==30000,"paused history freezes new writes"); ledger.mark("one",true); check(!ViewingLedger.resumable(ledger.find("one")),"explicit mark watched allowed");
        ledger.pause(false); ledger.observe("episode2",95000,100000,false,false); check(ledger.find("episode2").getString("status").equals("watched"),"watched threshold"); ledger.mark("episode2",false); check(ledger.find("episode2").getLong("position")==0,"start over clears position");
        ledger.opened("live","Live","https://example.com/live.m3u8","native"); ledger.observe("live",100000,1000000,true,false); check(ledger.find("live").getLong("duration")==0&&!ViewingLedger.resumable(ledger.find("live")),"live has no fabricated percentage");
        ViewingLedger restarted=new ViewingLedger(ledger.snapshot()); check(restarted.find("web")!=null,"snapshot rehydrates"); ledger.pause(true); ledger.clear(); check(ledger.paused()&&ledger.snapshot().getJSONArray("entries").length()==0,"clear preserves privacy setting");
        System.out.println("PASS: "+count+" document, migration, source contract and viewing assertions");
    }
}
