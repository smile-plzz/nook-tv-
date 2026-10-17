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
    private final ExecutorService fetchPool;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Map<String, CachedSource> cache = new ConcurrentHashMap<>();
    private Listener listener;
    private boolean refreshing;

    public static final class CachedSource {
        public final String sourceId;
        public final long lastRefresh;
        public final boolean stale;
        public final String status;
        public final FeedParser.FeedResult feed;

        public CachedSource(String sourceId, long lastRefresh, boolean stale, String status, FeedParser.FeedResult feed) {
            this.sourceId = sourceId;
            this.lastRefresh = lastRefresh;
            this.stale = stale;
            this.status = status != null ? status : "";
            this.feed = feed != null ? feed : new FeedParser.FeedResult("", Collections.emptyList());
        }

        public JSONObject toJSON() throws JSONException {
            return new JSONObject()
                .put("sourceId", sourceId)
                .put("lastRefresh", lastRefresh)
                .put("stale", stale)
                .put("status", status)
                .put("feed", feed.toJSON());
        }

        public static CachedSource fromJSON(JSONObject obj) throws JSONException {
            return new CachedSource(
                obj.getString("sourceId"),
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
        this.fetchPool = new ThreadPoolExecutor(
            1, 2, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(16),
            new ThreadFactory() {
                private int count = 0;
                @Override public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "nook-source-fetch-" + (++count));
                    t.setDaemon(true);
                    return t;
                }
            }
        );
        loadCache();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private synchronized void loadCache() {
        try {
            if (!cacheFile.getBaseFile().exists()) return;
            JSONObject json = new JSONObject(new String(cacheFile.readFully(), StandardCharsets.UTF_8));
            JSONArray arr = json.optJSONArray("items");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
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
            FileOutputStream out = cacheFile.startWrite();
            try {
                out.write(envelope.toString().getBytes(StandardCharsets.UTF_8));
                cacheFile.finishWrite(out);
            } catch (Exception e) {
                cacheFile.failWrite(out);
            }
        } catch (Exception ignored) {}
    }

    public CachedSource getCached(String sourceId) {
        return cache.get(sourceId);
    }

    public FeedParser.FeedResult testFetch(String type, String url) throws Exception {
        return fetchFeed(type, url);
    }

    public void refresh(boolean force) {
        if (refreshing) return;
        refreshing = true;
        fetchPool.execute(() -> {
            try {
                JSONObject config = store.read();
                JSONArray sources = config.optJSONArray("sources");
                if (sources == null || sources.length() == 0) {
                    refreshing = false;
                    notifyListener();
                    return;
                }

                long now = System.currentTimeMillis();
                List<Future<?>> tasks = new ArrayList<>();

                for (int i = 0; i < sources.length(); i++) {
                    JSONObject src = sources.optJSONObject(i);
                    if (src == null || !src.optBoolean("enabled", true)) continue;

                    String id = src.optString("id");
                    String type = src.optString("type");
                    String url = src.optString("url");
                    CachedSource existing = cache.get(id);

                    if (!force && existing != null && (now - existing.lastRefresh < REFRESH_INTERVAL_MS)) {
                        continue;
                    }

                    tasks.add(fetchPool.submit(() -> {
                        try {
                            FeedParser.FeedResult result = fetchFeed(type, url);
                            CachedSource updated = new CachedSource(id, System.currentTimeMillis(), false, "Updated", result);
                            cache.put(id, updated);
                        } catch (Exception e) {
                            if (existing != null) {
                                cache.put(id, new CachedSource(id, existing.lastRefresh, true, "Offline · Cached", existing.feed));
                            } else {
                                cache.put(id, new CachedSource(id, 0, true, "Feed unavailable", new FeedParser.FeedResult("", Collections.emptyList())));
                            }
                        }
                    }));
                }

                for (Future<?> f : tasks) {
                    try { f.get(25, TimeUnit.SECONDS); } catch (Exception ignored) {}
                }
                saveCache();
            } catch (Exception ignored) {
            } finally {
                refreshing = false;
                notifyListener();
            }
        });
    }

    private void notifyListener() {
        mainHandler.post(() -> {
            if (listener != null) listener.onSourcesRefreshed();
        });
    }

    public static FeedParser.FeedResult fetchFeed(String type, String rawUrl) throws Exception {
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
