package com.ismail.nooktv;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.*;
import android.net.Uri;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.util.*;

public final class MainActivity extends Activity implements EditorServer.Host {
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Pairing pairing=new Pairing();
    private DashboardStore store;
    private EditorServer server;
    private LinearLayout root, content;
    private String screen="home", focusKey="", returnFocus="", playingUrl="", playingLabel="";
    private VideoView video;
    private WebView web;
    private TextView playerStatus, pairInfo;
    private boolean prepared, videoStopped;
    private volatile boolean foreground;
    private List<String> addresses=new ArrayList<>();
    private String serverError="";
    private android.content.SharedPreferences history;
    private final int ink=Color.rgb(16,22,25), mint=Color.rgb(156,229,197), muted=Color.rgb(166,183,185);
    private final Runnable progress=new Runnable() { public void run() { saveProgress(); if(video!=null) handler.postDelayed(this,5000); } };

    @Override public void onCreate(Bundle b) {
        super.onCreate(b); getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        store=new DashboardStore(this); history=getSharedPreferences("native-progress",MODE_PRIVATE); server=new EditorServer(pairing,this);
        home();
    }
    @Override public void onResume() {
        super.onResume(); foreground=true;
        if(videoStopped) { videoStopped=false; home(); }
        try { addresses=server.start(); serverError=""; } catch(IOException e) { addresses=new ArrayList<>(); serverError="Editor could not start. Port 8765 may be in use."; }
        if(pairInfo!=null&&screen.equals("pair")) pairInfo.setText(addressText());
    }
    @Override public void onPause() {
        foreground=false; saveProgress(); if(video!=null&&prepared) video.pause(); if(web!=null) web.onPause(); server.stop(); super.onPause();
    }
    @Override public void onStop() {
        if(video!=null) { video.stopPlayback(); prepared=false; videoStopped=true; handler.removeCallbacks(progress); }
        if(web!=null) { releaseWeb(); screen="home"; home(); }
        super.onStop();
    }
    @Override public void onDestroy() { handler.removeCallbacksAndMessages(null); releasePlayback(); server.stop(); super.onDestroy(); }

    private int dp(int n) { return (int)(n*getResources().getDisplayMetrics().density+.5f); }
    private LinearLayout vertical() { LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private TextView text(String s,int size,int color) { TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); t.setPadding(0,dp(6),0,dp(6)); return t; }
    private GradientDrawable surface(int color) { GradientDrawable g=new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(12)); return g; }
    private Button action(String label,String key,Runnable run) {
        Button b=new Button(this); b.setText(label); b.setAllCaps(false); b.setTextColor(Color.WHITE); b.setTextSize(16); b.setMinHeight(dp(52)); b.setPadding(dp(18),dp(8),dp(18),dp(8)); b.setBackground(surface(Color.rgb(32,44,48)));
        b.setTag(key); b.setOnClickListener(v->run.run()); b.setOnFocusChangeListener((v,focused)->{ b.setBackground(surface(focused?mint:Color.rgb(32,44,48))); b.setTextColor(focused?ink:Color.WHITE); if(focused) focusKey=key; });
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2); p.setMargins(0,dp(5),dp(10),dp(5)); b.setLayoutParams(p); return b;
    }
    private void page(String name,String title,String subtitle) {
        releasePlayback(); screen=name; pairInfo=null;
        root=vertical(); root.setBackgroundColor(ink); root.setPadding(dp(40),dp(24),dp(40),dp(24));
        LinearLayout nav=new LinearLayout(this); nav.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo=text("nook",24,mint); logo.setTypeface(null,Typeface.BOLD); nav.addView(logo,new LinearLayout.LayoutParams(0,-2,1));
        nav.addView(action("Home","nav-home",this::home)); nav.addView(action("Apps","nav-apps",this::apps)); nav.addView(action("Pair PC","nav-pair",this::pair)); nav.addView(action("Settings","nav-settings",this::settings)); root.addView(nav);
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); content=vertical(); content.setPadding(0,dp(12),0,dp(20)); scroll.addView(content); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        content.addView(text(title,30,Color.WHITE)); content.addView(text(subtitle,15,muted)); setContentView(root);
    }
    private void section(String title) { TextView t=text(title,20,Color.WHITE); t.setPadding(0,dp(20),0,dp(8)); content.addView(t); }
    private LinearLayout row() { HorizontalScrollView s=new HorizontalScrollView(this); s.setHorizontalScrollBarEnabled(false); LinearLayout r=new LinearLayout(this); r.setPadding(0,dp(3),0,dp(6)); s.addView(r); content.addView(s); return r; }
    private void restore(String key) { root.post(()->{ View v=root.findViewWithTag(key); if(v!=null) v.requestFocus(); }); }
    private JSONObject current() { try { return store.read(); } catch(JSONException e) { throw new IllegalStateException(e); } }
    private void home() {
        String oldFocus=returnFocus.isEmpty()?focusKey:returnFocus; returnFocus=""; JSONObject config=current();
        page("home",config.optString("title"),"A little less searching. A little more watching.  •  Compatibility preview 0.1");
        String last=history.getString("url",""); int pos=history.getInt("position",0), duration=history.getInt("duration",0);
        if(!last.isEmpty()&&pos>0&&duration>0&&pos<duration*.95) {
            section("Continue watching"); LinearLayout r=row();
            r.addView(action(history.getString("label","Video")+"  •  Resume "+time(pos),"resume",()->play(last,history.getString("label","Video"))));
            r.addView(action("Start over","restart",()->{ history.edit().putInt("position",0).commit(); play(last,history.getString("label","Video")); }));
            r.addView(action("Remove","remove-progress",()->{ boolean paused=history.getBoolean("paused",false); history.edit().clear().putBoolean("paused",paused).commit(); home(); }));
        }
        section("Favorite apps"); LinearLayout favorites=row(); JSONArray pinned=config.optJSONArray("apps");
        if(pinned.length()==0) favorites.addView(action("+ Pin your first app","pin-first",this::apps));
        for(int i=0;i<pinned.length();i++) { final String component=pinned.optString(i); favorites.addView(action(appLabel(component),"app-"+component,()->appOptions(component))); }
        section("Saved for later"); JSONArray links=config.optJSONArray("links"); LinearLayout linksRow=row();
        if(links.length()==0) linksRow.addView(action("+ Add a link","add-link",this::addLink));
        for(int i=0;i<links.length();i++) { JSONObject l=links.optJSONObject(i); linksRow.addView(action(l.optString("label"),"link-"+l.optString("id"),()->linkOptions(l))); }
        section("Try your TV"); LinearLayout samples=row();
        samples.addView(action("Native MP4 sample","sample-mp4",()->play("https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4","Big Buck Bunny • MP4")));
        samples.addView(action("HLS sample","sample-hls",()->play("https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8","Apple BipBop • HLS")));
        samples.addView(action("YouTube","youtube",()->external("https://www.youtube.com/","youtube")));
        samples.addView(action("Web isolation test","sample-web",()->web("","Controlled web test")));
        samples.addView(action("Diagnostics","diagnostics",this::diagnostics));
        content.addView(text("NowShowing catalog, DASH/Media3 and web filtering are upcoming milestones. No provider is marked verified.",14,muted)); restore(oldFocus);
    }
    private String time(int ms) { return String.format(Locale.ROOT,"%d:%02d",ms/60000,ms/1000%60); }
    private String appLabel(String component) {
        try { return getPackageManager().getActivityInfo(ComponentName.unflattenFromString(component),0).loadLabel(getPackageManager()).toString(); }
        catch(Exception e) { ComponentName c=ComponentName.unflattenFromString(component); return (c==null?component:c.getPackageName())+" • Not installed"; }
    }
    private void apps() {
        page("apps","Make yourself at home","Choose an app to open it or pin it to your dashboard.");
        Map<String,String> discovered=new TreeMap<>(); PackageManager pm=getPackageManager();
        for(String category:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER}) for(ResolveInfo r:pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(category),0)) {
            if(r.activityInfo.packageName.equals(getPackageName())) continue;
            String component=new ComponentName(r.activityInfo.packageName,r.activityInfo.name).flattenToString(); discovered.put(component,r.loadLabel(pm).toString());
        }
        List<Map.Entry<String,String>> list=new ArrayList<>(discovered.entrySet()); list.sort(Comparator.comparing(Map.Entry::getValue,String.CASE_INSENSITIVE_ORDER));
        LinearLayout r=row(); int count=0;
        for(Map.Entry<String,String> app:list) { if(count>0&&count%5==0) r=row(); r.addView(action(app.getValue(),"app-"+app.getKey(),()->appOptions(app.getKey()))); count++; }
        if(count==0) content.addView(text("No launchable apps were reported by this device.",18,muted));
    }
    private void appOptions(String component) {
        boolean pinned=false; JSONArray apps=current().optJSONArray("apps"); for(int i=0;i<apps.length();i++) if(component.equals(apps.optString(i))) pinned=true;
        final boolean isPinned=pinned;
        new AlertDialog.Builder(this).setTitle(appLabel(component)).setItems(new String[]{"Open",pinned?"Unpin from Home":"Pin to Home"},(d,n)->{
            if(n==0) { try { startActivity(new Intent(Intent.ACTION_MAIN).setComponent(ComponentName.unflattenFromString(component)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); } catch(Exception e) { message("This app is unavailable. You can unpin it."); } }
            else try { JSONObject c=current(); JSONArray next=new JSONArray(); for(int i=0;i<apps.length();i++) if(!component.equals(apps.optString(i))) next.put(apps.optString(i)); if(!isPinned) next.put(component); c.put("apps",next); store.save(c,c.getLong("revision"),UUID.randomUUID().toString()); home(); } catch(Exception e) { message("Could not save favorite."); }
        }).show();
    }
    private void linkOptions(JSONObject link) {
        new AlertDialog.Builder(this).setTitle(link.optString("label")).setItems(new String[]{"Open • "+link.optString("route"),"Remove from saved links"},(d,n)->{
            if(n==0) { String u=link.optString("url"), label=link.optString("label"), route=link.optString("route"); if(route.equals("native")) play(u,label); else if(route.equals("web")) web(u,label); else external(u,route); }
            else try { JSONObject c=current(); JSONArray a=c.getJSONArray("links"), next=new JSONArray(); for(int i=0;i<a.length();i++) if(!a.getJSONObject(i).getString("id").equals(link.optString("id"))) next.put(a.getJSONObject(i)); c.put("links",next); store.save(c,c.getLong("revision"),UUID.randomUUID().toString()); home(); } catch(Exception e) { message("Could not remove link."); }
        }).show();
    }
    private void addLink() {
        LinearLayout fields=vertical(); fields.setPadding(dp(24),dp(8),dp(24),0); EditText label=new EditText(this),url=new EditText(this); label.setHint("Label"); url.setHint("https://…"); url.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI); fields.addView(label); fields.addView(url);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Save a website or YouTube link").setView(fields).setPositiveButton("Save",null).setNegativeButton("Cancel",null).create();
        dialog.setOnShowListener(d->dialog.getButton(-1).setOnClickListener(v->{ try { String u=url.getText().toString().trim(); Targets.https(u); JSONObject c=current(),l=new JSONObject(); l.put("id",UUID.randomUUID().toString()).put("label",label.getText().toString().trim()).put("url",u).put("route",Targets.youtube(u)?"youtube":"browser"); c.getJSONArray("links").put(l); store.save(c,c.getLong("revision"),UUID.randomUUID().toString()); dialog.dismiss(); home(); } catch(Exception e) { url.setError("Enter a label and valid HTTPS URL."); } })); dialog.show();
    }
    private void external(String url,String route) {
        try { Targets.https(url); Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse(url));
            if(route.equals("youtube")) { for(String pkg:new String[]{"com.google.android.youtube.tv","com.google.android.youtube"}) { Intent candidate=new Intent(intent).setPackage(pkg); if(candidate.resolveActivity(getPackageManager())!=null) { startActivity(candidate); return; } } }
            startActivity(intent);
        } catch(Exception e) { message("No compatible app or browser is installed for this link."); }
    }
    private void play(String url,String label) {
        try { Targets.https(url); } catch(Exception e) { message(e.getMessage()); return; }
        if(Uri.parse(url).getPath()!=null&&Uri.parse(url).getPath().endsWith(".mpd")) { message("DASH needs the planned Media3 player. This compatibility build supports platform MP4/HLS probes."); return; }
        returnFocus=focusKey; page("player",label,"Native test player • Progress is saved only after playback is prepared."); playingUrl=url; playingLabel=label; prepared=false;
        video=new VideoView(this); video.setBackgroundColor(Color.BLACK); content.addView(video,new LinearLayout.LayoutParams(-1,dp(240)));
        playerStatus=text("Loading…",14,muted); content.addView(playerStatus); LinearLayout controls=row();
        controls.addView(action("Play / Pause","play-pause",()->{ if(prepared) { if(video.isPlaying()) video.pause(); else video.start(); saveProgress(); } }));
        controls.addView(action("−10 sec","seek-back",()->seek(-10000))); controls.addView(action("+10 sec","seek-forward",()->seek(10000))); controls.addView(action("Back to Home","player-close",this::home));
        video.setOnPreparedListener(mp->{ prepared=true; int pos=playingUrl.equals(history.getString("url",""))?history.getInt("position",0):0; if(pos>0&&mp.getDuration()>pos) video.seekTo(pos); video.start(); handler.post(progress); });
        video.setOnCompletionListener(mp->{ saveProgress(); playerStatus.setText("Finished • Back returns to your dashboard."); });
        video.setOnErrorListener((mp,what,extra)->{ prepared=false; playerStatus.setText("This stream could not play ("+what+"/"+extra+"). Check network/codec support or try another link."); return true; });
        video.setVideoURI(Uri.parse(url)); restore("play-pause");
    }
    private void seek(int amount) { if(video!=null&&prepared) { video.seekTo(Math.max(0,Math.min(video.getDuration(),video.getCurrentPosition()+amount))); saveProgress(); } }
    private void saveProgress() {
        if(video==null||!prepared) return;
        try { int p=video.getCurrentPosition(),d=video.getDuration(); if(d>0) { if(!history.getBoolean("paused",false)) history.edit().putString("url",playingUrl).putString("label",playingLabel).putInt("position",p).putInt("duration",d).commit(); if(playerStatus!=null) playerStatus.setText(time(p)+" / "+time(d)+(video.isPlaying()?" • Playing":" • Paused")); } } catch(Exception ignored) {}
    }
    private void web(String url,String label) {
        returnFocus=focusKey; page("web",label,"Progress unavailable • Back leaves the web player. No privileged bridge or Brave Shields.");
        LinearLayout controls=row(); controls.addView(action("Leave web player","web-close",this::home)); if(!url.isEmpty()) controls.addView(action("Open in browser","web-browser",()->external(url,"browser")));
        try { web=new WebView(this); } catch(Exception e) { content.addView(text("WebView unavailable. Try an installed browser.",18,muted)); return; }
        WebSettings s=web.getSettings(); s.setJavaScriptEnabled(!url.isEmpty()); s.setDomStorageEnabled(!url.isEmpty()); s.setAllowFileAccess(false); s.setAllowContentAccess(false); s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW); s.setMediaPlaybackRequiresUserGesture(true); s.setSupportMultipleWindows(true); s.setJavaScriptCanOpenWindowsAutomatically(false);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest request) {
                if(!request.isForMainFrame()) return false;
                try { Targets.https(request.getUrl().toString()); } catch(Exception e) { return true; }
                String requested=request.getUrl().getHost(), initial=Uri.parse(url).getHost();
                if(Objects.equals(requested,initial)) return false;
                if(request.hasGesture()) new AlertDialog.Builder(MainActivity.this).setTitle("Leave this site?").setMessage("Open this link in an external browser?").setPositiveButton("Open",(d,n)->external(request.getUrl().toString(),"browser")).setNegativeButton("Cancel",null).show();
                return true;
            }
            @Override public void onReceivedError(WebView v,WebResourceRequest r,WebResourceError e) { if(r.isForMainFrame()) message("Website unavailable. Back or Open in browser can help."); }
        });
        web.setWebChromeClient(new WebChromeClient()); web.setDownloadListener((a,b,c,d,e)->message("Downloads are disabled in the test player.")); content.addView(web,new LinearLayout.LayoutParams(-1,dp(260)));
        if(url.isEmpty()) web.loadDataWithBaseURL("https://fixture.nook.invalid/","<html><body style='background:#162923;color:#9ce5c5;font:24px sans-serif;padding:35px'><h1>Web isolation test</h1><p>This is controlled bundled content.</p><p>Press Back to return to Nook.</p><button>Focus test</button></body></html>","text/html","utf-8",null);
        else web.loadUrl(url); restore("web-close");
    }
    private void releaseWeb() { if(web!=null) { web.stopLoading(); web.loadUrl("about:blank"); web.destroy(); web=null; } }
    private void releasePlayback() { saveProgress(); handler.removeCallbacks(progress); if(video!=null) { video.stopPlayback(); video=null; } prepared=false; playerStatus=null; releaseWeb(); }
    private String addressText() { return !serverError.isEmpty()?serverError:addresses.isEmpty()?"Connect your TV and PC to the same home network, then reopen this screen.":String.join("\n",addresses)+"\nKeep Nook open on the TV while editing. HTTP local connection; do not enter account passwords."; }
    private void pair() {
        page("pair","Bring your keyboard","Customize from a browser on your home network. Every pairing needs your approval here.");
        pairInfo=text(addressText(),19,mint); content.addView(pairInfo); content.addView(text("Pairing code:  "+pairing.begin(),36,Color.WHITE)); content.addView(text("Expires in 2 minutes. Five incorrect attempts lock this code.\nApprove only a browser you just requested. Codes and sessions are reset after an app restart.",16,muted));
        content.addView(action("Generate a new code","new-code",this::pair)); content.addView(action("Revoke all browsers","revoke",()->{ pairing.revoke(); message("Browser sessions revoked."); pair(); }));
    }
    @Override public void approve(String name) { handler.post(()->{ if(!foreground||!screen.equals("pair")) { pairing.cancel(); return; } new AlertDialog.Builder(this).setTitle("Allow “"+name+"” to edit?").setMessage("Approve only if you just submitted the code from your PC.").setPositiveButton("Allow",(d,n)->pairing.approve()).setNegativeButton("Deny",(d,n)->pairing.cancel()).setOnCancelListener(d->pairing.cancel()).show(); }); }
    @Override public byte[] asset(String name) throws IOException { try(InputStream in=getAssets().open(name); ByteArrayOutputStream out=new ByteArrayOutputStream()) { byte[] b=new byte[4096]; int n; while((n=in.read(b))>0) out.write(b,0,n); return out.toByteArray(); } }
    @Override public JSONObject config() throws Exception { if(!foreground) throw new IOException(); return store.read(); }
    @Override public JSONObject save(JSONObject input,long revision,String mutation) throws Exception { if(!foreground) throw new IOException(); JSONObject result=store.save(input,revision,mutation); handler.post(()->{ if(screen.equals("home")) home(); }); return result; }
    private void settings() {
        page("settings","Settings","Compatibility preview. Your saved dashboard stays on this TV.");
        content.addView(action("Add a saved link","settings-add",this::addLink));
        content.addView(action(history.getBoolean("paused",false)?"Resume recording playback history":"Pause playback history","history-pause",()->{ history.edit().putBoolean("paused",!history.getBoolean("paused",false)).commit(); settings(); }));
        content.addView(action("Clear viewing history","history-clear",()->new AlertDialog.Builder(this).setTitle("Clear native viewing history?").setMessage("Saved links and favorite apps stay.").setPositiveButton("Clear",(d,n)->{ boolean paused=history.getBoolean("paused",false); history.edit().clear().putBoolean("paused",paused).commit(); message("Viewing history cleared."); }).setNegativeButton("Cancel",null).show()));
        content.addView(action("Revoke paired browsers","settings-revoke",()->{ pairing.revoke(); message("Browser sessions revoked."); }));
        content.addView(action("Device diagnostics","settings-diagnostics",this::diagnostics));
        content.addView(text("Dashboard backup is available in the PC editor. Backgrounds, full collections, provider catalog, filtering and launcher replacement are not part of this M1 preview.",16,muted));
    }
    private void diagnostics() {
        page("diagnostics","Know your TV","No unique device identifiers, LAN addresses or session secrets are included below.");
        ActivityManager am=(ActivityManager)getSystemService(ACTIVITY_SERVICE); StringBuilder s=new StringBuilder("Nook TV 0.1.0-m1\nAndroid "+Build.VERSION.RELEASE+" • API "+Build.VERSION.SDK_INT+"\nABI: "+String.join(", ",Build.SUPPORTED_ABIS)+"\nApp memory class: "+am.getMemoryClass()+" MB\nDisplay: "+getResources().getDisplayMetrics().widthPixels+" × "+getResources().getDisplayMetrics().heightPixels+"\n");
        PackageInfo w=WebView.getCurrentWebViewPackage(); s.append("WebView: ").append(w==null?"Unavailable":w.packageName+" "+w.versionName).append("\n");
        for(String pkg:new String[]{"com.google.android.youtube.tv","com.google.android.youtube","com.brave.browser"}) { try { getPackageManager().getPackageInfo(pkg,0); s.append(pkg).append(": installed\n"); } catch(Exception e) { s.append(pkg).append(": not detected\n"); } }
        int homes=getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),0).size(); s.append("Home handlers: ").append(homes).append(" (not changed)\n\nVideo/audio decoders:\n");
        try { for(MediaCodecInfo c:new MediaCodecList(MediaCodecList.REGULAR_CODECS).getCodecInfos()) if(!c.isEncoder()) { for(String type:c.getSupportedTypes()) if(type.equals("video/avc")||type.equals("video/hevc")||type.equals("video/av01")||type.equals("audio/mp4a-latm")) s.append(type).append(" • ").append(c.getName()).append("\n"); } } catch(Exception e) { s.append("Codec inventory unavailable\n"); }
        TextView details=text(s.toString(),16,muted); details.setTextIsSelectable(true); content.addView(details);
    }
    private void message(String text) { Toast.makeText(this,text,Toast.LENGTH_LONG).show(); }
    @Override public void onBackPressed() {
        if(!screen.equals("home")) { home(); return; }
        new AlertDialog.Builder(this).setTitle("Leave Nook TV?").setPositiveButton("Exit",(d,n)->finish()).setNegativeButton("Stay",null).show();
    }
    @Override public boolean onKeyDown(int key,KeyEvent event) {
        if(video!=null&&prepared) { if(key==KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE||key==KeyEvent.KEYCODE_MEDIA_PLAY||key==KeyEvent.KEYCODE_MEDIA_PAUSE) { if(key==KeyEvent.KEYCODE_MEDIA_PLAY) video.start(); else if(key==KeyEvent.KEYCODE_MEDIA_PAUSE) video.pause(); else if(video.isPlaying()) video.pause(); else video.start(); saveProgress(); return true; } if(key==KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) { seek(10000); return true; } if(key==KeyEvent.KEYCODE_MEDIA_REWIND) { seek(-10000); return true; } }
        return super.onKeyDown(key,event);
    }
}
