package com.ismail.nooktv;

import android.content.Context;
import android.util.AtomicFile;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

/** M1 atomic configuration. Room and migrations remain a later milestone. */
public final class DashboardStore {
    private final AtomicFile file;
    private JSONObject state;
    private final java.util.LinkedHashMap<String, String> receipts = new java.util.LinkedHashMap<>();
    public DashboardStore(Context context) {
        file = new AtomicFile(new File(context.getFilesDir(), "dashboard-v1.json"));
        try { state = new JSONObject(new String(file.readFully(), StandardCharsets.UTF_8)); validate(state); }
        catch (FileNotFoundException e) { state = defaults(); }
        catch (Exception e) { state = defaults(); /* Leave unreadable file untouched until explicit save. */ }
    }
    private JSONObject defaults() { try { return new JSONObject("{\"schemaVersion\":1,\"revision\":0,\"title\":\"Your space. Your screen.\",\"links\":[],\"apps\":[]}"); } catch (JSONException e) { throw new AssertionError(e); } }
    public synchronized JSONObject read() throws JSONException { return new JSONObject(state.toString()); }
    public synchronized JSONObject save(JSONObject input, long expected, String mutation) throws Exception {
        if (!mutation.matches("[a-zA-Z0-9-]{16,80}")) throw new IllegalArgumentException("Invalid mutation ID");
        String fingerprint = expected + ":" + input.toString();
        if (receipts.containsKey(mutation)) {
            if (!receipts.get(mutation).equals(fingerprint)) throw new IllegalArgumentException("Mutation ID reused for another edit");
            return read();
        }
        if (expected != state.getLong("revision")) throw new java.util.ConcurrentModificationException();
        validate(input);
        JSONObject next = new JSONObject(input.toString()); next.put("revision", expected + 1);
        byte[] bytes = next.toString().getBytes(StandardCharsets.UTF_8);
        FileOutputStream out = file.startWrite();
        try { out.write(bytes); file.finishWrite(out); } catch (Exception e) { file.failWrite(out); throw e; }
        state = next; receipts.put(mutation, fingerprint);
        if (receipts.size() > 128) receipts.remove(receipts.keySet().iterator().next());
        return read();
    }
    public static void validate(JSONObject s) throws JSONException {
        if (s.getInt("schemaVersion") != 1 || s.toString().length() > 16000) throw new IllegalArgumentException("Unsupported schema or size");
        if (s.length() != 5 || !s.has("revision")) throw new IllegalArgumentException("Unknown fields");
        String title = s.getString("title").trim(); if (title.isEmpty() || title.length() > 80) throw new IllegalArgumentException("Title must be 1–80 characters");
        JSONArray links = s.getJSONArray("links"), apps = s.getJSONArray("apps");
        if (links.length() > 40 || apps.length() > 40) throw new IllegalArgumentException("M1 allows 40 links and 40 apps");
        HashSet<String> ids = new HashSet<>();
        for (int i=0;i<links.length();i++) {
            JSONObject l=links.getJSONObject(i); String id=l.getString("id"), label=l.getString("label"), route=l.getString("route");
            if (l.length()!=5 || !id.matches("[a-zA-Z0-9-]{1,80}") || !ids.add(id) || label.trim().isEmpty() || label.length()>100 || !(route.equals("browser")||route.equals("web")||route.equals("native")||route.equals("youtube"))) throw new IllegalArgumentException("Invalid link");
            String url=l.getString("url"); Targets.https(url); if (route.equals("youtube")&&!Targets.youtube(url)) throw new IllegalArgumentException("YouTube route needs a YouTube URL");
        }
        for(int i=0;i<apps.length();i++) if(!apps.getString(i).matches("[a-zA-Z0-9_.]+/[a-zA-Z0-9_.$]+")) throw new IllegalArgumentException("Invalid app component");
    }
}
