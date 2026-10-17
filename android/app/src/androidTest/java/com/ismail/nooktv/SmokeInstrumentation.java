package com.ismail.nooktv;
import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.view.*;
import org.json.*;
import androidx.media3.common.*;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Platform-only instrumentation: real Android storage, activity and TV input smoke checks. */
public final class SmokeInstrumentation extends Instrumentation {
    private int assertions;
    private void check(boolean condition,String description) { assertions++; if(!condition) throw new AssertionError(description); }
    private void onMain(Runnable run) { final Throwable[] failure={null}; runOnMainSync(()->{ try { run.run(); } catch(Throwable e) { failure[0]=e; } }); if(failure[0]!=null) throw new AssertionError("Main-thread test operation failed",failure[0]); }
    private View awaitTag(Activity activity,String tag) throws Exception { final View[] found={null};long deadline=System.currentTimeMillis()+10000;while(System.currentTimeMillis()<deadline){onMain(()->found[0]=activity.getWindow().getDecorView().findViewWithTag(tag));if(found[0]!=null)return found[0];Thread.sleep(80);}throw new AssertionError("Missing view: "+tag); }
    @Override public void onCreate(Bundle arguments) { super.onCreate(arguments); start(); }
    @Override public void onStart() {
        Bundle result=new Bundle();
        try {
            Context context=getTargetContext(); DashboardStore store=new DashboardStore(context); JSONObject original=store.read(); long revision=original.getLong("revision");
            JSONObject input=new JSONObject(original.toString()).put("title","নুক | Android storage test"); String mutation="instrumentation-save-0001";
            JSONObject saved=store.save(input,revision,mutation); check(saved.getLong("revision")==revision+1,"acknowledgement follows persistence");
            DashboardStore reopened=new DashboardStore(context); check(reopened.read().getString("title").equals(input.getString("title")),"AtomicFile survives repository recreation");
            check(reopened.save(input,revision,mutation).getLong("revision")==revision+1,"mutation receipt survives repository recreation");
            boolean conflict=false; try { reopened.save(input,revision,"instrumentation-conflict"); } catch(java.util.ConcurrentModificationException e) { conflict=true; } check(conflict,"stale write rejected");
            JSONObject backup=DashboardDocument.defaults().put("title","Imported dashboard"); reopened.restore(backup,revision+1,"instrumentation-import-001"); check(reopened.canRecover(),"pre-import recovery exists");
            new DashboardStore(context).undoImport(); check(new DashboardStore(context).read().getString("title").equals(input.getString("title")),"pre-import snapshot durable and restorable");
            long before=new DashboardStore(context).read().getLong("revision"); boolean rejected=false; try { new DashboardStore(context).restore(new JSONObject(backup.toString()).put("schemaVersion",999),before,"instrumentation-bad-import"); } catch(Exception e) { rejected=true; } check(rejected&&new DashboardStore(context).read().getLong("revision")==before,"invalid import leaves disk unchanged");
            ViewingStore viewing=new ViewingStore(context); viewing.ledger.pause(false); viewing.ledger.opened("native-test","Test","https://example.com/sample.mp4","native"); viewing.ledger.observe("native-test",30000,100000,false,false); viewing.persist(); check(new ViewingStore(context).ledger.find("native-test").getLong("position")==30000,"committed progress survives recreation");
            viewing.ledger.remove("native-test");viewing.persist();
            DashboardStore uiStore=new DashboardStore(context);JSONObject uiConfig=new JSONObject(original.toString());String probe=new ComponentName(getContext(),LaunchProbeActivity.class).flattenToString();uiConfig.put("apps",new JSONArray().put(probe));uiConfig.put("title","Make yourself at home.");uiStore.save(uiConfig,uiStore.read().getLong("revision"),"instrumentation-ui-setup");
            byte[] retainedFixture;try(java.io.InputStream in=getContext().getAssets().open("fixture.mp4");java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream()){byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);retainedFixture=out.toByteArray();}
            assertions+=ReceivedMediaChecks.run(context,retainedFixture);
            Intent intent=new Intent(context,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); Activity activity=startActivitySync(intent); waitForIdleSync();
            View direct=awaitTag(activity,"app-"+probe);
            onMain(()->{View root=activity.getWindow().getDecorView();check(root.findViewWithTag("favorite-grid")!=null,"artwork app grid reachable");check(root.findViewWithTag("sample-mp4")==null&&root.findViewWithTag("diagnostics")==null&&root.findViewWithTag("nav-pair")==null,"Home excludes samples and pairing clutter");check(direct.isFocusable(),"app tile accepts remote focus");});
            ActivityMonitor monitor=addMonitor(LaunchProbeActivity.class.getName(),new ActivityResult(Activity.RESULT_OK,null),true);
            onMain(()->direct.performClick());check(monitor.getHits()==1,"one click dispatches the intended app without a management dialog");removeMonitor(monitor);waitForIdleSync();
            onMain(()->{View root=activity.getWindow().getDecorView();root.findViewWithTag("tile-phone").performClick();});waitForIdleSync();
            onMain(()->{View root=activity.getWindow().getDecorView();check(root.findViewWithTag("phone-pair")!=null&&root.findViewWithTag("phone-apps")!=null,"phone sharing options reachable without false receiver");});
            sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);waitForIdleSync();
            onMain(()->{View root=activity.getWindow().getDecorView();root.findViewWithTag("tile-shared").performClick();});waitForIdleSync();
            onMain(()->check(activity.getWindow().getDecorView().findViewWithTag("shared-phone")!=null,"Shared and Recent destination reachable"));
            sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);waitForIdleSync();
            onMain(()->{ View root=activity.getWindow().getDecorView(); View home=root.findViewWithTag("nav-home"); check(home!=null&&home.isFocusable(),"native Home navigation"); root.findViewWithTag("nav-settings").performClick(); }); waitForIdleSync();
            onMain(()->{ View root=activity.getWindow().getDecorView(); check(root.findViewWithTag("settings-collections")!=null,"collections settings reachable"); root.findViewWithTag("settings-collections").performClick(); }); waitForIdleSync();
            onMain(()->check(activity.getWindow().getDecorView().findViewWithTag("manage-add")!=null,"remote-only collection creation reachable"));
            sendKeyDownUpSync(KeyEvent.KEYCODE_BACK); waitForIdleSync(); onMain(()->check(activity.getWindow().getDecorView().findViewWithTag("tile-phone")!=null,"Back returns to launcher Home"));
            onMain(()->activity.getWindow().getDecorView().findViewWithTag("nav-settings").performClick());waitForIdleSync();
            onMain(()->activity.getWindow().getDecorView().findViewWithTag("settings-home").performClick());waitForIdleSync();
            onMain(()->check(activity.getWindow().getDecorView().findViewWithTag("home-enable")!=null,"explicit Home setup and recovery reachable"));
            ComponentName alias=new ComponentName(context,"com.ismail.nooktv.HomeEntry");check(context.getPackageManager().getComponentEnabledSetting(alias)!=android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,"Home alias disabled by default");
            context.getPackageManager().setComponentEnabledSetting(alias,android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,android.content.pm.PackageManager.DONT_KILL_APP);
            boolean eligible=false;for(android.content.pm.ResolveInfo r:context.getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),0))if(r.activityInfo.packageName.equals(context.getPackageName()))eligible=true;check(eligible,"enabled alias advertises HOME");
            context.getPackageManager().setComponentEnabledSetting(alias,android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,android.content.pm.PackageManager.DONT_KILL_APP);
            sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);waitForIdleSync();
            View savedVideo=awaitTag(activity,"received-"+ReceivedMediaChecks.ID);onMain(savedVideo::performClick);waitForIdleSync();
            final ExoPlayer[] retainedPlayer={null};CountDownLatch retainedReady=new CountDownLatch(1);
            onMain(()->{try{java.lang.reflect.Field field=MainActivity.class.getDeclaredField("player");field.setAccessible(true);retainedPlayer[0]=(ExoPlayer)field.get(activity);retainedPlayer[0].addListener(new Player.Listener(){@Override public void onPlaybackStateChanged(int state){if(state==Player.STATE_READY)retainedReady.countDown();}});if(retainedPlayer[0].getPlaybackState()==Player.STATE_READY)retainedReady.countDown();}catch(Exception e){throw new RuntimeException(e);}});
            check(retainedReady.await(20,TimeUnit.SECONDS),"production player prepares retained phone video without sender");
            onMain(()->{check(retainedPlayer[0].getCurrentMediaItem().localConfiguration.uri.getScheme().equals("file"),"retained playback resolves private local file");retainedPlayer[0].seekTo(1200);retainedPlayer[0].pause();});Thread.sleep(250);
            onMain(()->activity.getWindow().getDecorView().findViewWithTag("player-close").performClick());waitForIdleSync();
            long savedUntil=System.currentTimeMillis()+10000;boolean positionSaved=false;while(System.currentTimeMillis()<savedUntil){JSONObject entry=new ViewingStore(context).ledger.find("local:"+ReceivedMediaChecks.ID);if(entry!=null&&entry.optLong("position")>=1100){positionSaved=true;break;}Thread.sleep(100);}check(positionSaved,"retained-video progress persists after leaving production player");
            java.io.File fixture=new java.io.File(context.getCacheDir(),"fixture.mp4");
            try(java.io.InputStream in=getContext().getAssets().open("fixture.mp4"); java.io.OutputStream out=new java.io.FileOutputStream(fixture)) { byte[] bytes=new byte[4096]; int n; while((n=in.read(bytes))!=-1) out.write(bytes,0,n); }
            final ExoPlayer[] player={null}; final PlayerView[] view={null}; CountDownLatch ready=new CountDownLatch(1); final String[] failure={null};
            onMain(()->{ player[0]=new ExoPlayer.Builder(context).build(); view[0]=new PlayerView(activity); view[0].setPlayer(player[0]); activity.addContentView(view[0],new ViewGroup.LayoutParams(320,180)); player[0].addListener(new Player.Listener(){ @Override public void onPlaybackStateChanged(int state){ if(state==Player.STATE_READY)ready.countDown(); } @Override public void onPlayerError(PlaybackException e){ failure[0]=e.getErrorCodeName();ready.countDown(); }}); player[0].setMediaItem(MediaItem.fromUri(android.net.Uri.fromFile(fixture))); player[0].prepare();player[0].play(); });
            check(ready.await(20,TimeUnit.SECONDS)&&failure[0]==null,"Media3 prepares generated H264/AAC fixture");
            CountDownLatch advancing=new CountDownLatch(1); final String[] playerState={"No sample"};
            android.os.Handler main=new android.os.Handler(android.os.Looper.getMainLooper());
            Runnable sample=new Runnable(){ public void run(){ long position=player[0].getCurrentPosition(),duration=player[0].getDuration(); playerState[0]="position="+position+", duration="+duration+", state="+player[0].getPlaybackState()+", ready="+player[0].getPlayWhenReady()+", suppression="+player[0].getPlaybackSuppressionReason(); if(duration>2000&&position>300)advancing.countDown();else main.postDelayed(this,100); }};
            main.post(sample); boolean advanced=advancing.await(12,TimeUnit.SECONDS); main.removeCallbacks(sample); check(advanced,"Media3 advances a finite native timeline: "+playerState[0]);
            onMain(()->{ player[0].seekTo(1500);player[0].pause(); });
            Thread.sleep(300); onMain(()->{ check(player[0].getCurrentPosition()>=1400&&!player[0].getPlayWhenReady(),"native seek and pause"); view[0].setPlayer(null);player[0].release();((ViewGroup)view[0].getParent()).removeView(view[0]); });
            // Restore owner-shaped Home for the visual artifact, not storage/test fixture text.
            onMain(()->{try{MainActivity mainActivity=(MainActivity)activity;mainActivity.save(original,mainActivity.config().getLong("revision"),"instrumentation-ui-cleanup");}catch(Exception e){throw new RuntimeException(e);}});waitForIdleSync();
            onMain(()->activity.getWindow().getDecorView().findViewWithTag("nav-home").performClick());waitForIdleSync();Thread.sleep(800);
            result.putString("stream","PASS: "+assertions+" Android storage/activity assertions\n"); result.putInt("assertions",assertions); finish(Activity.RESULT_OK,result);
        } catch(Throwable error) { result.putString("stream","FAIL: "+android.util.Log.getStackTraceString(error)+"\n"); result.putString("failure",error.toString()); finish(Activity.RESULT_CANCELED,result); }
    }
}
