package com.ismail.nooktv;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AtomicFile;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * Manages fetching, caching, and refresh lifecycle of dynamic content sources.
 * Enforces 30-minute refresh intervals, 2-worker bounded concurrency, and offline staleness.
 */
public final class ContentSourceManager {
    public static final long REFRESH_INTERVAL_MS = 30 * 60 * 1000L; // 30 minutes
    public static final int CONNECT_TIMEOUT_MS = 10000;
    public static final int READ_TIMEOUT_MS = 10000;
    public static final int MAX_REDIRECTS = 3;

    public interface Listener {
        void onSourcesRefreshed();
    }

    private final SourceStore store;
    private final AtomicFile cacheFile;
    private final SourceRefreshQueue fetchPool = new SourceRefreshQueue();
    private final Map<String, Long> attempts = new ConcurrentHashMap<>();
    private volatile boolean closed;
    private boolean refreshRequested;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Map<String, CachedSource> cache = new ConcurrentHashMap<>();
    private Listener listener;


    public static final class CachedSource {
        public final String sourceId;
        public final String sourceKey;
        public final long lastRefresh;
        public final boolean stale;
        public final String status;
        public final FeedParser.FeedResult feed;

        public CachedSource(String sourceId, String sourceKey, long lastRefresh, boolean stale, String status, FeedParser.FeedResult feed) {
            this.sourceKey = sourceKey;
            this.sourceId = sourceId;
            this.lastRefresh = lastRefresh;
            this.stale = stale;
            this.status = status != null ? status : "";
            this.feed = feed != null ? feed : new FeedParser.FeedResult("", Collections.emptyList());
        }

        public JSONObject toJSON() throws JSONException {
            return new JSONObject()
                .put("sourceId", sourceId)
                .put("sourceKey", sourceKey)
                .put("lastRefresh", lastRefresh)
                .put("stale", stale)
                .put("status", status)
                .put("feed", feed.toJSON());
        }

        public static CachedSource fromJSON(JSONObject obj) throws JSONException {
            return new CachedSource(
                obj.getString("sourceId"),
                obj.getString("sourceKey"),
                obj.optLong("lastRefresh", 0),
                obj.optBoolean("stale", false),
                obj.optString("status", ""),
                FeedParser.FeedResult.fromJSON(obj.getJSONObject("feed"))
            );
        }
    }

    public ContentSourceManager(Context context, SourceStore store) {
        this.store = store;
        this.cacheFile = new AtomicFile(new File(context.getFilesDir(), "source-cache-v1.json"));
        loadCache();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private synchronized void loadCache() {
        try {
            if (!cacheFile.getBaseFile().exists() || cacheFile.getBaseFile().length() > 1024 * 1024) return;
            JSONObject json = new JSONObject(new String(cacheFile.readFully(), StandardCharsets.UTF_8));
            JSONArray arr = json.optJSONArray("items");
            if (arr != null) {
                for (int i = 0; i < Math.min(arr.length(), SourceDocument.MAX_SOURCES); i++) {
                    CachedSource cs = CachedSource.fromJSON(arr.getJSONObject(i));
                    cache.put(cs.sourceId, cs);
                }
            }
        } catch (Exception ignored) {}
    }

    private synchronized void saveCache() {
        try {
            JSONArray arr = new JSONArray();
            for (CachedSource cs : cache.values()) {
                arr.put(cs.toJSON());
            }
            JSONObject envelope = new JSONObject().put("items", arr);
            byte[] bytes = envelope.toString().getBytes(StandardCharsets.UTF_8);
            if (bytes.length > 1024 * 1024 || cacheFile.getBaseFile().getParentFile().getUsableSpace() < ReceivedMediaStore.HEADROOM + bytes.length) return;
            FileOutputStream out = cacheFile.startWrite();
            try {
                out.write(bytes);
                cacheFile.finishWrite(out);
            } catch (Exception e) {
                cacheFile.failWrite(out);
            }
        } catch (Exception ignored) {}
    }

    private static String key(String type, String url) { return type + ":" + url; }

    public CachedSource getCached(String sourceId) {
        CachedSource cs = cache.get(sourceId);
        if (cs == null) return null;
        try {
            JSONArray sources = store.read().getJSONArray("sources");
            for (int i = 0; i < sources.length(); i++) {
                JSONObject src = sources.getJSONObject(i);
                if (sourceId.equals(src.getString("id")) && cs.sourceKey.equals(key(src.getString("type"), src.getString("url")))) {
                    if (!cs.stale && System.currentTimeMillis() - cs.lastRefresh >= REFRESH_INTERVAL_MS)
                        return new CachedSource(cs.sourceId, cs.sourceKey, cs.lastRefresh, true, "Cached · Refresh due", cs.feed);
                    return cs;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public FeedParser.FeedResult testFetch(String type, String url) throws Exception {
        return fetchFeed(type, url);
    }

    public synchronized void refresh(boolean force) {
        if (closed) return;
        try {
            JSONArray sources = store.read().getJSONArray("sources");
            Set<String> active = new HashSet<>();
            Set<String> attemptKeys = new HashSet<>();
            List<Runnable> tasks = new ArrayList<>();
            long now = System.currentTimeMillis();
            for (int i = 0; i < sources.length(); i++) {
                JSONObject src = sources.getJSONObject(i);
                final String id = src.getString("id"), type = src.getString("type"), url = src.getString("url");
                final String identity = key(type, url);
                active.add(id);
                attemptKeys.add(id+identity);
                if (!src.getBoolean("enabled")) continue;
                CachedSource existing = getCached(id);
                Long attempt = attempts.get(id + identity);
                long minimum = force ? 10000 : existing != null && !existing.stale ? REFRESH_INTERVAL_MS : 60000;
                if (attempt != null && now - attempt < minimum) continue;
                if (!force && existing != null && !existing.stale && now - existing.lastRefresh < REFRESH_INTERVAL_MS) continue;
                tasks.add(() -> {
                    if (closed) return;
                    attempts.put(id + identity, System.currentTimeMillis());
                    CachedSource updated;
                    try {
                        FeedParser.FeedResult feed = fetchFeed(type, url);
                        updated = new CachedSource(id, identity, System.currentTimeMillis(), false, "Updated", feed);
                    } catch (Exception e) {
                        updated = new CachedSource(id, identity, existing == null ? 0 : existing.lastRefresh, true,
                            existing == null ? "Feed unavailable" : "Offline · Cached",
                            existing == null ? null : existing.feed);
                    }
                    if (!closed && configured(id, identity)) cache.put(id, updated);
                });
            }
            cache.keySet().retainAll(active);
            attempts.keySet().retainAll(attemptKeys);
            if(!tasks.isEmpty()&&!fetchPool.start(tasks,this::refreshFinished))refreshRequested=true;
        } catch (Exception ignored) {}
    }

    private synchronized void refreshFinished(){
        if(closed)return;
        saveCache();notifyListener();
        if(refreshRequested){refreshRequested=false;refresh(true);}
    }

    private boolean configured(String id, String identity) {
        try {
            JSONArray sources = store.read().getJSONArray("sources");
            for (int i = 0; i < sources.length(); i++) {
                JSONObject src = sources.getJSONObject(i);
                if (id.equals(src.getString("id")) && src.getBoolean("enabled")
                    && identity.equals(key(src.getString("type"), src.getString("url")))) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private void notifyListener() {
        mainHandler.post(() -> { if (!closed && listener != null) listener.onSourcesRefreshed(); });
    }

    public void close() {
        closed = true;
        listener = null;
        fetchPool.close();
        mainHandler.removeCallbacksAndMessages(null);
    }

    public static FeedParser.FeedResult fetchFeed(String type, String rawUrl) throws Exception {
        if (!"youtube".equals(type) && !"rss".equals(type)) throw new IllegalArgumentException("Unsupported source type");
        String targetUrl = rawUrl;
        if ("youtube".equals(type)) {
            targetUrl = SourceDocument.canonicalYouTubeFeed(rawUrl);
        }
        SourceDocument.validateSourceUrl(type, targetUrl);

        int redirects = 0;
        HttpURLConnection conn = null;
        try {
            while (redirects <= MAX_REDIRECTS) {
                URI uri = URI.create(targetUrl);
                SourceDocument.validatePublicHost(uri.getHost());

                URL u = uri.toURL();
                conn = (HttpURLConnection) u.openConnection();
                conn.setInstanceFollowRedirects(false);
                conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
                conn.setReadTimeout(READ_TIMEOUT_MS);
                conn.setRequestProperty("User-Agent", "NookTV/0.4.2 (Android TV)");
                conn.setRequestProperty("Accept", "application/atom+xml, application/rss+xml, application/xml, text/xml");

                int code = conn.getResponseCode();
                if (code >= 300 && code < 400) {
                    String loc = conn.getHeaderField("Location");
                    if (loc == null || loc.isEmpty()) throw new IOException("Redirect missing Location header");
                    targetUrl = loc.startsWith("http") ? loc : uri.resolve(loc).toString();
                    SourceDocument.validateSourceUrl(type, targetUrl);
                    redirects++;
                    conn.disconnect();
                    continue;
                }
                if (code != 200) {
                    throw new IOException("HTTP error " + code);
                }
                try (InputStream in = conn.getInputStream()) {
                    return FeedParser.parse(in);
                }
            }
            throw new IOException("Too many redirects");
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}

