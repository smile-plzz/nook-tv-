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
    private ReceivedMediaStore received;
    private String playingId="";
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    private int generation;
    private ScrollView homeScroll;
    private int homeScrollY;
    private final android.util.LruCache<String,Bitmap> appArtwork=new android.util.LruCache<String,Bitmap>(4*1024*1024){ @Override protected int sizeOf(String key,Bitmap value){return value.getByteCount();} };
    private String firstHomeTag="";
    private final List<LauncherApps.Entry> installedApps=new ArrayList<>();
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
        store=new DashboardStore(this); history=getSharedPreferences("nook",MODE_PRIVATE); focusKey=history.getString("lastFocus",""); homeScrollY=history.getInt("homeScroll",0); viewing=new ViewingStore(this); received=new ReceivedMediaStore(this); server=new EditorServer(pairing,this);
        io.execute(()->RetiredFeatureCleanup.run(this));
        if(getIntent().hasCategory(Intent.CATEGORY_HOME)) { focusKey=""; homeScrollY=0; } home();
    }
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent);setIntent(intent);if(intent.hasCategory(Intent.CATEGORY_HOME)){focusKey="";returnFocus="";homeScrollY=0;home();} }
    @Override public void onResume() {
        super.onResume(); foreground=true;
        if(videoStopped) { videoStopped=false; home(); }
        if(screen.equals("home")) refreshHomeApps();
        try { addresses=server.start(); serverError=""; } catch(IOException e) { addresses=new ArrayList<>(); serverError="Editor could not start. Port 8765 may be in use."; }
        if(pairInfo!=null&&screen.equals("pair")) pairInfo.setText(addressText());
    }
    @Override public void onPause() {
        foreground=false; history.edit().putString("lastFocus",returnFocus.isEmpty()?focusKey:returnFocus).putInt("homeScroll",screen.equals("home")&&homeScroll!=null?homeScroll.getScrollY():homeScrollY).apply(); saveProgress(); if(player!=null) player.pause(); if(web!=null) web.onPause(); server.stop(); super.onPause();
    }
    @Override public void onStop() {
        if(player!=null) { releasePlayback(); videoStopped=true; }
        if(web!=null) { releaseWeb(); screen="home"; home(); }
        super.onStop();
    }
    @Override public void onDestroy() { generation++; handler.removeCallbacksAndMessages(null); releasePlayback(); server.stop(); io.shutdown(); super.onDestroy(); }

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
        mint=Color.parseColor(current().optString("accent","#9ce5c5")); root=vertical(); root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff17233d,0xff10121f,0xff252039}));root.setClipChildren(false); applyBackground(generation); root.setPadding(dp(40),dp(24),dp(40),dp(24));
        LinearLayout nav=new LinearLayout(this); nav.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo=text("nook",24,mint); logo.setTypeface(null,Typeface.BOLD); nav.addView(logo,new LinearLayout.LayoutParams(0,-2,1));
        android.widget.TextClock clock=new android.widget.TextClock(this);clock.setFormat12Hour("h:mm a");clock.setFormat24Hour("HH:mm");clock.setTextColor(muted);clock.setTextSize(16);clock.setPadding(0,0,dp(24),0);nav.addView(clock);
        nav.addView(action("Home","nav-home",()->{focusKey="";returnFocus="";homeScrollY=0;home();})); nav.addView(action("Apps","nav-apps",this::apps)); nav.addView(action("Shared","nav-shared",this::shared)); nav.addView(action("Settings","nav-settings",this::settings)); root.addView(nav);
        ScrollView scroll=new ScrollView(this); if(name.equals("home")) homeScroll=scroll; scroll.setFillViewport(true); content=vertical(); content.setPadding(0,dp(12),0,dp(20)); scroll.addView(content); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        if(!title.isEmpty())content.addView(text(title,30,Color.WHITE)); if(!subtitle.isEmpty())content.addView(text(subtitle,15,muted)); setContentView(root);
    }
    private void section(String title) { TextView t=text(title,20,Color.WHITE); t.setPadding(0,dp(20),0,dp(8)); content.addView(t); }
    private LinearLayout row() { HorizontalScrollView s=new HorizontalScrollView(this); s.setHorizontalScrollBarEnabled(false); LinearLayout r=new LinearLayout(this); r.setPadding(0,dp(3),0,dp(6)); s.addView(r); content.addView(s); return r; }
    private void restore(String key) { root.post(()->{ View v=root.findViewWithTag(key); if(v!=null) v.requestFocus(); }); }
    private JSONObject current() { try { return store.read(); } catch(JSONException e) { throw new IllegalStateException(e); } }
    private LinearLayout homeAppsHost;
    private JSONObject homeConfig;
    private String homeRestore="";
    private void home() {
        String oldFocus=returnFocus.isEmpty()?focusKey:returnFocus;returnFocus=""; JSONObject config=current();
        page("home","",""); homeConfig=config;homeRestore=oldFocus;firstHomeTag="";
        TextView heading=text(config.optString("title","Home"),30,Color.WHITE);heading.setTypeface(null,Typeface.BOLD);content.addView(heading);
        if(store.damaged())content.addView(text("Saved data has been preserved. Editing is unavailable until it can be recovered.",15,muted));
        homeAppsHost=vertical();homeAppsHost.setTag("favorite-grid");content.addView(homeAppsHost);
        renderHomeApps(installedApps);
        receivedRow();
        for(int n=0;n<config.optJSONArray("sections").length();n++){
            JSONObject item=config.optJSONArray("sections").optJSONObject(n);if(!item.optBoolean("visible"))continue;String id=item.optString("id");
            if(id.equals("resume"))historyRow(true);
            if(id.equals("recent"))historyRow(false);
            if(id.equals("saved")&&config.optJSONArray("links").length()>0){section("Saved for later");LinearLayout r=row();JSONArray links=config.optJSONArray("links");for(int i=0;i<links.length();i++){JSONObject l=links.optJSONObject(i);r.addView(mediaTile(l.optString("label"),"Open saved link","link-"+l.optString("id"),()->openLink(l),()->linkOptions(l)));}}
            if(id.equals("collections")){JSONArray groups=config.optJSONArray("collections");for(int i=0;i<groups.length();i++){JSONObject group=groups.optJSONObject(i);JSONArray members=group.optJSONArray("items");if(members.length()==0)continue;section(group.optString("label"));LinearLayout r=row();for(int j=0;j<members.length();j++){String ref=members.optString(j);if(ref.startsWith("app:")){String component=ref.substring(4);r.addView(mediaTile(appLabel(component),"Open app","group-"+group.optString("id")+"-"+j,()->launchApp(component),()->appOptions(component)));}else{JSONArray links=config.optJSONArray("links");for(int k=0;k<links.length();k++){JSONObject l=links.optJSONObject(k);if(l.optString("id").equals(ref))r.addView(mediaTile(l.optString("label"),"Open saved link","group-"+group.optString("id")+"-"+j,()->openLink(l),()->linkOptions(l)));}}}}}
        }
        root.post(()->{View found=root.findViewWithTag(oldFocus);if(found!=null)found.requestFocus();else focusFirstHome();homeScroll.scrollTo(0,homeScrollY);});
        refreshHomeApps();
    }
    private int gridColumns(){int width=(int)(getResources().getDisplayMetrics().widthPixels/getResources().getDisplayMetrics().density)-80;return Math.max(2,Math.min(6,width/140));}
    private int tileWidth(){int width=getResources().getDisplayMetrics().widthPixels-dp(80);return (width-dp(16)*gridColumns())/gridColumns();}
    private void addGridTile(LinearLayout host,LauncherTile tile,int index){
        int cols=gridColumns();LinearLayout r;if(index%cols==0){r=new LinearLayout(this);r.setClipChildren(false);r.setPadding(0,dp(8),0,dp(10));host.addView(r);}else r=(LinearLayout)host.getChildAt(host.getChildCount()-1);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(tileWidth(),dp(136));p.setMargins(dp(4),0,dp(12),0);r.addView(tile,p);
    }
    private void trackTile(LauncherTile tile,String tag){tile.setTag(tag);View.OnFocusChangeListener visual=tile.getOnFocusChangeListener();tile.setOnFocusChangeListener((v,f)->{if(visual!=null)visual.onFocusChange(v,f);if(f)focusKey=tag;});}
    private LauncherTile makeTile(String label,String detail,String symbol,String tag,int color,Runnable open,Runnable more){
        LauncherTile tile=new LauncherTile(this,label,detail,symbol,color,tileWidth(),!history.getBoolean("reducedMotion",false),open,more);trackTile(tile,tag);return tile;
    }
    private View mediaTile(String label,String detail,String tag,Runnable open,Runnable more){LauncherTile tile=makeTile(label,detail,"play",tag,0xff34384b,open,more);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(214),dp(136));p.setMargins(dp(4),dp(6),dp(16),dp(8));tile.setLayoutParams(p);return tile;}
    private void focusFirstHome(){View first=root.findViewWithTag(firstHomeTag);if(first!=null)first.requestFocus();}
    private void renderHomeApps(List<LauncherApps.Entry> available){
        if(homeAppsHost==null||!screen.equals("home"))return;String desired=homeRestore;View focused=root.findFocus();if(!firstHomeTag.isEmpty()&&focused!=null&&focused.getTag() instanceof String)desired=(String)focused.getTag();homeAppsHost.removeAllViews();firstHomeTag="";int index=0;JSONArray pinned=homeConfig.optJSONArray("apps");List<String> favorites=new ArrayList<>();
        if(pinned.length()>0){for(int i=0;i<pinned.length();i++)favorites.add(pinned.optString(i));}
        else for(LauncherApps.Entry app:available){if(favorites.size()==4)break;favorites.add(app.component);}
        boolean youtube=false;for(String c:favorites)if(c.startsWith("com.google.android.youtube"))youtube=true;
        for(String c:favorites){LauncherApps.Entry found=null;for(LauncherApps.Entry app:available)if(app.component.equals(c)){found=app;break;}String tag="app-"+c;String label=found==null?appLabel(c):found.label;
            LauncherTile tile=makeTile(label,found==null?"Not installed":"",label,tag,0xff343e59,()->launchApp(c),()->appOptions(c));if(firstHomeTag.isEmpty())firstHomeTag=tag;addGridTile(homeAppsHost,tile,index++);if(found!=null)loadAppArtwork(tile,found);
        }
        if(pinned.length()==0&&!youtube){LauncherTile tile=makeTile("YouTube","Open website","play","tile-youtube",0xff883e56,()->external("https://www.youtube.com/","youtube"),null);if(firstHomeTag.isEmpty())firstHomeTag="tile-youtube";addGridTile(homeAppsHost,tile,index++);}
        addGridTile(homeAppsHost,makeTile("From iPhone","Sharing options","phone","tile-phone",0xff414674,this::phone,null),index++);
        addGridTile(homeAppsHost,makeTile("All Apps","Browse & organize","apps","tile-apps",0xff3c5268,this::apps,null),index++);
        addGridTile(homeAppsHost,makeTile("Shared & Recent","Return to your media","recent","tile-shared",0xff3e5264,this::shared,null),index++);
        // Drive is a deliberate preview destination, never a fake connected account.
        if(history.getBoolean("showDrive",false))addGridTile(homeAppsHost,makeTile("Google Drive","Not connected","drive","tile-drive",0xff425b5a,this::drive,null),index++);
        View target=root.findViewWithTag(desired);if(target!=null)target.requestFocus();else focusFirstHome();
    }
    private void refreshHomeApps(){
        if(!screen.equals("home"))return;int token=generation;io.execute(()->{List<LauncherApps.Entry> found=LauncherApps.discover(this);handler.post(()->{if(token!=generation||!screen.equals("home"))return;installedApps.clear();installedApps.addAll(found);renderHomeApps(found);});});
    }
    private void loadAppArtwork(LauncherTile tile,LauncherApps.Entry app){
        Bitmap cached=appArtwork.get(app.component);if(cached!=null){tile.artwork(new BitmapDrawable(getResources(),cached));return;}int token=generation;
        io.execute(()->{try{android.graphics.drawable.Drawable image=app.info.loadBanner(getPackageManager());if(image==null)image=app.info.loadIcon(getPackageManager());int w=image.getIntrinsicWidth(),h=image.getIntrinsicHeight();float scale=Math.min(1f,Math.min(224f/Math.max(1,w),112f/Math.max(1,h)));w=Math.max(1,(int)(w*scale));h=Math.max(1,(int)(h*scale));Bitmap bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);image.setBounds(0,0,w,h);image.draw(new android.graphics.Canvas(bitmap));appArtwork.put(app.component,bitmap);handler.post(()->{if(token==generation)tile.artwork(new BitmapDrawable(getResources(),bitmap));});}catch(Exception ignored){}});
    }
    private void launchApp(String component){
        try{ComponentName target=ComponentName.unflattenFromString(component);if(target==null)throw new IllegalArgumentException();startActivity(new Intent(Intent.ACTION_MAIN).setComponent(target).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}
        catch(Exception e){new AlertDialog.Builder(this).setTitle("App unavailable").setMessage("This app may have been removed. You can update your favorites in All Apps.").setPositiveButton("All Apps",(d,n)->apps()).setNegativeButton("Close",null).show();}
    }
    private void phone(){
        page("phone","From iPhone","Send a video copy, then watch it without your phone.");
        content.addView(text("1. Pair Safari on your iPhone",22,Color.WHITE));content.addView(text("Open the TV address on the same home network, enter the pairing code and approve the request on TV.",16,muted));
        content.addView(action("Pair a browser","phone-pair",this::pair));
        content.addView(text("2. Choose Send a video in the browser",22,Color.WHITE));content.addView(text("Select an MP4 or MOV from Photos or Files. Keep Nook open until Saved on TV appears. Maximum 512 MiB per video; 1 GiB retained storage. No automatic playback.",16,muted));
        content.addView(text("3. Open Shared & Recent",22,Color.WHITE));content.addView(text("Saved copies stay on this TV after the phone disconnects. Playback depends on the TV's video codec support. Clear history keeps files; Delete saved copy removes a file.",16,muted));
        content.addView(action("Shared & Recent","phone-recent",this::shared));content.addView(action("Manage saved videos","phone-storage",this::mediaStorage));
        content.addView(text("AirPlay and live mirroring are separate. This build receives saved file copies through Safari, not system AirPlay. You can still open an existing receiver app from All Apps.",15,muted));content.addView(action("Open All Apps","phone-apps",this::apps));restore("phone-pair");
    }
    private void receivedRow(){
        try{JSONArray items=received.snapshot().getJSONArray("items");if(items.length()==0)return;section("Saved on this TV");LinearLayout r=row();for(int i=0;i<items.length();i++){JSONObject item=items.getJSONObject(i);JSONObject progress=viewing.ledger.find("local:"+item.getString("id"));String detail=!item.optBoolean("available")?"Unavailable":progress!=null&&ViewingLedger.resumable(progress)?"Resume "+time((int)progress.optLong("position")):"Play saved copy";r.addView(mediaTile(item.getString("label"),detail,"received-"+item.getString("id"),()->playReceived(item.optString("id")),()->receivedOptions(item)));}}catch(Exception e){content.addView(text("Saved video storage could not be read.",16,muted));}
    }
    private void shared(){
        page("shared","Shared & Recent","Saved on this TV, plus your links and observed playback.");
        int before=content.getChildCount();receivedRow();historyRow(true);historyRow(false);JSONArray links=current().optJSONArray("links");if(links.length()>0){section("Saved links");LinearLayout r=row();for(int i=0;i<links.length();i++){JSONObject l=links.optJSONObject(i);r.addView(mediaTile(l.optString("label"),"Open saved link","shared-"+l.optString("id"),()->openLink(l),()->linkOptions(l)));}}
        if(before==content.getChildCount()){content.addView(text("Your media starts here",24,Color.WHITE));content.addView(text("Pair Safari or a PC browser to send a video copy or save a link. AirPlay sessions are not imported.",16,muted));}
        content.addView(action("From iPhone","shared-phone",this::phone));content.addView(action("Manage saved videos","shared-storage",this::mediaStorage));content.addView(action("Manage viewing history","shared-manage",this::allHistory));restore("shared-phone");
    }
    private void playReceived(String id){
        try{JSONObject item=received.find(id);if(item==null)throw new FileNotFoundException();String uri=Uri.fromFile(received.playable(id)).toString();playNative(uri,item.getString("label"),"local:"+id,true);}
        catch(Exception e){message("This saved copy is unavailable. Send it again or remove its entry in saved-video storage.");}
    }
    private void receivedOptions(JSONObject item){
        String id=item.optString("id");new AlertDialog.Builder(this).setTitle(item.optString("label")).setItems(new String[]{"Play / Resume","Start over","Delete saved copy"},(d,n)->{if(n==0)playReceived(id);else if(n==1){try{viewing.ledger.mark("local:"+id,false);persistViewing();playReceived(id);}catch(Exception e){message("Could not reset progress.");}}else confirmDeleteMedia(item);}).show();
    }
    private void confirmDeleteMedia(JSONObject item){
        new AlertDialog.Builder(this).setTitle("Delete saved copy?").setMessage(item.optString("label")+" will be removed from this TV. Your phone's original is not changed.").setPositiveButton("Delete",(d,n)->io.execute(()->{try{received.delete(item.getString("id"));viewing.ledger.remove("local:"+item.getString("id"));viewing.persist();handler.post(this::mediaStorage);}catch(Exception e){handler.post(()->message("Could not complete deletion. Wait for any transfer and check storage again."));}})).setNegativeButton("Cancel",null).show();
    }
    private String sizeLabel(long bytes){return String.format(Locale.ROOT,"%.1f MiB",bytes/(1024d*1024d));}
    private void mediaStorage(){
        page("media-storage","Saved video storage","These are retained copies. Clearing viewing history does not remove them.");
        try{JSONObject snapshot=received.snapshot();content.addView(text(sizeLabel(snapshot.getLong("usedBytes"))+" used of "+sizeLabel(snapshot.getLong("quotaBytes"))+" · up to 50 videos",17,muted));if(snapshot.optBoolean("damaged"))content.addView(text("The media index is unreadable and has been preserved. New transfers are blocked.",17,muted));
            JSONArray items=snapshot.getJSONArray("items");for(int i=0;i<items.length();i++){JSONObject item=items.getJSONObject(i);content.addView(action(item.getString("label")+" · "+sizeLabel(item.getLong("size"))+(item.optBoolean("available")?"":" · Unavailable"),"storage-"+item.getString("id"),()->receivedOptions(item)));}
            if(items.length()==0)content.addView(text("No saved video copies yet.",19,muted));
            if(snapshot.optLong("unindexedBytes")>0)content.addView(action("Clean unindexed transfer storage","media-clean",()->new AlertDialog.Builder(this).setTitle("Remove unindexed transfer data?").setMessage("Only incomplete or unlisted transfer files will be removed. Listed saved videos stay.").setPositiveButton("Clean",(d,n)->io.execute(()->{try{received.cleanUnindexed();handler.post(this::mediaStorage);}catch(Exception e){handler.post(()->message("Wait for any transfer to finish and try again."));}})).setNegativeButton("Cancel",null).show()));
        }catch(Exception e){content.addView(text("Storage status unavailable.",18,muted));}
        content.addView(action("Refresh storage","media-refresh",this::mediaStorage));restore("media-refresh");
    }
    private void drive(){
        page("drive","Google Drive","Not connected");
        content.addView(text("Drive media access is planned",23,Color.WHITE));content.addView(text("This prototype cannot sign into Drive, browse your files or play private Drive videos. Account authorization and selected-file playback are a later integration. Your Google password is never requested here.",16,muted));content.addView(action("Back Home","drive-home",this::home));restore("drive-home");
    }
    private void openLink(JSONObject link){String url=link.optString("url"),label=link.optString("label"),route=link.optString("route");if(route.equals("native"))play(url,label);else if(route.equals("web"))web(url,label);else external(url,route);}
    private void openHistory(JSONObject entry){String route=entry.optString("route");if(route.equals("local")){playReceived(entry.optString("id").replaceFirst("^local:",""));return;}if(route.equals("native"))play(entry.optString("url"),entry.optString("label"),entry.optString("id"));else if(route.equals("web"))web(entry.optString("url"),entry.optString("label"));else external(entry.optString("url"),route);}
    private void historyRow(boolean resume) {
        try {
            JSONArray entries=viewing.ledger.snapshot().getJSONArray("entries"); boolean heading=false; LinearLayout r=null;
            for(int i=0;i<entries.length();i++) { JSONObject e=entries.getJSONObject(i); if(resume!=ViewingLedger.resumable(e)) continue;
                if(!resume&&e.optString("status").equals("watched")) continue;
                if(!heading) { section(resume?"Continue Watching":"Recently opened"); r=row(); heading=true; }
                String label=e.optString("label")+(resume?" | Resume "+time((int)e.optLong("position")):" | "+(e.optString("status").equals("failed")?"Failed attempt":"Open again"));
                r.addView(mediaTile(e.optString("label"),resume?"Resume "+time((int)e.optLong("position")):(e.optString("status").equals("failed")?"Retry opening":"Open again"),"history-"+e.optString("id"),()->openHistory(e),()->historyOptions(e)));
            }
        } catch(Exception e) { message("Could not read viewing history."); }
    }
    private void historyOptions(JSONObject e) {
        String id=e.optString("id"); new AlertDialog.Builder(this).setTitle(e.optString("label")).setItems(new String[]{ViewingLedger.resumable(e)?"Resume":"Open again","Start over","Mark watched","Remove"},(d,n)->{
            try { if(n==1) viewing.ledger.mark(id,false); if(n==2) viewing.ledger.mark(id,true); if(n==3) viewing.ledger.remove(id); persistViewing();
                if(n<2) openHistory(e); else home();
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
        page("apps","All Apps","OK opens an app. Hold OK for options, or choose Edit favorites.");
        LinearLayout host=vertical();content.addView(host);int token=generation;
        content.addView(action("Edit favorites","apps-edit",this::editFavorites));
        io.execute(()->{List<LauncherApps.Entry> list=LauncherApps.discover(this);handler.post(()->{if(token!=generation)return;int index=0;for(LauncherApps.Entry app:list){LauncherTile tile=makeTile(app.label,"",app.label,"app-"+app.component,0xff343e59,()->launchApp(app.component),()->appOptions(app.component));addGridTile(host,tile,index++);loadAppArtwork(tile,app);}if(list.isEmpty())host.addView(text("No launchable apps were reported by this device.",18,muted));if(!list.isEmpty())restore("app-"+list.get(0).component);});});
    }
    private void editFavorites(){
        page("favorites","Edit favorites","Select an app for pinning or movement options. An empty list uses suggested installed apps.");
        LinearLayout host=vertical();content.addView(host);int token=generation;
        io.execute(()->{List<LauncherApps.Entry> list=LauncherApps.discover(this);handler.post(()->{if(token!=generation)return;JSONArray pinned=current().optJSONArray("apps");Set<String> shown=new HashSet<>();for(int i=0;i<pinned.length();i++){String c=pinned.optString(i);shown.add(c);host.addView(action(appLabel(c)+" · Pinned","edit-"+c,()->appOptions(c)));}for(LauncherApps.Entry app:list)if(shown.add(app.component))host.addView(action(app.label,"edit-"+app.component,()->appOptions(app.component)));});});
    }
    private void appOptions(String component) {
        boolean pinned=false; JSONArray apps=current().optJSONArray("apps"); for(int i=0;i<apps.length();i++) if(component.equals(apps.optString(i))) pinned=true;
        final boolean isPinned=pinned;
        new AlertDialog.Builder(this).setTitle(appLabel(component)).setItems(new String[]{"Open",pinned?"Unpin from Home":"Pin to Home","Move earlier","Move later"},(d,n)->{
            if(n==0) launchApp(component);
            else if(n>=2) moveFavorite(component,n==2?-1:1);
            else try { JSONObject c=current(); JSONArray next=new JSONArray(); for(int i=0;i<apps.length();i++) if(!component.equals(apps.optString(i))) next.put(apps.optString(i)); if(!isPinned) next.put(component); c.put("apps",next); if(isPinned) DashboardDocument.removeReference(c,"app:"+component); store.save(c,c.getLong("revision"),UUID.randomUUID().toString()); home(); } catch(Exception e) { message("Could not save favorite."); }
        }).show();
    }
    private void moveFavorite(String component,int direction){
        try{JSONObject config=current();JSONArray pinned=config.getJSONArray("apps");List<String> ordered=new ArrayList<>();for(int i=0;i<pinned.length();i++)ordered.add(pinned.optString(i));int index=ordered.indexOf(component);if(index<0){message("Pin this app before reordering it.");return;}int next=Math.max(0,Math.min(ordered.size()-1,index+direction));Collections.swap(ordered,index,next);config.put("apps",new JSONArray(ordered));store.save(config,config.getLong("revision"),UUID.randomUUID().toString());home();}catch(Exception e){message("Could not move favorite.");}
    }
    private void linkOptions(JSONObject link) {
        new AlertDialog.Builder(this).setTitle(link.optString("label")).setItems(new String[]{"Open • "+link.optString("route"),"Remove from saved links"},(d,n)->{
            if(n==0) { String u=link.optString("url"), label=link.optString("label"), route=link.optString("route"); if(route.equals("native")) play(u,label); else if(route.equals("web")) web(u,label); else external(u,route); }
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
        playNative(url,label,id,false);
    }
    private void playNative(String url,String label,String id,boolean local) {
        returnFocus=focusKey; page("player",label,"Native Media3 playback | Back returns to your dashboard.");
        playingUrl=url; playingLabel=label; playingId=id; prepared=false;
        try { if(local)viewing.ledger.openedLocal(id.substring(6),label);else viewing.ledger.opened(id,label,url,"native"); persistViewing(); } catch(Exception ignored) {}
        player=new ExoPlayer.Builder(this).setLoadControl(new androidx.media3.exoplayer.DefaultLoadControl.Builder().setBufferDurationsMs(5000,20000,1000,2000).setTargetBufferBytes(24*1024*1024).build()).setAudioAttributes(androidx.media3.common.AudioAttributes.DEFAULT,true).setHandleAudioBecomingNoisy(true).build();
        playerView=new PlayerView(this); playerView.setPlayer(player); playerView.setUseController(true); playerView.setControllerAutoShow(true); playerView.setShowSubtitleButton(true);
        content.addView(playerView,new LinearLayout.LayoutParams(-1,dp(270))); playerStatus=text("Loading...",14,muted); content.addView(playerStatus); LinearLayout controls=row();
        controls.addView(action("Play / Pause","play-pause",()->{ if(player!=null) { if(player.isPlaying()) player.pause(); else player.play(); saveProgress(); } }));
        controls.addView(action("-10 sec","seek-back",()->seek(-10000))); controls.addView(action("+10 sec","seek-forward",()->seek(10000)));
        controls.addView(action("Audio / Subtitles","tracks",this::chooseTracks));
        controls.addView(action("Retry","retry-native",()->{if(local)playReceived(id.substring(6));else play(url,label,id);})); controls.addView(action("Home","player-close",this::home));
        player.addListener(new Player.Listener() {
            @Override public void onPlaybackStateChanged(int state) {
                if(player==null) return;
                // Once a timeline is ready, buffering during a seek must not discard its position.
                if(state==Player.STATE_READY||state==Player.STATE_ENDED)prepared=true;
                if(state==Player.STATE_READY) saveProgress();
                if(state==Player.STATE_ENDED) { saveProgress(); playerStatus.setText("Finished"); }
                if(state==Player.STATE_BUFFERING) playerStatus.setText("Buffering...");
            }
            @Override public void onPlayerError(PlaybackException error) {
                prepared=false; try { viewing.ledger.failed(id); persistViewing(); } catch(Exception ignored) {}
                if(playerStatus!=null) playerStatus.setText("Could not play this source ("+error.getErrorCodeName()+"). Retry or choose another link.");
            }
            @Override public void onPositionDiscontinuity(Player.PositionInfo before,Player.PositionInfo after,int reason) { if(reason==Player.DISCONTINUITY_REASON_SEEK)saveProgress(); }
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
    private void releasePlayback() { saveProgress(); prepared=false; handler.removeCallbacks(progress); if(playerView!=null) { playerView.setPlayer(null); playerView=null; } ExoPlayer closing=player;player=null;if(closing!=null)closing.release();playerStatus=null;releaseWeb(); }
    private String addressText() { return !serverError.isEmpty()?serverError:addresses.isEmpty()?"Connect your TV and PC to the same home network, then reopen this screen.":String.join("\n",addresses)+"\nKeep Nook open on the TV while editing. HTTP local connection; do not enter account passwords."; }
    private void pair() {
        page("pair","Bring your keyboard","Customize from a browser on your home network. Every pairing needs your approval here.");
        pairInfo=text(addressText(),19,mint); content.addView(pairInfo); content.addView(text("Pairing code:  "+pairing.begin(),36,Color.WHITE)); content.addView(text("Expires in 2 minutes. Five incorrect attempts lock this code.\nApprove only a browser you just requested. Codes and sessions are reset after an app restart.",16,muted));
        content.addView(action("Generate a new code","new-code",this::pair)); content.addView(action("Revoke all browsers","revoke",()->{ pairing.revoke(); message("Browser sessions revoked."); pair(); }));
    }
    @Override public void approve(String name) { handler.post(()->{ if(!foreground||!screen.equals("pair")) { pairing.cancel(); return; } new AlertDialog.Builder(this).setTitle("Allow “"+name+"” to edit?").setMessage("Approve only if you just submitted the code from your PC.").setPositiveButton("Allow",(d,n)->pairing.approve()).setNegativeButton("Deny",(d,n)->pairing.cancel()).setOnCancelListener(d->pairing.cancel()).show(); }); }
    @Override public byte[] asset(String name) throws IOException { try(InputStream in=getAssets().open(name); ByteArrayOutputStream out=new ByteArrayOutputStream()) { byte[] b=new byte[4096]; int n; while((n=in.read(b))>0) out.write(b,0,n); return out.toByteArray(); } }
    @Override public JSONObject media() throws Exception { if(!foreground)throw new IOException();return received.snapshot(); }
    @Override public JSONObject upload(InputStream input,long length,String name,String id,java.util.function.BooleanSupplier allowed) throws Exception {
        if(!foreground)throw new IOException();JSONObject item=received.receive(input,length,name,id,()->foreground&&allowed.getAsBoolean());handler.post(()->{if(foreground&&(screen.equals("home")||screen.equals("shared"))) {if(screen.equals("shared"))shared();else home();}message("Video saved on TV. Open Shared & Recent to play.");});return item;
    }
    @Override public JSONObject config() throws Exception { if(!foreground) throw new IOException(); return store.read(); }
    @Override public JSONObject save(JSONObject input,long revision,String mutation) throws Exception { if(!foreground) throw new IOException(); DashboardDocument.validate(input); validateBackground(input); JSONObject result=store.save(input,revision,mutation); handler.post(()->{ if(screen.equals("home")) home(); }); return result; }
    private void settings() {
        page("settings","Settings","Make Nook feel like home.");
        content.addView(action("Default Home setup","settings-home",this::homeSetup));
        content.addView(action("Customize from PC or iPhone","settings-pair",this::pair));
        content.addView(action("Edit favorite apps","settings-apps",this::editFavorites));
        content.addView(action("From iPhone","settings-phone",this::phone));
        content.addView(action("Google Drive · Not connected","settings-drive",this::drive));
        content.addView(action(history.getBoolean("showDrive",false)?"Hide Drive tile":"Show Drive tile","settings-drive-tile",()->{history.edit().putBoolean("showDrive",!history.getBoolean("showDrive",false)).apply();settings();}));
        content.addView(action(history.getBoolean("reducedMotion",false)?"Enable focus animation":"Reduce motion","settings-motion",()->{history.edit().putBoolean("reducedMotion",!history.getBoolean("reducedMotion",false)).apply();settings();}));
        content.addView(action("Playback checks","settings-samples",this::samples));
        content.addView(action("Manage collections","settings-collections",this::manageCollections));
        content.addView(action("Viewing history","settings-history",this::allHistory));
        content.addView(action("Saved video storage","settings-media",this::mediaStorage));
        if(store.canRecover()) content.addView(action("Undo last dashboard import","settings-undo",()->new AlertDialog.Builder(this).setTitle("Restore the pre-import dashboard?").setMessage("Current dashboard changes will be replaced. A recovery snapshot is kept.").setPositiveButton("Restore",(d,n)->io.execute(()->{ try { store.undoImport(); handler.post(this::home); } catch(Exception e) { handler.post(()->message("Could not restore snapshot.")); } })).setNegativeButton("Cancel",null).show()));
        content.addView(action("Add a saved link","settings-add",this::addLink));
        content.addView(action(viewing.ledger.paused()?"Resume recording playback history":"Pause playback history","history-pause",()->{ try { viewing.ledger.pause(!viewing.ledger.paused()); persistViewing(); settings(); } catch(Exception e) { message("Could not change history setting."); } }));
        content.addView(action("Clear viewing history","history-clear",()->new AlertDialog.Builder(this).setTitle("Clear viewing history?").setMessage("Saved links and favorite apps stay.").setPositiveButton("Clear",(d,n)->{ try { viewing.ledger.clear(); persistViewing(); message("Viewing history cleared."); } catch(Exception e) { message("Could not clear history."); } }).setNegativeButton("Cancel",null).show()));
        content.addView(action("Revoke paired browsers","settings-revoke",()->{ pairing.revoke(); message("Browser sessions revoked."); }));
        content.addView(action("Device diagnostics","settings-diagnostics",this::diagnostics));
        content.addView(text("Collections, layout, backgrounds and dashboard backup/restore are available in the PC editor. Target-TV acceptance is still incomplete.",16,muted));
    }
    private ComponentName homeAlias(){return new ComponentName(this,"com.ismail.nooktv.HomeEntry");}
    private boolean homeEnabled(){return getPackageManager().getComponentEnabledSetting(homeAlias())==PackageManager.COMPONENT_ENABLED_STATE_ENABLED;}
    private boolean isDefaultHome(){ResolveInfo info=getPackageManager().resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),PackageManager.MATCH_DEFAULT_ONLY);return info!=null&&info.activityInfo!=null&&getPackageName().equals(info.activityInfo.packageName);}
    private void homeSetup(){
        page("home-setup","Your TV home",isDefaultHome()?"Nook is the selected Home app.":homeEnabled()?"Nook is available for Home selection.":"Choose Nook in your TV's Home settings.");
        content.addView(text("Your original launcher stays installed. You can restore it in system Home settings. Support depends on your TV firmware.",16,muted));
        content.addView(action(homeEnabled()?"Open system Home settings":"Enable Nook Home & open settings","home-enable",()->{getPackageManager().setComponentEnabledSetting(homeAlias(),PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);openHomeSettings();}));
        content.addView(action("Open system settings","home-system",()->{try{startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));}catch(Exception e){message("Open your TV's system settings from its remote.");}}));
        content.addView(action("Refresh Home status","home-status",this::homeSetup));
        if(homeEnabled())content.addView(action("Remove Nook from Home choices","home-disable",()->new AlertDialog.Builder(this).setTitle("Remove Nook Home option?").setMessage("Select your original launcher in system Home settings first. Nook remains available as an ordinary app.").setPositiveButton("Remove",(dialog,which)->{if(isDefaultHome()){message("Choose another default Home app first.");openHomeSettings();return;}getPackageManager().setComponentEnabledSetting(homeAlias(),PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);homeSetup();}).setNegativeButton("Cancel",null).show()));
        restore("home-enable");
    }
    private void openHomeSettings(){try{startActivity(new Intent(android.provider.Settings.ACTION_HOME_SETTINGS));}catch(Exception e){message("This firmware does not expose Home settings here. Try system Settings > Apps > Default apps. Nook has not disabled your original launcher.");}}
    private void samples(){
        page("samples","Playback checks","Diagnostic samples; not part of your daily Home.");
        content.addView(action("MP4 sample","sample-mp4",()->play("https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4","Big Buck Bunny | MP4")));
        content.addView(action("HLS sample","sample-hls",()->play("https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8","Apple BipBop | HLS")));
        content.addView(action("DASH sample","sample-dash",()->play("https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd","Tears of Steel | DASH")));
        content.addView(action("Web test","sample-web",()->web("","Controlled web test")));content.addView(action("Diagnostics","diagnostics",this::diagnostics));
    }
    private void diagnostics() {
        page("diagnostics","Know your TV","No unique device identifiers, LAN addresses or session secrets are included below.");
        ActivityManager am=(ActivityManager)getSystemService(ACTIVITY_SERVICE); StringBuilder s=new StringBuilder("Nook TV 0.4.0-phone-preview\nAndroid "+Build.VERSION.RELEASE+" • API "+Build.VERSION.SDK_INT+"\nABI: "+String.join(", ",Build.SUPPORTED_ABIS)+"\nApp memory class: "+am.getMemoryClass()+" MB\nDisplay: "+getResources().getDisplayMetrics().widthPixels+" × "+getResources().getDisplayMetrics().heightPixels+"\n");
        PackageInfo w=WebView.getCurrentWebViewPackage(); s.append("WebView: ").append(w==null?"Unavailable":w.packageName+" "+w.versionName).append("\n");
        for(String pkg:new String[]{"com.google.android.youtube.tv","com.google.android.youtube","com.brave.browser"}) { try { getPackageManager().getPackageInfo(pkg,0); s.append(pkg).append(": installed\n"); } catch(Exception e) { s.append(pkg).append(": not detected\n"); } }
        int homes=getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),0).size(); s.append("Home handlers: ").append(homes).append(homeEnabled()?" (Nook Home option enabled)\n\nVideo/audio decoders:\n":" (Nook Home option disabled)\n\nVideo/audio decoders:\n");
        try { for(MediaCodecInfo c:new MediaCodecList(MediaCodecList.REGULAR_CODECS).getCodecInfos()) if(!c.isEncoder()) { for(String type:c.getSupportedTypes()) if(type.equals("video/avc")||type.equals("video/hevc")||type.equals("video/av01")||type.equals("audio/mp4a-latm")) s.append(type).append(" • ").append(c.getName()).append("\n"); } } catch(Exception e) { s.append("Codec inventory unavailable\n"); }
        TextView details=text(s.toString(),16,muted); details.setTextIsSelectable(true); content.addView(details);
    }
    private void message(String text) { Toast.makeText(this,text,Toast.LENGTH_LONG).show(); }
    private void recordOpen(String url,String label,String route) { if(url.isEmpty()) return; try { viewing.ledger.opened(route+":"+url,label,url,route); persistViewing(); } catch(Exception ignored) {} }
    private void allHistory() {
        page("history","Viewing history","Measured playback and recently opened links are separate. Watched items stay here until you remove or clear them.");
        try { JSONArray entries=viewing.ledger.snapshot().getJSONArray("entries"); if(entries.length()==0) content.addView(text("No viewing history yet.",18,muted)); for(int i=0;i<entries.length();i++) { JSONObject e=entries.getJSONObject(i); content.addView(action(e.optString("label")+" | "+e.optString("status"),"all-history-"+e.optString("id"),()->historyOptions(e))); } } catch(Exception e) { message("Could not read viewing history."); }
    }
    private void createCollection() {
        EditText input=new EditText(this); input.setHint("Collection name");
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Create a collection").setView(input).setPositiveButton("Create",null).setNegativeButton("Cancel",null).create();
        dialog.setOnShowListener(d->dialog.getButton(-1).setOnClickListener(v->{ try { JSONObject c=current(); c.getJSONArray("collections").put(new JSONObject().put("id",UUID.randomUUID().toString()).put("label",input.getText().toString().trim()).put("items",new JSONArray())); store.save(c,c.getLong("revision"),UUID.randomUUID().toString()); dialog.dismiss(); home(); } catch(Exception e) { input.setError("Use a name of 1-80 characters; maximum 20 collections."); } })); dialog.show();
    }
    private void manageCollections() {
        page("collections","Your collections","Collections group your saved links and favorite apps. Removing a collection keeps those saved items.");
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
    @Override public JSONObject restore(JSONObject input,long revision,String mutation) throws Exception { if(!foreground) throw new IOException(); JSONObject normalized=DashboardDocument.migrate(input); validateBackground(normalized); JSONObject result=store.restore(normalized,revision,mutation); handler.post(()->{ if(screen.equals("home")) home(); }); return result; }
    private void validateBackground(JSONObject input) throws Exception {
        String bg=input.getString("background"); if(bg.isEmpty()) return; byte[] bytes=android.util.Base64.decode(bg.substring(bg.indexOf(',')+1),android.util.Base64.DEFAULT); if(bytes.length>512000) throw new IllegalArgumentException(); BitmapFactory.Options options=new BitmapFactory.Options(); options.inJustDecodeBounds=true; BitmapFactory.decodeByteArray(bytes,0,bytes.length,options); if(options.outWidth<1||options.outWidth>3840||options.outHeight<1||options.outHeight>2160||!"image/jpeg".equals(options.outMimeType)) throw new IllegalArgumentException("Invalid image");
    }
    @Override public JSONObject inventory() throws Exception {
        if(!foreground) throw new IOException(); JSONArray apps=new JSONArray(); Set<String> components=new HashSet<>(); PackageManager pm=getPackageManager(); for(String category:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER}) for(ResolveInfo info:pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(category),0)) { String component=new ComponentName(info.activityInfo.packageName,info.activityInfo.name).flattenToString(); if(!info.activityInfo.packageName.equals(getPackageName())&&components.add(component)) apps.put(new JSONObject().put("component",component).put("label",info.loadLabel(pm).toString())); } return new JSONObject().put("apps",apps);
    }
    @Override public void onBackPressed() {
        if(!screen.equals("home")) { home(); return; }
        if(isDefaultHome()||getIntent().hasCategory(Intent.CATEGORY_HOME)){homeScroll.smoothScrollTo(0,0);focusFirstHome();return;}
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

