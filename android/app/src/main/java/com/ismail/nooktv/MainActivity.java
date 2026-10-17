package com.ismail.nooktv;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.*;
import androidx.media3.common.*;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import java.util.concurrent.*;
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
    private ExoPlayer player;
    private PlayerView playerView;
    private ViewingStore viewing;
    private CatalogClient catalog;
    private String playingId="";
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    private int generation;
    private ScrollView homeScroll;
    private int homeScrollY;
    private Bitmap backgroundBitmap;
    private String backgroundIdentity="";
    private WebView web;
    private TextView playerStatus, pairInfo;
    private boolean prepared, videoStopped;
    private volatile boolean foreground;
    private List<String> addresses=new ArrayList<>();
    private String serverError="";
    private android.content.SharedPreferences history;
    private final int ink=Color.rgb(16,22,25), muted=Color.rgb(166,183,185);
    private int mint=Color.rgb(156,229,197);
    private final Runnable progress=new Runnable() { public void run() { saveProgress(); if(player!=null) handler.postDelayed(this,5000); } };

    @Override public void onCreate(Bundle b) {
        super.onCreate(b); getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        store=new DashboardStore(this); history=getSharedPreferences("nook",MODE_PRIVATE); viewing=new ViewingStore(this); catalog=new CatalogClient(this); server=new EditorServer(pairing,this);
        home();
    }
    @Override public void onResume() {
        super.onResume(); foreground=true;
        if(videoStopped) { videoStopped=false; home(); }
        try { addresses=server.start(); serverError=""; } catch(IOException e) { addresses=new ArrayList<>(); serverError="Editor could not start. Port 8765 may be in use."; }
        if(pairInfo!=null&&screen.equals("pair")) pairInfo.setText(addressText());
    }
    @Override public void onPause() {
        foreground=false; saveProgress(); if(player!=null) player.pause(); if(web!=null) web.onPause(); server.stop(); super.onPause();
    }
    @Override public void onStop() {
        if(player!=null) { releasePlayback(); videoStopped=true; }
        if(web!=null) { releaseWeb(); screen="home"; home(); }
        super.onStop();
    }
    @Override public void onDestroy() { handler.removeCallbacksAndMessages(null); releasePlayback(); server.stop(); io.shutdown(); super.onDestroy(); }

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
        if(screen.equals("home")&&homeScroll!=null) homeScrollY=homeScroll.getScrollY(); releasePlayback(); screen=name; pairInfo=null; generation++;
        mint=Color.parseColor(current().optString("accent","#9ce5c5")); root=vertical(); root.setBackgroundColor(ink); applyBackground(generation); root.setPadding(dp(40),dp(24),dp(40),dp(24));
        LinearLayout nav=new LinearLayout(this); nav.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo=text("nook",24,mint); logo.setTypeface(null,Typeface.BOLD); nav.addView(logo,new LinearLayout.LayoutParams(0,-2,1));
        nav.addView(action("Home","nav-home",this::home)); nav.addView(action("Apps","nav-apps",this::apps)); nav.addView(action("Watch","nav-watch",this::watch)); nav.addView(action("Pair PC","nav-pair",this::pair)); nav.addView(action("Settings","nav-settings",this::settings)); root.addView(nav);
        ScrollView scroll=new ScrollView(this); if(name.equals("home")) homeScroll=scroll; scroll.setFillViewport(true); content=vertical(); content.setPadding(0,dp(12),0,dp(20)); scroll.addView(content); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        content.addView(text(title,30,Color.WHITE)); content.addView(text(subtitle,15,muted)); setContentView(root);
    }
    private void section(String title) { TextView t=text(title,20,Color.WHITE); t.setPadding(0,dp(20),0,dp(8)); content.addView(t); }
    private LinearLayout row() { HorizontalScrollView s=new HorizontalScrollView(this); s.setHorizontalScrollBarEnabled(false); LinearLayout r=new LinearLayout(this); r.setPadding(0,dp(3),0,dp(6)); s.addView(r); content.addView(s); return r; }
    private void restore(String key) { root.post(()->{ View v=root.findViewWithTag(key); if(v!=null) v.requestFocus(); }); }
    private JSONObject current() { try { return store.read(); } catch(JSONException e) { throw new IllegalStateException(e); } }
    private void home() {
        String oldFocus=returnFocus.isEmpty()?focusKey:returnFocus; returnFocus=""; JSONObject config=current();
        page("home",config.optString("title"),"Your personal big screen | Preview 0.2");
        if(store.damaged()) content.addView(text("Your saved dashboard could not be read. It has been preserved; editing is disabled.",16,muted));
        JSONArray sections=config.optJSONArray("sections");
        for(int n=0;n<sections.length();n++) {
            JSONObject row=sections.optJSONObject(n); if(!row.optBoolean("visible")) continue;
            String id=row.optString("id");
            if(id.equals("resume")||id.equals("recent")) { historyRow(id.equals("resume")); continue; }
            if(id.equals("apps")) { section("Favorite apps"); LinearLayout r=row(); JSONArray a=config.optJSONArray("apps"); if(a.length()==0) r.addView(action("+ Pin your first app","pin-first",this::apps)); for(int i=0;i<a.length();i++) { String c=a.optString(i); r.addView(action(appLabel(c),"app-"+c,()->appOptions(c))); } }
            if(id.equals("saved")) { section("Saved for later"); LinearLayout r=row(); JSONArray a=config.optJSONArray("links"); if(a.length()==0) r.addView(action("+ Add a link","add-link",this::addLink)); for(int i=0;i<a.length();i++) { JSONObject l=a.optJSONObject(i); r.addView(action(l.optString("label"),"link-"+l.optString("id"),()->linkOptions(l))); } }
            if(id.equals("collections")) {
                JSONArray groups=config.optJSONArray("collections"); for(int i=0;i<groups.length();i++) {
                    JSONObject group=groups.optJSONObject(i); section(group.optString("label")); LinearLayout r=row(); JSONArray members=group.optJSONArray("items");
                    for(int j=0;j<members.length();j++) { String ref=members.optString(j); if(ref.startsWith("app:")) { String c=ref.substring(4); r.addView(action(appLabel(c),"group-"+group.optString("id")+"-"+j,()->appOptions(c))); }
                        else { JSONArray links=config.optJSONArray("links"); for(int k=0;k<links.length();k++) { JSONObject l=links.optJSONObject(k); if(l.optString("id").equals(ref)) r.addView(action(l.optString("label"),"group-"+group.optString("id")+"-"+j,()->linkOptions(l))); } }
                    } if(members.length()==0) r.addView(action("Add saved items","empty-"+group.optString("id"),()->collectionMembers(group.optString("id"))));
                }
                content.addView(action("+ Create a collection","collection-add",this::createCollection));
            }
            if(id.equals("samples")) { section("Playback and compatibility"); LinearLayout r=row();
                r.addView(action("MP4 sample","sample-mp4",()->play("https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4","Big Buck Bunny | MP4")));
                r.addView(action("HLS sample","sample-hls",()->play("https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8","Apple BipBop | HLS")));
                r.addView(action("DASH sample","sample-dash",()->play("https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd","Tears of Steel | DASH")));
                r.addView(action("YouTube","youtube",()->external("https://www.youtube.com/","youtube")));
                r.addView(action("Web test","sample-web",()->web("","Controlled web test"))); r.addView(action("Diagnostics","diagnostics",this::diagnostics));
            }
        }
        restore(oldFocus); homeScroll.post(()->homeScroll.scrollTo(0,homeScrollY));
    }
    private void historyRow(boolean resume) {
        try {
            JSONArray entries=viewing.ledger.snapshot().getJSONArray("entries"); boolean heading=false; LinearLayout r=null;
            for(int i=0;i<entries.length();i++) { JSONObject e=entries.getJSONObject(i); if(resume!=ViewingLedger.resumable(e)) continue;
                if(!resume&&e.optString("status").equals("watched")) continue;
                if(!heading) { section(resume?"Continue watching":"Recently opened"); r=row(); heading=true; }
                String label=e.optString("label")+(resume?" | Resume "+time((int)e.optLong("position")):" | "+(e.optString("status").equals("failed")?"Failed attempt":"Open again"));
                r.addView(action(label,"history-"+e.optString("id"),()->historyOptions(e)));
            }
        } catch(Exception e) { message("Could not read viewing history."); }
    }
    private void historyOptions(JSONObject e) {
        String id=e.optString("id"); new AlertDialog.Builder(this).setTitle(e.optString("label")).setItems(new String[]{ViewingLedger.resumable(e)?"Resume":"Open again","Start over","Mark watched","Remove"},(d,n)->{
            try { if(n==1) viewing.ledger.mark(id,false); if(n==2) viewing.ledger.mark(id,true); if(n==3) viewing.ledger.remove(id); persistViewing();
                if(n<2) { String route=e.optString("route"); if(route.equals("native")) play(e.optString("url"),e.optString("label"),id); else if(route.equals("web")) web(e.optString("url"),e.optString("label")); else external(e.optString("url"),route); } else home();
            } catch(Exception error) { message("Could not update viewing state."); }
        }).show();
    }
    private void persistViewing() { io.execute(()->{ try { viewing.persist(); } catch(Exception e) { handler.post(()->message("Viewing state could not be saved. Your previous committed state remains.")); } }); }
    private void applyBackground(int token) {
        String encoded=current().optString("background");
        if(encoded.isEmpty()) return;
        if(encoded.equals(backgroundIdentity)&&backgroundBitmap!=null) { setBackgroundBitmap(); return; }
        io.execute(()->{ try { byte[] bytes=android.util.Base64.decode(encoded.substring(encoded.indexOf(',')+1),android.util.Base64.DEFAULT); BitmapFactory.Options options=new BitmapFactory.Options(); options.inJustDecodeBounds=true; BitmapFactory.decodeByteArray(bytes,0,bytes.length,options); if(options.outWidth<1||options.outWidth>3840||options.outHeight<1||options.outHeight>2160) return; options.inJustDecodeBounds=false; options.inSampleSize=2; Bitmap bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.length,options); handler.post(()->{ if(token!=generation||bitmap==null) return; backgroundBitmap=bitmap; backgroundIdentity=encoded; setBackgroundBitmap(); }); } catch(Exception ignored) {} });
    }
    private void setBackgroundBitmap() { root.setBackground(new LayerDrawable(new android.graphics.drawable.Drawable[]{new BitmapDrawable(getResources(),backgroundBitmap),new ColorDrawable(0xdc101619)})); }
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
            else try { JSONObject c=current(); JSONArray next=new JSONArray(); for(int i=0;i<apps.length();i++) if(!component.equals(apps.optString(i))) next.put(apps.optString(i)); if(!isPinned) next.put(component); c.put("apps",next); if(isPinned) DashboardDocument.removeReference(c,"app:"+component); store.save(c,c.getLong("revision"),UUID.randomUUID().toString()); home(); } catch(Exception e) { message("Could not save favorite."); }
        }).show();
    }
    private void linkOptions(JSONObject link) {
        new AlertDialog.Builder(this).setTitle(link.optString("label")).setItems(new String[]{"Open • "+link.optString("route"),"Remove from saved links"},(d,n)->{
            if(n==0) { String u=link.optString("url"), label=link.optString("label"), route=link.optString("route"); if(route.equals("native")) play(u,label); else if(route.equals("web")) web(u,label); else if(route.equals("catalog")) details(Targets.imdb(u)); else external(u,route); }
            else try { JSONObject c=current(); JSONArray a=c.getJSONArray("links"), next=new JSONArray(); for(int i=0;i<a.length();i++) if(!a.getJSONObject(i).getString("id").equals(link.optString("id"))) next.put(a.getJSONObject(i)); c.put("links",next); DashboardDocument.removeReference(c,link.optString("id")); store.save(c,c.getLong("revision"),UUID.randomUUID().toString()); home(); } catch(Exception e) { message("Could not remove link."); }
        }).show();
    }
    private void addLink() {
        LinearLayout fields=vertical(); fields.setPadding(dp(24),dp(8),dp(24),0); EditText label=new EditText(this),url=new EditText(this); label.setHint("Label"); url.setHint("https://…"); url.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI); fields.addView(label); fields.addView(url);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Save a website or YouTube link").setView(fields).setPositiveButton("Save",null).setNegativeButton("Cancel",null).create();
        dialog.setOnShowListener(d->dialog.getButton(-1).setOnClickListener(v->{ try { String u=url.getText().toString().trim(); Targets.https(u); JSONObject c=current(),l=new JSONObject(); l.put("id",UUID.randomUUID().toString()).put("label",label.getText().toString().trim()).put("url",u).put("route",Targets.youtube(u)?"youtube":"browser"); c.getJSONArray("links").put(l); store.save(c,c.getLong("revision"),UUID.randomUUID().toString()); dialog.dismiss(); home(); } catch(Exception e) { url.setError("Enter a label and valid HTTPS URL."); } })); dialog.show();
    }
    private void external(String url,String route) {
        try { Targets.https(url); recordOpen(url,url,route); Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse(url));
            if(route.equals("youtube")) { for(String pkg:new String[]{"com.google.android.youtube.tv","com.google.android.youtube"}) { Intent candidate=new Intent(intent).setPackage(pkg); if(candidate.resolveActivity(getPackageManager())!=null) { startActivity(candidate); return; } } }
            startActivity(intent);
        } catch(Exception e) { message("No compatible app or browser is installed for this link."); }
    }
    private void play(String url,String label) { play(url,label,"direct:"+url); }
    private void play(String url,String label,String id) {
        try { Targets.https(url); } catch(Exception e) { message(e.getMessage()); return; }
        returnFocus=focusKey; page("player",label,"Native Media3 playback | Back returns to your dashboard.");
        playingUrl=url; playingLabel=label; playingId=id; prepared=false;
        try { viewing.ledger.opened(id,label,url,"native"); persistViewing(); } catch(Exception ignored) {}
        player=new ExoPlayer.Builder(this).build();
        playerView=new PlayerView(this); playerView.setPlayer(player); playerView.setUseController(true); playerView.setControllerAutoShow(true); playerView.setShowSubtitleButton(true);
        content.addView(playerView,new LinearLayout.LayoutParams(-1,dp(270))); playerStatus=text("Loading...",14,muted); content.addView(playerStatus); LinearLayout controls=row();
        controls.addView(action("Play / Pause","play-pause",()->{ if(player!=null) { if(player.isPlaying()) player.pause(); else player.play(); saveProgress(); } }));
        controls.addView(action("-10 sec","seek-back",()->seek(-10000))); controls.addView(action("+10 sec","seek-forward",()->seek(10000)));
        controls.addView(action("Audio / Subtitles","tracks",this::chooseTracks));
        controls.addView(action("Retry","retry-native",()->play(url,label,id))); controls.addView(action("Home","player-close",this::home));
        player.addListener(new Player.Listener() {
            @Override public void onPlaybackStateChanged(int state) {
                if(player==null) return;
                prepared=state==Player.STATE_READY||state==Player.STATE_ENDED;
                if(state==Player.STATE_READY) saveProgress();
                if(state==Player.STATE_ENDED) { saveProgress(); playerStatus.setText("Finished"); }
                if(state==Player.STATE_BUFFERING) playerStatus.setText("Buffering...");
            }
            @Override public void onPlayerError(PlaybackException error) {
                prepared=false; try { viewing.ledger.failed(id); persistViewing(); } catch(Exception ignored) {}
                if(playerStatus!=null) playerStatus.setText("Could not play this source ("+error.getErrorCodeName()+"). Retry or choose another link.");
            }
            @Override public void onIsPlayingChanged(boolean active) { if(prepared) saveProgress(); }
        });
        player.setMediaItem(new MediaItem.Builder().setUri(url).setMediaId(id).build());
        try { JSONObject entry=viewing.ledger.find(id); if(entry!=null&&ViewingLedger.resumable(entry)) player.seekTo(entry.optLong("position")); } catch(Exception ignored) {}
        player.prepare(); player.play(); handler.postDelayed(progress,5000); restore("play-pause");
    }
    private void chooseTracks() {
        if(player==null) return; List<String> names=new ArrayList<>(); List<Runnable> actions=new ArrayList<>();
        names.add("Subtitles off"); actions.add(()->player.setTrackSelectionParameters(player.getTrackSelectionParameters().buildUpon().setTrackTypeDisabled(C.TRACK_TYPE_TEXT,true).build()));
        for(Tracks.Group group:player.getCurrentTracks().getGroups()) { int type=group.getType(); if(type!=C.TRACK_TYPE_AUDIO&&type!=C.TRACK_TYPE_TEXT) continue;
            for(int i=0;i<group.length;i++) { if(!group.isTrackSupported(i)) continue; final int index=i; Format format=group.getTrackFormat(i); names.add((type==C.TRACK_TYPE_TEXT?"Subtitles: ":"Audio: ")+(format.label!=null?format.label:format.language!=null?format.language:"Track "+(i+1)));
                actions.add(()->player.setTrackSelectionParameters(player.getTrackSelectionParameters().buildUpon().setTrackTypeDisabled(type,false).setOverrideForType(new TrackSelectionOverride(group.getMediaTrackGroup(),index)).build()));
            }
        } new AlertDialog.Builder(this).setTitle("Available audio and subtitles").setItems(names.toArray(new String[0]),(d,n)->{ if(player!=null) actions.get(n).run(); }).show();
    }
    private void seek(int amount) { if(player!=null&&player.isCurrentMediaItemSeekable()) { player.seekTo(Math.max(0,player.getCurrentPosition()+amount)); saveProgress(); } }
    private void saveProgress() {
        if(player==null||!prepared) return;
        try { long position=player.getCurrentPosition(),duration=player.getDuration(); viewing.ledger.observe(playingId,position,duration,player.isCurrentMediaItemLive(),player.getPlaybackState()==Player.STATE_ENDED); persistViewing();
            if(playerStatus!=null) playerStatus.setText(duration>0?time((int)position)+" / "+time((int)duration):"Live or unknown duration | No completion percentage");
        } catch(Exception ignored) {}
    }
    private void web(String url,String label) {
        returnFocus=focusKey; recordOpen(url,label,"web"); page("web",label,"Progress unavailable • Back leaves the web player. No privileged bridge or Brave Shields.");
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
    private void releasePlayback() { saveProgress(); handler.removeCallbacks(progress); if(playerView!=null) { playerView.setPlayer(null); playerView=null; } if(player!=null) { player.release(); player=null; } prepared=false; playerStatus=null; releaseWeb(); }
    private String addressText() { return !serverError.isEmpty()?serverError:addresses.isEmpty()?"Connect your TV and PC to the same home network, then reopen this screen.":String.join("\n",addresses)+"\nKeep Nook open on the TV while editing. HTTP local connection; do not enter account passwords."; }
    private void pair() {
        page("pair","Bring your keyboard","Customize from a browser on your home network. Every pairing needs your approval here.");
        pairInfo=text(addressText(),19,mint); content.addView(pairInfo); content.addView(text("Pairing code:  "+pairing.begin(),36,Color.WHITE)); content.addView(text("Expires in 2 minutes. Five incorrect attempts lock this code.\nApprove only a browser you just requested. Codes and sessions are reset after an app restart.",16,muted));
        content.addView(action("Generate a new code","new-code",this::pair)); content.addView(action("Revoke all browsers","revoke",()->{ pairing.revoke(); message("Browser sessions revoked."); pair(); }));
    }
    @Override public void approve(String name) { handler.post(()->{ if(!foreground||!screen.equals("pair")) { pairing.cancel(); return; } new AlertDialog.Builder(this).setTitle("Allow “"+name+"” to edit?").setMessage("Approve only if you just submitted the code from your PC.").setPositiveButton("Allow",(d,n)->pairing.approve()).setNegativeButton("Deny",(d,n)->pairing.cancel()).setOnCancelListener(d->pairing.cancel()).show(); }); }
    @Override public byte[] asset(String name) throws IOException { try(InputStream in=getAssets().open(name); ByteArrayOutputStream out=new ByteArrayOutputStream()) { byte[] b=new byte[4096]; int n; while((n=in.read(b))>0) out.write(b,0,n); return out.toByteArray(); } }
    @Override public JSONObject config() throws Exception { if(!foreground) throw new IOException(); return store.read(); }
    @Override public JSONObject save(JSONObject input,long revision,String mutation) throws Exception { if(!foreground) throw new IOException(); DashboardDocument.validate(input); validateBackground(input); JSONObject result=store.save(input,revision,mutation); handler.post(()->{ if(screen.equals("home")) home(); }); return result; }
    private void settings() {
        page("settings","Settings","Compatibility preview. Your saved dashboard stays on this TV.");
        content.addView(action("Manage collections","settings-collections",this::manageCollections));
        content.addView(action("Metadata API key","settings-api",this::metadataKey));
        content.addView(action("Configure a web source","settings-source",this::configureSource));
        content.addView(action("Clear metadata cache","settings-cache",()->io.execute(()->{ catalog.clear(); handler.post(()->message("Metadata cache cleared. Your dashboard and viewing history stay.")); })));
        if(store.canRecover()) content.addView(action("Undo last dashboard import","settings-undo",()->new AlertDialog.Builder(this).setTitle("Restore the pre-import dashboard?").setMessage("Current dashboard changes will be replaced. A recovery snapshot is kept.").setPositiveButton("Restore",(d,n)->io.execute(()->{ try { store.undoImport(); handler.post(this::home); } catch(Exception e) { handler.post(()->message("Could not restore snapshot.")); } })).setNegativeButton("Cancel",null).show()));
        content.addView(action("Add a saved link","settings-add",this::addLink));
        content.addView(action(viewing.ledger.paused()?"Resume recording playback history":"Pause playback history","history-pause",()->{ try { viewing.ledger.pause(!viewing.ledger.paused()); persistViewing(); settings(); } catch(Exception e) { message("Could not change history setting."); } }));
        content.addView(action("Clear viewing history","history-clear",()->new AlertDialog.Builder(this).setTitle("Clear viewing history?").setMessage("Saved links and favorite apps stay.").setPositiveButton("Clear",(d,n)->{ try { viewing.ledger.clear(); persistViewing(); message("Viewing history cleared."); } catch(Exception e) { message("Could not clear history."); } }).setNegativeButton("Cancel",null).show()));
        content.addView(action("Revoke paired browsers","settings-revoke",()->{ pairing.revoke(); message("Browser sessions revoked."); }));
        content.addView(action("Device diagnostics","settings-diagnostics",this::diagnostics));
        content.addView(text("Collections, layout, backgrounds and dashboard backup/restore are available in the PC editor. Provider playback and target-TV acceptance are still unverified.",16,muted));
    }
    private void diagnostics() {
        page("diagnostics","Know your TV","No unique device identifiers, LAN addresses or session secrets are included below.");
        ActivityManager am=(ActivityManager)getSystemService(ACTIVITY_SERVICE); StringBuilder s=new StringBuilder("Nook TV 0.2.0-preview\nAndroid "+Build.VERSION.RELEASE+" • API "+Build.VERSION.SDK_INT+"\nABI: "+String.join(", ",Build.SUPPORTED_ABIS)+"\nApp memory class: "+am.getMemoryClass()+" MB\nDisplay: "+getResources().getDisplayMetrics().widthPixels+" × "+getResources().getDisplayMetrics().heightPixels+"\n");
        PackageInfo w=WebView.getCurrentWebViewPackage(); s.append("WebView: ").append(w==null?"Unavailable":w.packageName+" "+w.versionName).append("\n");
        for(String pkg:new String[]{"com.google.android.youtube.tv","com.google.android.youtube","com.brave.browser"}) { try { getPackageManager().getPackageInfo(pkg,0); s.append(pkg).append(": installed\n"); } catch(Exception e) { s.append(pkg).append(": not detected\n"); } }
        int homes=getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),0).size(); s.append("Home handlers: ").append(homes).append(" (not changed)\n\nVideo/audio decoders:\n");
        try { for(MediaCodecInfo c:new MediaCodecList(MediaCodecList.REGULAR_CODECS).getCodecInfos()) if(!c.isEncoder()) { for(String type:c.getSupportedTypes()) if(type.equals("video/avc")||type.equals("video/hevc")||type.equals("video/av01")||type.equals("audio/mp4a-latm")) s.append(type).append(" • ").append(c.getName()).append("\n"); } } catch(Exception e) { s.append("Codec inventory unavailable\n"); }
        TextView details=text(s.toString(),16,muted); details.setTextIsSelectable(true); content.addView(details);
    }
    private void message(String text) { Toast.makeText(this,text,Toast.LENGTH_LONG).show(); }
    private void recordOpen(String url,String label,String route) { if(url.isEmpty()) return; try { viewing.ledger.opened(route+":"+url,label,url,route); persistViewing(); } catch(Exception ignored) {} }
    private void createCollection() {
        EditText input=new EditText(this); input.setHint("Collection name");
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Create a collection").setView(input).setPositiveButton("Create",null).setNegativeButton("Cancel",null).create();
        dialog.setOnShowListener(d->dialog.getButton(-1).setOnClickListener(v->{ try { JSONObject c=current(); c.getJSONArray("collections").put(new JSONObject().put("id",UUID.randomUUID().toString()).put("label",input.getText().toString().trim()).put("items",new JSONArray())); store.save(c,c.getLong("revision"),UUID.randomUUID().toString()); dialog.dismiss(); home(); } catch(Exception e) { input.setError("Use a name of 1-80 characters; maximum 20 collections."); } })); dialog.show();
    }
    private void manageCollections() {
        page("collections","Your collections","Collections group your saved links, titles and favorite apps. Removing a collection keeps those saved items.");
        JSONArray groups=current().optJSONArray("collections");
        for(int i=0;i<groups.length();i++) { JSONObject g=groups.optJSONObject(i); String id=g.optString("id"); content.addView(action(g.optString("label"),"manage-"+id,()->new AlertDialog.Builder(this).setTitle(g.optString("label")).setItems(new String[]{"Choose items","Rename","Remove collection"},(d,n)->{ if(n==0) collectionMembers(id); else if(n==1) renameCollection(id); else new AlertDialog.Builder(this).setTitle("Remove this collection?").setMessage("Saved links and app favorites remain.").setPositiveButton("Remove",(x,y)->{ try { JSONObject c=current(); JSONArray old=c.getJSONArray("collections"),next=new JSONArray(); for(int k=0;k<old.length();k++) if(!old.getJSONObject(k).getString("id").equals(id)) next.put(old.getJSONObject(k)); c.put("collections",next); store.save(c,c.getLong("revision"),UUID.randomUUID().toString()); manageCollections(); } catch(Exception e) { message("Could not remove collection."); } }).setNegativeButton("Cancel",null).show(); }).show())); }
        content.addView(action("+ Create collection","manage-add",this::createCollection));
    }
    private void renameCollection(String id) {
        EditText input=new EditText(this); input.setHint("New name"); AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Rename collection").setView(input).setPositiveButton("Save",null).setNegativeButton("Cancel",null).create();
        dialog.setOnShowListener(d->dialog.getButton(-1).setOnClickListener(v->{ try { JSONObject c=current(); JSONArray groups=c.getJSONArray("collections"); for(int i=0;i<groups.length();i++) if(groups.getJSONObject(i).getString("id").equals(id)) groups.getJSONObject(i).put("label",input.getText().toString().trim()); store.save(c,c.getLong("revision"),UUID.randomUUID().toString()); dialog.dismiss(); manageCollections(); } catch(Exception e) { input.setError("Use a non-empty name up to 80 characters."); } })); dialog.show();
    }
    private void collectionMembers(String id) {
        try {
            JSONObject config=current(); JSONArray links=config.getJSONArray("links"),apps=config.getJSONArray("apps"),groups=config.getJSONArray("collections"); JSONObject group=null; for(int i=0;i<groups.length();i++) if(groups.getJSONObject(i).getString("id").equals(id)) group=groups.getJSONObject(i); if(group==null) return;
            List<String> labels=new ArrayList<>(),refs=new ArrayList<>(); for(int i=0;i<links.length();i++) { JSONObject l=links.getJSONObject(i); refs.add(l.getString("id")); labels.add(l.getString("label")); } for(int i=0;i<apps.length();i++) { refs.add("app:"+apps.getString(i)); labels.add(appLabel(apps.getString(i))); }
            boolean[] selected=new boolean[refs.size()]; JSONArray members=group.getJSONArray("items"); for(int i=0;i<selected.length;i++) for(int j=0;j<members.length();j++) if(refs.get(i).equals(members.getString(j))) selected[i]=true;
            if(refs.isEmpty()) { message("Save a link or pin an app first."); return; } JSONObject target=group;
            new AlertDialog.Builder(this).setTitle(group.getString("label")).setMultiChoiceItems(labels.toArray(new String[0]),selected,(d,n,checked)->selected[n]=checked).setPositiveButton("Save",(d,n)->{ try { JSONArray next=new JSONArray(); for(int i=0;i<selected.length;i++) if(selected[i]) next.put(refs.get(i)); target.put("items",next); store.save(config,config.getLong("revision"),UUID.randomUUID().toString()); home(); } catch(Exception e) { message("Dashboard changed. Reopen this collection and try again."); } }).setNegativeButton("Cancel",null).show();
        } catch(Exception e) { message("Could not read collection."); }
    }
    private void metadataKey() {
        EditText input=new EditText(this); input.setHint("Your OMDb API key"); input.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("OMDb metadata key").setMessage("Optional. Enables movie and series metadata; does not provide streaming availability. Stored only on this TV. Sent over HTTPS to OMDb; excluded from browser editing and backups.").setView(input).setPositiveButton("Save",null).setNeutralButton("Remove",(d,n)->{ getSharedPreferences("secrets",0).edit().remove("omdb").apply(); catalog.clear(); }).setNegativeButton("Cancel",null).create();
        dialog.setOnShowListener(d->dialog.getButton(-1).setOnClickListener(v->{ String key=input.getText().toString().trim(); if(!key.matches("[a-zA-Z0-9]{4,64}")) { input.setError("Check your API key."); return; } getSharedPreferences("secrets",0).edit().putString("omdb",key).apply(); io.execute(catalog::clear); dialog.dismiss(); message("Metadata key saved on TV."); })); dialog.show();
    }
    private void configureSource() {
        LinearLayout fields=vertical(); EditText label=new EditText(this),movie=new EditText(this),series=new EditText(this); label.setHint("Source name"); movie.setHint("HTTPS movie URL with {imdb}"); series.setHint("HTTPS series URL with {imdb}, {season}, {episode}"); fields.addView(label); fields.addView(movie); fields.addView(series);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Add your permitted web source").setMessage("No source is preverified. Use the site's documented IMDb embed URL contract. Playback remains a web route without timestamp resume.").setView(fields).setPositiveButton("Save",null).setNegativeButton("Cancel",null).create();
        dialog.setOnShowListener(d->dialog.getButton(-1).setOnClickListener(v->{ try { String name=label.getText().toString().trim(),m=movie.getText().toString().trim(),t=series.getText().toString().trim(); if(name.isEmpty()||name.length()>60) throw new IllegalArgumentException(); SourceTemplate.build(m,"tt1234567",0,0); if(!t.isEmpty()) SourceTemplate.build(t,"tt1234567",1,1); JSONArray sources=new JSONArray(history.getString("sources","[]")); if(sources.length()>=10) throw new IllegalArgumentException(); sources.put(new JSONObject().put("id",UUID.randomUUID().toString()).put("label",name).put("movie",m).put("series",t)); history.edit().putString("sources",sources.toString()).apply(); dialog.dismiss(); message("Source saved as unverified."); } catch(Exception e) { movie.setError("Check the name, HTTPS URL placeholders, or 10-source limit."); } })); dialog.show();
    }
    private void watch() {
        page("watch","Watch something good","Native movie and series browsing. Metadata requires your optional OMDb key in TV Settings.");
        LinearLayout form=new LinearLayout(this); EditText query=new EditText(this); query.setHint("Movie or series title"); query.setSingleLine(true); form.addView(query,new LinearLayout.LayoutParams(0,-2,1)); form.addView(action("Search","catalog-search",()->searchCatalog(query.getText().toString(),1))); content.addView(form);
        content.addView(text("Your saved movie and series titles are in Saved for later. No provider is assumed to work until tested.",16,muted));
    }
    private void searchCatalog(String query,int pageNumber) {
        page("search","Results for “"+query+"”","Loading metadata..."); int token=generation; TextView status=text("Searching...",16,muted); content.addView(status);
        io.execute(()->{ try { JSONObject result=catalog.search(query,pageNumber); handler.post(()->{ if(token!=generation) return; status.setText(result.optBoolean("nookStale")?"Offline cached metadata":"Metadata results; playback availability is separate."); JSONArray matches=result.optJSONArray("Search"); if(matches==null) return; for(int i=0;i<matches.length();i++) { JSONObject title=matches.optJSONObject(i); String id=title.optString("imdbID"); if(!id.matches("tt[0-9]{7,10}")) continue; content.addView(action(title.optString("Title")+" | "+title.optString("Year")+" | "+title.optString("Type"),"result-"+id,()->details(id))); } if(pageNumber<20&&result.optInt("totalResults")>pageNumber*10) content.addView(action("Next page","results-next",()->searchCatalog(query,pageNumber+1))); }); } catch(Exception e) { handler.post(()->{ if(token==generation) { status.setText(e.getMessage()); content.addView(action("Settings","catalog-settings",this::settings)); content.addView(action("Try again","catalog-retry",()->searchCatalog(query,pageNumber))); } }); } });
    }
    private void details(String id) {
        page("detail","Title details","Loading..."); int token=generation; TextView status=text("Loading metadata...",16,muted); content.addView(status);
        io.execute(()->{ try { JSONObject title=catalog.detail(id,0); handler.post(()->{ if(token!=generation) return; status.setText(title.optString("Title")+" ("+title.optString("Year")+")\n"+title.optString("Genre")+" | "+title.optString("Runtime")+"\n\n"+title.optString("Plot")+(title.optBoolean("nookStale")?"\nCached metadata":"")); boolean series=title.optString("Type").equals("series"); int season=history.getInt(id+"-season",1),episode=history.getInt(id+"-episode",1); TextView selection=text(series?"Selected S"+season+" E"+episode:"Movie",18,muted); content.addView(selection);
            if(series) content.addView(action("Choose season / episode","select-episode",()->chooseEpisode(id,title)));
            content.addView(action("Play / choose source","catalog-play",()->sourceOptions(id,title.optString("Title"),series)));
            content.addView(action("Save for later","catalog-save",()->{ try { JSONObject c=current(); String linkId="imdb-"+id; JSONArray links=c.getJSONArray("links"); for(int i=0;i<links.length();i++) if(links.getJSONObject(i).getString("id").equals(linkId)) { message("Already saved."); return; } links.put(new JSONObject().put("id",linkId).put("label",title.optString("Title")).put("url","https://www.imdb.com/title/"+id+"/").put("route","catalog")); store.save(c,c.getLong("revision"),UUID.randomUUID().toString()); message("Saved. Add it to a collection from Home or the PC editor."); } catch(Exception e) { message("Could not save title."); } }));
            content.addView(action("Open IMDb information","imdb-info",()->external("https://www.imdb.com/title/"+id+"/","browser")));
        }); } catch(Exception e) { handler.post(()->{ if(token==generation) { status.setText(e.getMessage()); content.addView(action("Settings","detail-settings",this::settings)); } }); } });
    }
    private void chooseEpisode(String id,JSONObject title) {
        LinearLayout fields=vertical(); NumberPicker season=new NumberPicker(this),episode=new NumberPicker(this); season.setMinValue(1); season.setMaxValue(Math.max(1,Math.min(100,title.optInt("totalSeasons",30)))); season.setValue(Math.min(season.getMaxValue(),history.getInt(id+"-season",1))); episode.setMinValue(1); episode.setMaxValue(1000); episode.setValue(Math.min(1000,history.getInt(id+"-episode",1))); fields.addView(text("Season",16,muted)); fields.addView(season); fields.addView(text("Episode (verify availability with the source)",16,muted)); fields.addView(episode);
        new AlertDialog.Builder(this).setTitle("Choose episode").setView(fields).setPositiveButton("Save",(d,n)->{ history.edit().putInt(id+"-season",season.getValue()).putInt(id+"-episode",episode.getValue()).apply(); details(id); }).setNegativeButton("Cancel",null).show();
    }
    private void sourceOptions(String id,String label,boolean series) {
        try { JSONArray sources=new JSONArray(history.getString("sources","[]")); List<JSONObject> available=new ArrayList<>(); List<String> names=new ArrayList<>(); for(int i=0;i<sources.length();i++) { JSONObject source=sources.getJSONObject(i); if(series&&source.optString("series").isEmpty()) continue; available.add(source); names.add(source.optString("label")+" | Unverified web source"); }
            if(available.isEmpty()) { message("Configure a permitted web source in TV Settings first. No provider is enabled by default."); return; }
            new AlertDialog.Builder(this).setTitle("Choose source | Progress unavailable").setItems(names.toArray(new String[0]),(d,n)->{ try { JSONObject source=available.get(n); int season=series?history.getInt(id+"-season",1):0,episode=series?history.getInt(id+"-episode",1):0; String url=SourceTemplate.build(source.getString(series?"series":"movie"),id,season,episode); history.edit().putString(id+"-source",source.getString("id")).apply(); web(url,label+(series?" | S"+season+" E"+episode:"")); } catch(Exception e) { message("This source does not support the selected title or episode contract."); } }).show();
        } catch(Exception e) { message("Source configuration is unreadable."); }
    }
    @Override public JSONObject restore(JSONObject input,long revision,String mutation) throws Exception { if(!foreground) throw new IOException(); JSONObject normalized=DashboardDocument.migrate(input); validateBackground(normalized); JSONObject result=store.restore(normalized,revision,mutation); handler.post(()->{ if(screen.equals("home")) home(); }); return result; }
    private void validateBackground(JSONObject input) throws Exception {
        String bg=input.getString("background"); if(bg.isEmpty()) return; byte[] bytes=android.util.Base64.decode(bg.substring(bg.indexOf(',')+1),android.util.Base64.DEFAULT); if(bytes.length>512000) throw new IllegalArgumentException(); BitmapFactory.Options options=new BitmapFactory.Options(); options.inJustDecodeBounds=true; BitmapFactory.decodeByteArray(bytes,0,bytes.length,options); if(options.outWidth<1||options.outWidth>3840||options.outHeight<1||options.outHeight>2160||!"image/jpeg".equals(options.outMimeType)) throw new IllegalArgumentException("Invalid image");
    }
    @Override public JSONObject inventory() throws Exception {
        if(!foreground) throw new IOException(); JSONArray apps=new JSONArray(); Set<String> components=new HashSet<>(); PackageManager pm=getPackageManager(); for(String category:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER}) for(ResolveInfo info:pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(category),0)) { String component=new ComponentName(info.activityInfo.packageName,info.activityInfo.name).flattenToString(); if(!info.activityInfo.packageName.equals(getPackageName())&&components.add(component)) apps.put(new JSONObject().put("component",component).put("label",info.loadLabel(pm).toString())); } return new JSONObject().put("apps",apps);
    }
    @Override public void onBackPressed() {
        if(!screen.equals("home")) { home(); return; }
        new AlertDialog.Builder(this).setTitle("Leave Nook TV?").setPositiveButton("Exit",(d,n)->finish()).setNegativeButton("Stay",null).show();
    }
    @Override public boolean onKeyDown(int key,KeyEvent event) {
        if(player!=null) {
            if(key==KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE||key==KeyEvent.KEYCODE_MEDIA_PLAY||key==KeyEvent.KEYCODE_MEDIA_PAUSE) { if(key==KeyEvent.KEYCODE_MEDIA_PLAY) player.play(); else if(key==KeyEvent.KEYCODE_MEDIA_PAUSE) player.pause(); else if(player.isPlaying()) player.pause(); else player.play(); saveProgress(); return true; }
            if(key==KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) { seek(10000); return true; } if(key==KeyEvent.KEYCODE_MEDIA_REWIND) { seek(-10000); return true; }
        }
        return super.onKeyDown(key,event);
    }
}

