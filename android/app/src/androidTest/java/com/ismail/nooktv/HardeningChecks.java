package com.ismail.nooktv;

import android.app.*;
import android.content.Context;
import android.view.*;
import org.json.*;
import java.lang.reflect.*;
import java.util.*;

final class HardeningChecks {
    private static int assertions;
    private static void check(boolean pass,String message){assertions++;if(!pass)throw new AssertionError(message);}
    private static void onMain(Instrumentation test,Runnable run){final Throwable[] failure={null};test.runOnMainSync(()->{try{run.run();}catch(Throwable e){failure[0]=e;}});if(failure[0]!=null)throw new AssertionError("Main-thread hardening check",failure[0]);}
    private static Object field(Activity a,String name)throws Exception{Field f=MainActivity.class.getDeclaredField(name);f.setAccessible(true);return f.get(a);}
    private static void invoke(Activity a,String name){try{Method m=MainActivity.class.getDeclaredMethod(name);m.setAccessible(true);m.invoke(a);}catch(Exception e){throw new RuntimeException(e);}}
    static int run(Instrumentation test,Activity activity,Context c)throws Exception{
        assertions=0;SourceStore sources=(SourceStore)field(activity,"sourceStore");JSONObject original=sources.read();
        ContentSourceManager manager=(ContentSourceManager)field(activity,"sourceManager");Field cacheField=ContentSourceManager.class.getDeclaredField("cache");cacheField.setAccessible(true);
        @SuppressWarnings("unchecked") Map<String,ContentSourceManager.CachedSource> cache=(Map<String,ContentSourceManager.CachedSource>)cacheField.get(manager);
        String url="https://www.youtube.com/channel/UC12345",tag=SourceDocument.itemTag("focus-source","a","https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        JSONObject fixture=SourceDocument.defaults();fixture.getJSONArray("sources").put(new JSONObject().put("id","focus-source").put("type","youtube").put("label","Focus fixture").put("url",url).put("enabled",true));sources.save(fixture,original.getLong("revision"),"focus-fixture-source-save");
        FeedParser.FeedItem a=new FeedParser.FeedItem("a","A","https://www.youtube.com/watch?v=dQw4w9WgXcQ","","",""),b=new FeedParser.FeedItem("b","B","https://www.youtube.com/watch?v=aqz-KE-bpKQ","","","");
        cache.put("focus-source",new ContentSourceManager.CachedSource("focus-source","youtube:"+url,System.currentTimeMillis(),false,"Updated",new FeedParser.FeedResult("Focus fixture",Arrays.asList(a,b))));
        onMain(test,()->invoke(activity,"home"));test.waitForIdleSync();
        // View.post() restoration and the first traversal must finish before offscreen focus requests.
        long focusDeadline=android.os.SystemClock.uptimeMillis()+5000;boolean[] laidOut={false};
        do{onMain(test,()->{View v=activity.getWindow().getDecorView().findViewWithTag(tag);laidOut[0]=v!=null&&v.isLaidOut()&&v.isShown()&&v.hasWindowFocus();});if(!laidOut[0])Thread.sleep(50);}while(!laidOut[0]&&android.os.SystemClock.uptimeMillis()<focusDeadline);
        check(laidOut[0],"source shelf is laid out in the foreground window");
        test.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);test.waitForIdleSync();
        View[] before={null};onMain(test,()->{before[0]=activity.getWindow().getDecorView().findViewWithTag(tag);check(before[0]!=null,"source item has stable identity tag");check(before[0].requestFocus(),"source tile accepts remote focus; touchMode="+before[0].isInTouchMode()+", size="+before[0].getWidth()+"x"+before[0].getHeight());check(before[0].hasFocus(),"source tile holds focus before refresh");});test.waitForIdleSync();
        cache.put("focus-source",new ContentSourceManager.CachedSource("focus-source","youtube:"+url,System.currentTimeMillis(),false,"Updated",new FeedParser.FeedResult("Focus fixture",Arrays.asList(b,a))));
        onMain(test,()->{invoke(activity,"renderSourceShelves");View root=activity.getWindow().getDecorView();check(root.findViewWithTag(tag)==before[0]&&before[0].hasFocus(),"feed refresh defers reorder while focused; sameView="+(root.findViewWithTag(tag)==before[0])+", actualFocus="+(root.findFocus()==null?"none":root.findFocus().getTag()));root.findViewWithTag("nav-settings").requestFocus();});test.waitForIdleSync();
        onMain(test,()->{View root=activity.getWindow().getDecorView();check(root.findViewWithTag(tag)!=before[0],"deferred feed update applies after focus leaves shelf");check(root.findViewWithTag("nav-settings").hasFocus(),"refresh does not steal focus from navigation");});
        ReceivedMediaStore media=(ReceivedMediaStore)field(activity,"received");ViewingStore viewing=(ViewingStore)field(activity,"viewing");String id=ReceivedMediaChecks.ID;
        media.update(id,new JSONObject().put("hidden",true));viewing.ledger.openedLocal(id,"Hidden fixture");viewing.ledger.observe("local:"+id,1000,10000,false,false);viewing.persist();
        onMain(test,()->{invoke(activity,"home");View root=activity.getWindow().getDecorView();check(root.findViewWithTag("received-"+id)==null&&root.findViewWithTag("history-local:"+id)==null,"hidden media is absent from every Home row");invoke(activity,"shared");check(activity.getWindow().getDecorView().findViewWithTag("received-"+id)!=null,"hidden-from-Home clip remains manageable in Shared");});
        media.update(id,new JSONObject().put("hidden",false));viewing.ledger.remove("local:"+id);viewing.persist();sources.save(original,sources.read().getLong("revision"),"focus-fixture-source-clean");cache.remove("focus-source");onMain(test,()->invoke(activity,"home"));test.waitForIdleSync();
        return assertions;
    }
}
