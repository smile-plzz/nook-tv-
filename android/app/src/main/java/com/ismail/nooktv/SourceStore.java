package com.ismail.nooktv;

import android.content.Context;
import android.util.AtomicFile;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Atomic persistence for dynamic content source configurations.
 * Thread-safe with revision tracking and mutation receipts.
 */
public final class SourceStore {
    private final AtomicFile file;
    private JSONObject state, receipts = new JSONObject();
    private boolean damaged;

    public SourceStore(Context context) {
        file = new AtomicFile(new File(context.getFilesDir(), "sources-v1.json"));
        try {
            JSONObject disk = new JSONObject(new String(file.readFully(), StandardCharsets.UTF_8));
            if (disk.has("config")) {
                state = disk.getJSONObject("config");
                receipts = disk.optJSONObject("receipts");
                if (receipts == null) receipts = new JSONObject();
            } else {
                state = disk;
            }
            SourceDocument.validate(state);
        } catch (FileNotFoundException e) {
            fresh();
        } catch (Exception e) {
            damaged = true;
            fresh();
        }
    }

    private void fresh() {
        try {
            state = SourceDocument.defaults();
        } catch (JSONException e) {
            throw new AssertionError(e);
        }
    }

    public synchronized boolean damaged() {
        return damaged;
    }

    public synchronized JSONObject read() throws JSONException {
        return new JSONObject(state.toString());
    }

    public synchronized JSONObject save(JSONObject input, long expected, String mutation) throws Exception {
        if (damaged) throw new IllegalStateException("Saved sources could not be read; changes not saved.");
        if (!mutation.matches("[a-zA-Z0-9-]{16,80}")) throw new IllegalArgumentException("Invalid mutation ID");
        String digest = digest(expected + ":" + input.toString());
        if (receipts.has(mutation)) {
            if (!receipts.getJSONObject(mutation).getString("digest").equals(digest)) {
                throw new IllegalArgumentException("Reused mutation ID");
            }
            return read();
        }
        if (expected != state.getLong("revision")) throw new java.util.ConcurrentModificationException();
        SourceDocument.validate(input);

        JSONObject next = new JSONObject(input.toString()).put("revision", expected + 1);
        JSONObject newReceipts = new JSONObject(receipts.toString());
        newReceipts.put(mutation, new JSONObject().put("digest", digest).put("revision", expected + 1));
        while (newReceipts.length() > 64) newReceipts.remove(newReceipts.keys().next());

        JSONObject envelope = new JSONObject().put("config", next).put("receipts", newReceipts);
        FileOutputStream out = file.startWrite();
        try {
            out.write(envelope.toString().getBytes(StandardCharsets.UTF_8));
            file.finishWrite(out);
        } catch (Exception e) {
            file.failWrite(out);
            throw e;
        }
        state = next;
        receipts = newReceipts;
        return read();
    }

    private static String digest(String s) throws Exception {
        byte[] bytes = java.security.MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder r = new StringBuilder();
        for (byte b : bytes) r.append(String.format(java.util.Locale.ROOT, "%02x", b & 255));
        return r.toString();
    }
}
