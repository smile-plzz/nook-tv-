package com.ismail.nooktv;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import org.json.*;
import java.io.*;
import java.util.Arrays;

/** Run this same test APK against F4, then F5 without uninstalling the target app. */
final class UpgradeChecks {
    private final Instrumentation test;
    private UpgradeChecks(Instrumentation test){this.test=test;}
    private static final String ID="66666666666666666666666666666666";
    private int assertions;
    private void check(boolean value,String message){assertions++;if(!value)throw new AssertionError(message);}
    private byte[] fixture()throws Exception{try(InputStream in=test.getContext().getAssets().open("fixture.mp4");ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)out.write(b,0,n);return out.toByteArray();}}
    static int run(Instrumentation test,String mode)throws Exception{return new UpgradeChecks(test).execute(mode);}
    private int execute(String mode)throws Exception{
        Context c=test.getTargetContext();byte[] video=fixture();
        DashboardStore dashboard=new DashboardStore(c);SourceStore sources=new SourceStore(c);ReceivedMediaStore media=new ReceivedMediaStore(c);ViewingStore viewing=new ViewingStore(c);
        if(mode.equals("seed")){
            JSONObject d=dashboard.read();d.put("title","Upgrade continuity").getJSONArray("apps").put("com.example.player/com.example.player.MainActivity");dashboard.save(d,d.getLong("revision"),"upgrade-dashboard-seed");
            JSONObject s=sources.read();s.getJSONArray("sources").put(new JSONObject().put("id","upgrade-source").put("type","youtube").put("label","Upgrade source").put("url","https://www.youtube.com/channel/UC12345").put("enabled",false));sources.save(s,s.getLong("revision"),"upgrade-sources-seed");
            media.receive(new ByteArrayInputStream(video),video.length,"Upgrade video.mp4",ID,()->true);media.update(ID,new JSONObject().put("pinned",true).put("hidden",true).put("collection","Upgrade collection"));media.thumbnail(ID);
            viewing.ledger.openedLocal(ID,"Upgrade video.mp4");viewing.ledger.observe("local:"+ID,1000,10000,false,false);viewing.persist();
            c.getSharedPreferences("nook",0).edit().putBoolean("reducedMotion",true).commit();
        }else{
            JSONObject d=dashboard.read();check(d.getString("title").equals("Upgrade continuity"),"dashboard title survives same-key APK upgrade");check(d.getJSONArray("apps").getString(0).equals("com.example.player/com.example.player.MainActivity"),"pinned app survives upgrade");
            check(sources.read().getJSONArray("sources").getJSONObject(0).getString("id").equals("upgrade-source"),"independent source configuration survives upgrade");
            JSONObject item=media.find(ID);check(item!=null&&item.getBoolean("pinned")&&item.getBoolean("hidden")&&item.getString("collection").equals("Upgrade collection"),"personal media organization survives upgrade");
            check(Arrays.equals(ReceivedMediaChecks.read(media.playable(ID)),video),"retained video bytes survive upgrade");check(media.thumbnail(ID)!=null,"thumbnail survives upgrade");
            check(viewing.ledger.find("local:"+ID).getLong("position")==1000,"measured viewing position survives upgrade");check(c.getSharedPreferences("nook",0).getBoolean("reducedMotion",false),"appearance preference survives upgrade");
            media.delete(ID);viewing.ledger.remove("local:"+ID);viewing.persist();sources.save(SourceDocument.defaults(),sources.read().getLong("revision"),"upgrade-sources-clean");dashboard.save(DashboardDocument.defaults(),d.getLong("revision"),"upgrade-dashboard-clean");c.getSharedPreferences("nook",0).edit().remove("reducedMotion").commit();
        }
        return assertions;
    }
}
