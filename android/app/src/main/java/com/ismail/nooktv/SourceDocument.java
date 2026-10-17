package com.ismail.nooktv;

import org.json.*;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Validates and manages user-configured dynamic content sources (YouTube & RSS).
 * Maintained separately from DashboardDocument to preserve schema v2 integrity.
 */
public final class SourceDocument {
    public static final int MAX_SOURCES = 6;
    public static final int MAX_ITEMS_PER_SOURCE = 20;

    private SourceDocument() {}

    public static JSONObject defaults() throws JSONException {
        return new JSONObject()
            .put("schemaVersion", 1)
            .put("revision", 0)
            .put("sources", new JSONArray());
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }

    private static String text(JSONObject obj, String key, int max) throws JSONException {
        Object val = obj.get(key);
        require(val instanceof String, "Expected text: " + key);
        String s = ((String) val).trim();
        require(s.length() <= max, "Text exceeds maximum length: " + key);
        return s;
    }

    public static void validate(JSONObject doc) throws JSONException {
        require(doc.length() == 3 && Integer.valueOf(1).equals(doc.get("schemaVersion")), "Unsupported sources schema");
        require(doc.get("revision") instanceof Number && doc.getDouble("revision") == doc.getLong("revision") && doc.getLong("revision") >= 0, "Invalid revision");
        JSONArray sources = doc.getJSONArray("sources");
        require(sources.length() <= MAX_SOURCES, "Maximum " + MAX_SOURCES + " content sources allowed");

        Set<String> ids = new HashSet<>();
        for (int i = 0; i < sources.length(); i++) {
            JSONObject src = sources.getJSONObject(i);
            require(src.length() == 5, "Source entry must have exact fields (id, type, label, url, enabled)");
            String id = text(src, "id", 80);
            require(id.matches("[a-zA-Z0-9-]{1,80}") && ids.add(id), "Invalid or duplicate source ID: " + id);

            String type = text(src, "type", 20);
            require("youtube".equals(type) || "rss".equals(type), "Invalid source type: " + type);

            String label = text(src, "label", 80);
            require(!label.isEmpty(), "Source label cannot be empty");

            String url = text(src, "url", 2048);
            validateSourceUrl(type, url);

            require(src.get("enabled") instanceof Boolean, "Source enabled must be boolean");
        }
    }

    public static void validateSourceUrl(String type, String url) {
        URI uri = Targets.https(url);
        String host = uri.getHost();
        require(host != null && !host.isEmpty(), "Valid host required");

        if ("youtube".equals(type)) {
            require(Targets.youtube(url), "YouTube source must point to youtube.com or youtu.be");
        } else if ("rss".equals(type)) {
            validatePublicHost(host);
        }
    }

    public static void validatePublicHost(String host) {
        try {
            InetAddress addr = InetAddress.getByName(host);
            require(!addr.isLoopbackAddress(), "Loopback addresses are not permitted");
            require(!addr.isSiteLocalAddress(), "Private network addresses are not permitted");
            require(!addr.isLinkLocalAddress(), "Link-local addresses are not permitted");
            require(!addr.isAnyLocalAddress(), "Wildcard local addresses are not permitted");
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) throw (IllegalArgumentException) e;
            throw new IllegalArgumentException("Cannot resolve host: " + host);
        }
    }

    public static String canonicalYouTubeFeed(String input) {
        if (!Targets.youtube(input)) return input;
        URI uri = Targets.https(input);
        String path = uri.getPath() == null ? "" : uri.getPath();
        String query = uri.getQuery() == null ? "" : uri.getQuery();

        // If already a feed URL, keep it
        if (path.contains("/feeds/videos.xml")) return input;

        // Channel ID: /channel/UC...
        if (path.startsWith("/channel/")) {
            String channelId = path.substring("/channel/".length()).split("/")[0];
            if (channelId.matches("UC[a-zA-Z0-9_-]+")) {
                return "https://www.youtube.com/feeds/videos.xml?channel_id=" + channelId;
            }
        }
        // Playlist: /playlist?list=PL...
        if (path.equals("/playlist") || path.startsWith("/playlist/")) {
            for (String param : query.split("&")) {
                if (param.startsWith("list=")) {
                    String listId = param.substring("list=".length());
                    return "https://www.youtube.com/feeds/videos.xml?playlist_id=" + listId;
                }
            }
        }
        return input;
    }
}
