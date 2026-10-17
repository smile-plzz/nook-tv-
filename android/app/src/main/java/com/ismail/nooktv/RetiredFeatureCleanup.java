package com.ismail.nooktv;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.File;

/** Remove only private settings/cache owned by the retired discovery feature. */
final class RetiredFeatureCleanup {
    private RetiredFeatureCleanup() {}

    static void run(Context context) {
        // This obsolete private preference file contained only the retired service credential.
        context.getSharedPreferences("secrets",0).edit().clear().commit();
        SharedPreferences preferences=context.getSharedPreferences("nook",0);
        SharedPreferences.Editor edit=preferences.edit().remove("sources");
        for(String key:preferences.getAll().keySet())
            if(key.matches("tt[0-9]{7,10}-(season|episode|source)")) edit.remove(key);
        edit.commit();
        // No recursive deletion: only generated response-cache files in the old private directory.
        File directory=new File(context.getCacheDir(),"metadata");
        File[] files=directory.listFiles();
        if(files!=null) for(File file:files)
            if(file.isFile()&&file.getName().matches("[0-9a-f]{1,8}\\.json")) file.delete();
        directory.delete(); // Succeeds only when empty; unrelated contents remain untouched.
    }
}
