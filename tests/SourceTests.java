package com.ismail.nooktv;

import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class SourceTests {
    private static int count;

    private static void check(boolean condition, String message) {
        count++;
        if (!condition) throw new AssertionError(message);
    }

    interface Edit { void apply(JSONObject doc) throws Exception; }

    private static void expectInvalid(Edit edit, String message) throws Exception {
        JSONObject doc = SourceDocument.defaults();
        edit.apply(doc);
        boolean failed = false;
        try {
            SourceDocument.validate(doc);
        } catch (Exception e) {
            failed = true;
        }
        check(failed, message);
    }

    private static JSONObject makeSource(String id, String type, String label, String url, boolean enabled) throws JSONException {
        return new JSONObject()
            .put("id", id)
            .put("type", type)
            .put("label", label)
            .put("url", url)
            .put("enabled", enabled);
    }

    public static void main(String[] args) throws Exception {
        // 1. SourceDocument defaults & structure
        JSONObject base = SourceDocument.defaults();
        SourceDocument.validate(base);
        check(base.getInt("schemaVersion") == 1, "schemaVersion is 1");
        check(base.getLong("revision") == 0, "revision is 0");
        check(base.getJSONArray("sources").length() == 0, "sources empty by default");

        // 2. Add valid YouTube and RSS sources
        base.getJSONArray("sources").put(makeSource("src-yt", "youtube", "Veritasium", "https://www.youtube.com/channel/UCHnyfMqiRRG1u-2MsSQLbXA", true));
        base.getJSONArray("sources").put(makeSource("src-rss", "rss", "BBC News", "https://feeds.bbci.co.uk/news/rss.xml", true));
        SourceDocument.validate(base);
        check(base.getJSONArray("sources").length() == 2, "valid sources accepted");

        // 3. Validation rejection rules
        expectInvalid(s -> s.put("schemaVersion", 2), "future schema rejected");
        expectInvalid(s -> s.put("revision", -1), "negative revision rejected");
        expectInvalid(s -> s.put("extraField", "forbidden"), "extra top-level fields rejected");
        expectInvalid(s -> s.getJSONArray("sources").put(makeSource("src-bad", "unknown", "Bad", "https://example.com/rss", true)), "unknown type rejected");
        expectInvalid(s -> s.getJSONArray("sources").put(makeSource("src-blank", "rss", "", "https://example.com/rss", true)), "empty label rejected");
        expectInvalid(s -> s.getJSONArray("sources").put(makeSource("src-http", "rss", "HTTP", "http://example.com/rss", true)), "non-HTTPS rejected");
        expectInvalid(s -> s.getJSONArray("sources").put(makeSource("src-loopback", "rss", "Local", "https://127.0.0.1/rss", true)), "loopback IP rejected");
        expectInvalid(s -> s.getJSONArray("sources").put(makeSource("src-lan", "rss", "LAN", "https://192.168.1.100/rss", true)), "private LAN IP rejected");
        expectInvalid(s -> s.getJSONArray("sources").put(makeSource("src-yt-spoof", "youtube", "Fake", "https://youtube.com.evil.com/feed", true)), "YouTube host spoof rejected");

        // 4. Max 6 sources limit
        JSONObject full = SourceDocument.defaults();
        for (int i = 0; i < 6; i++) {
            full.getJSONArray("sources").put(makeSource("src-" + i, "youtube", "YT " + i, "https://www.youtube.com/channel/UC" + i, true));
        }
        SourceDocument.validate(full);
        check(full.getJSONArray("sources").length() == 6, "6 sources accepted");
        full.getJSONArray("sources").put(makeSource("src-7", "rss", "Extra", "https://example.com/rss", true));
        boolean maxExceeded = false;
        try { SourceDocument.validate(full); } catch (Exception e) { maxExceeded = true; }
        check(maxExceeded, "7th source rejected due to MAX_SOURCES limit");

        // 5. Canonical YouTube URL conversion
        String chan = SourceDocument.canonicalYouTubeFeed("https://www.youtube.com/channel/UC12345");
        check(chan.equals("https://www.youtube.com/feeds/videos.xml?channel_id=UC12345"), "channel URL canonicalized");
        String play = SourceDocument.canonicalYouTubeFeed("https://www.youtube.com/playlist?list=PL67890");
        check(play.equals("https://www.youtube.com/feeds/videos.xml?playlist_id=PL67890"), "playlist URL canonicalized");

        // 6. FeedParser: standard RSS 2.0
        String rssXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rss version=\"2.0\"><channel>\n" +
            "  <title>Tech &amp; Science</title>\n" +
            "  <link>https://example.com</link>\n" +
            "  <item>\n" +
            "    <title>Rover finds &lt;b&gt;water&lt;/b&gt; on Mars</title>\n" +
            "    <link>https://example.com/mars-rover</link>\n" +
            "    <pubDate>Mon, 17 Oct 2026 12:00:00 GMT</pubDate>\n" +
            "    <description>Exciting &lt;i&gt;new&lt;/i&gt; findings from Jezero Crater.</description>\n" +
            "    <enclosure url=\"https://example.com/image.jpg\" type=\"image/jpeg\" />\n" +
            "  </item>\n" +
            "</channel></rss>";
        FeedParser.FeedResult rssResult = FeedParser.parse(new ByteArrayInputStream(rssXml.getBytes(StandardCharsets.UTF_8)));
        check(rssResult.title.equals("Tech & Science"), "RSS channel title parsed and entity unescaped");
        check(rssResult.items.size() == 1, "RSS item count");
        check(rssResult.items.get(0).title.equals("Rover finds water on Mars"), "HTML tags stripped from RSS title");
        check(rssResult.items.get(0).url.equals("https://example.com/mars-rover"), "RSS article link parsed");
        check(rssResult.items.get(0).thumbnail.equals("https://example.com/image.jpg"), "RSS enclosure thumbnail parsed");
        check(rssResult.items.get(0).summary.equals("Exciting new findings from Jezero Crater."), "HTML tags stripped from RSS summary");

        // 7. FeedParser: YouTube Atom XML
        String ytXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:yt=\"http://www.youtube.com/xml/schemas/2015\">\n" +
            "  <title>Veritasium</title>\n" +
            "  <entry>\n" +
            "    <yt:videoId>dQw4w9WgXcQ</yt:videoId>\n" +
            "    <title>The Science of Misconceptions</title>\n" +
            "    <link rel=\"alternate\" href=\"https://www.youtube.com/watch?v=dQw4w9WgXcQ\" />\n" +
            "    <published>2026-10-15T18:00:00+00:00</published>\n" +
            "  </entry>\n" +
            "</feed>";
        FeedParser.FeedResult ytResult = FeedParser.parse(new ByteArrayInputStream(ytXml.getBytes(StandardCharsets.UTF_8)));
        check(ytResult.title.equals("Veritasium"), "YouTube feed title parsed");
        check(ytResult.items.size() == 1, "YouTube video entry parsed");
        check(ytResult.items.get(0).url.equals("https://www.youtube.com/watch?v=dQw4w9WgXcQ"), "exact YouTube video URL generated");
        check(ytResult.items.get(0).thumbnail.equals("https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg"), "YouTube standard thumbnail generated");

        // 8. FeedParser: XXE attack defense (disallow DOCTYPE / external entities)
        String xxeXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE test [\n" +
            "  <!ENTITY xxe SYSTEM \"file:///etc/passwd\">\n" +
            "]>\n" +
            "<rss version=\"2.0\"><channel><title>&xxe;</title></channel></rss>";
        boolean xxeBlocked = false;
        try {
            FeedParser.parse(new ByteArrayInputStream(xxeXml.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            xxeBlocked = true;
        }
        check(xxeBlocked, "DOCTYPE and external entity injection blocked");
        boolean utf16Blocked=false;try{FeedParser.parse(new ByteArrayInputStream(xxeXml.getBytes("UTF-16")));}catch(Exception e){utf16Blocked=true;}
        check(utf16Blocked,"non-UTF-8 XML cannot bypass DOCTYPE rejection");

        // 9. FeedParser: Oversized input rejected
        byte[] large = new byte[FeedParser.MAX_FEED_BYTES + 100];
        Arrays.fill(large, (byte) 'a');
        boolean oversizedRejected = false;
        try {
            FeedParser.parse(new ByteArrayInputStream(large));
        } catch (Exception e) {
            oversizedRejected = true;
        }
        check(oversizedRejected, "oversized feed body > 2 MiB rejected");
        JSONObject offline=SourceDocument.defaults();offline.getJSONArray("sources").put(makeSource("offline","rss","Offline feed","https://cannot-resolve.nook.invalid/feed",true));
        SourceDocument.validate(offline);check(true,"saved hostname configuration validates without network/DNS");
        String stable=SourceDocument.itemTag("one","video-a","https://example.com/a");
        check(stable.equals(SourceDocument.itemTag("one","video-a","https://example.com/changed")),"focus identity follows stable feed ID after reorder or URL change");
        check(!stable.equals(SourceDocument.itemTag("one","video-b","https://example.com/b")),"different feed items never share an index-based focus tag");
        expectInvalid(s->s.getJSONArray("sources").put(makeSource("local","rss","Private IPv6","https://[fd00::1]/feed",true)),"private IPv6 rejected");
        expectInvalid(s->s.getJSONArray("sources").put(makeSource("shared","rss","Shared IP","https://100.64.0.1/feed",true)),"shared network address rejected");

        System.out.println("PASS: " + count + " dynamic content source and secure feed assertions");
    }
}

