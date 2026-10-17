package com.ismail.nooktv;
import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.view.*;
import org.json.*;

/** Platform-only instrumentation: real Android storage, activity and TV input smoke checks. */
public final class SmokeInstrumentation extends Instrumentation {
    private int assertions;
    private void check(boolean condition,String description) { assertions++; if(!condition) throw new AssertionError(description); }
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
            Intent intent=new Intent(context,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); Activity activity=startActivitySync(intent); waitForIdleSync();
            runOnMainSync(()->{ View root=activity.getWindow().getDecorView(); View home=root.findViewWithTag("nav-home"); check(home!=null&&home.isFocusable(),"native Home navigation"); root.findViewWithTag("nav-settings").performClick(); }); waitForIdleSync();
            runOnMainSync(()->{ View root=activity.getWindow().getDecorView(); check(root.findViewWithTag("settings-collections")!=null,"collections settings reachable"); root.findViewWithTag("settings-collections").performClick(); }); waitForIdleSync();
            runOnMainSync(()->check(activity.getWindow().getDecorView().findViewWithTag("manage-add")!=null,"remote-only collection creation reachable"));
            sendKeyDownUpSync(KeyEvent.KEYCODE_BACK); waitForIdleSync(); runOnMainSync(()->check(activity.getWindow().getDecorView().findViewWithTag("nav-watch")!=null,"Back returns to native Home"));
            result.putString("stream","PASS: "+assertions+" Android storage/activity assertions\n"); result.putInt("assertions",assertions); finish(Activity.RESULT_OK,result);
        } catch(Throwable error) { result.putString("stream","FAIL: "+error.toString()+"\n"); result.putString("failure",error.toString()); finish(Activity.RESULT_CANCELED,result); }
    }
}
