package com.ismail.nooktv;
import android.content.*;
import android.content.pm.*;
import java.util.*;

/** Package discovery stays outside the UI thread; prefer TV activities when both are present. */
final class LauncherApps {
    static final class Entry {
        final String component,label; final ActivityInfo info;
        Entry(ActivityInfo info,String label){this.info=info;this.label=label;component=new ComponentName(info.packageName,info.name).flattenToString();}
    }
    static List<Entry> discover(Context context){
        PackageManager pm=context.getPackageManager();Map<String,Entry> entries=new LinkedHashMap<>();
        for(String category:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER})for(ResolveInfo r:pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(category),0)){
            if(r.activityInfo.packageName.equals(context.getPackageName())||r.activityInfo.packageName.equals(context.getPackageName()+".test")||entries.containsKey(r.activityInfo.packageName))continue;
            entries.put(r.activityInfo.packageName,new Entry(r.activityInfo,r.loadLabel(pm).toString()));
        }
        List<Entry> result=new ArrayList<>(entries.values());result.sort((a,b)->{int priority=Integer.compare(priority(a.info.packageName),priority(b.info.packageName));return priority!=0?priority:a.label.compareToIgnoreCase(b.label);});return result;
    }
    private static int priority(String pkg){return pkg.startsWith("com.google.android.youtube")?0:pkg.startsWith("com.spotify")?1:2;}
}
