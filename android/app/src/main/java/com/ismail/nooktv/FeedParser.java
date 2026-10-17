package com.ismail.nooktv;

import org.json.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Secure, bounded parser for RSS 2.0, Atom 1.0, and YouTube XML feeds.
 * Hardened against XML External Entity (XXE) injection and oversized content.
 */
public final class FeedParser {
    public static final int MAX_FEED_BYTES = 2 * 1024 * 1024; // 2 MiB max feed body
    public static final int MAX_ITEMS = 20;
    public static final int MAX_TITLE_LEN = 120;

    public static final class FeedItem {
        public final String id;
        public final String title;
        public final String url;
        public final String published;
        public final String thumbnail;
        public final String summary;

        public FeedItem(String id, String title, String url, String published, String thumbnail, String summary) {
            this.id = id != null ? id : UUID.randomUUID().toString();
            this.title = title != null ? title : "";
            this.url = url != null ? url : "";
            this.published = published != null ? published : "";
            this.thumbnail = thumbnail != null ? thumbnail : "";
            this.summary = summary != null ? summary : "";
        }

        public JSONObject toJSON() throws JSONException {
            return new JSONObject()
                .put("id", id)
                .put("title", title)
                .put("url", url)
                .put("published", published)
                .put("thumbnail", thumbnail)
                .put("summary", summary);
        }

        public static FeedItem fromJSON(JSONObject obj) {
            return new FeedItem(
                obj.optString("id"),
                obj.optString("title"),
                obj.optString("url"),
                obj.optString("published"),
                obj.optString("thumbnail"),
                obj.optString("summary")
            );
        }
    }

    public static final class FeedResult {
        public final String title;
        public final List<FeedItem> items;

        public FeedResult(String title, List<FeedItem> items) {
            this.title = title != null ? title : "";
            this.items = items != null ? Collections.unmodifiableList(items) : Collections.emptyList();
        }

        public JSONObject toJSON() throws JSONException {
            JSONArray arr = new JSONArray();
            for (FeedItem item : items) arr.put(item.toJSON());
            return new JSONObject().put("title", title).put("items", arr);
        }

        public static FeedResult fromJSON(JSONObject obj) throws JSONException {
            String title = obj.optString("title", "");
            List<FeedItem> list = new ArrayList<>();
            JSONArray arr = obj.optJSONArray("items");
            if (arr != null) {
                for (int i = 0; i < Math.min(arr.length(),MAX_ITEMS); i++) {
                    list.add(FeedItem.fromJSON(arr.getJSONObject(i)));
                }
            }
            return new FeedResult(title, list);
        }
    }

    private FeedParser() {}

    public static String stripHtml(String text) {
        if (text == null) return "";
        // Remove tags and unescape common XML/HTML entities
        String stripped = text.replaceAll("<[^>]*>", " ");
        stripped = stripped.replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'");
        return stripped.replaceAll("\\s+", " ").trim();
    }

    private static String cleanTitle(String title) {
        String s = stripHtml(title);
        if (s.length() > MAX_TITLE_LEN) s = s.substring(0, MAX_TITLE_LEN).trim();
        return s;
    }

    private static DocumentBuilder createSecureBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setExpandEntityReferences(false);
        try { dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); } catch (Exception ignored) {}
        try { dbf.setFeature("http://xml.org/sax/features/external-general-entities", false); } catch (Exception ignored) {}
        try { dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false); } catch (Exception ignored) {}
        try { dbf.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true); } catch (Exception ignored) {}
        DocumentBuilder builder=dbf.newDocumentBuilder();
        builder.setEntityResolver((publicId,systemId)->{throw new org.xml.sax.SAXException("External entities are not permitted");});
        return builder;
    }

    public static FeedResult parse(InputStream in) throws Exception {
        // Read bounded bytes
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int total = 0, count;
        while ((count = in.read(chunk)) != -1) {
            buffer.write(chunk, 0, count);
            total += count;
            if (total > MAX_FEED_BYTES) throw new IllegalArgumentException("Feed body exceeds maximum limit (2 MiB)");
        }
        if (total == 0) throw new IllegalArgumentException("Empty feed response");
        String xml=StandardCharsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(buffer.toByteArray())).toString();
        if(xml.indexOf('\u0000')>=0||xml.toUpperCase(Locale.ROOT).contains("<!DOCTYPE"))throw new IllegalArgumentException("Feed must be UTF-8 XML without a DOCTYPE");

        DocumentBuilder builder = createSecureBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(buffer.toByteArray()));
        Element root = doc.getDocumentElement();
        if (root == null) throw new IllegalArgumentException("Invalid XML document");

        String rootTag = root.getLocalName() != null ? root.getLocalName() : root.getTagName();
        if ("rss".equalsIgnoreCase(rootTag)) {
            return parseRss(root);
        } else if ("feed".equalsIgnoreCase(rootTag)) {
            return parseAtom(root);
        } else {
            throw new IllegalArgumentException("Unsupported feed root element: " + rootTag);
        }
    }

    private static FeedResult parseRss(Element root) {
        Element channel = getFirstChildByTag(root, "channel");
        if (channel == null) channel = root;

        String feedTitle = cleanTitle(getChildText(channel, "title"));
        List<FeedItem> items = new ArrayList<>();

        NodeList nodes = channel.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;
            Element el = (Element) node;
            String tagName = el.getLocalName() != null ? el.getLocalName() : el.getTagName();
            if ("item".equalsIgnoreCase(tagName)) {
                String title = cleanTitle(getChildText(el, "title"));
                String link = getChildText(el, "link");
                String pubDate = getChildText(el, "pubDate");
                String guid = getChildText(el, "guid");
                if (guid.isEmpty()) guid = link;
                String desc = cleanTitle(getChildText(el, "description"));

                String thumb = "";
                Element enc = getFirstChildByTag(el, "enclosure");
                if (enc != null && enc.hasAttribute("url")) {
                    String type = enc.getAttribute("type");
                    if (type == null || type.startsWith("image/")) thumb = enc.getAttribute("url");
                }
                if (thumb.isEmpty()) {
                    Element mediaThumb = getFirstChildByTag(el, "thumbnail");
                    if (mediaThumb != null && mediaThumb.hasAttribute("url")) thumb = mediaThumb.getAttribute("url");
                }

                if (safeLink(link)) {
                    items.add(new FeedItem(guid, title, link, pubDate, thumb, desc));
                    if (items.size() >= MAX_ITEMS) break;
                }
            }
        }
        return new FeedResult(feedTitle, items);
    }

    private static FeedResult parseAtom(Element root) {
        String feedTitle = cleanTitle(getChildText(root, "title"));
        List<FeedItem> items = new ArrayList<>();

        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;
            Element el = (Element) node;
            String tagName = el.getLocalName() != null ? el.getLocalName() : el.getTagName();
            if ("entry".equalsIgnoreCase(tagName)) {
                String title = cleanTitle(getChildText(el, "title"));
                String id = getChildText(el, "id");
                String published = getChildText(el, "published");
                if (published.isEmpty()) published = getChildText(el, "updated");
                String desc = cleanTitle(getChildText(el, "summary"));

                // YouTube specific: yt:videoId
                String videoId = getChildText(el, "videoId");
                String link = "";
                String thumb = "";

                if (videoId.matches("[a-zA-Z0-9_-]{11}")) {
                    link = "https://www.youtube.com/watch?v=" + videoId;
                    thumb = "https://i.ytimg.com/vi/" + videoId + "/hqdefault.jpg";
                    if (id.isEmpty()) id = videoId;
                }

                // Check link tag
                if (link.isEmpty()) {
                    NodeList linkNodes = el.getChildNodes();
                    for (int j = 0; j < linkNodes.getLength(); j++) {
                        Node lNode = linkNodes.item(j);
                        if (lNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element lEl = (Element) lNode;
                            if ("link".equalsIgnoreCase(lEl.getLocalName() != null ? lEl.getLocalName() : lEl.getTagName())) {
                                String rel = lEl.getAttribute("rel");
                                if (rel == null || rel.isEmpty() || "alternate".equals(rel)) {
                                    link = lEl.getAttribute("href");
                                    break;
                                }
                            }
                        }
                    }
                }

                // Check media:group for thumbnail or description
                Element mediaGroup = getFirstChildByTag(el, "group");
                if (mediaGroup != null) {
                    if (thumb.isEmpty()) {
                        Element mThumb = getFirstChildByTag(mediaGroup, "thumbnail");
                        if (mThumb != null && mThumb.hasAttribute("url")) thumb = mThumb.getAttribute("url");
                    }
                    if (desc.isEmpty()) {
                        desc = cleanTitle(getChildText(mediaGroup, "description"));
                    }
                }

                if (safeLink(link)) {
                    items.add(new FeedItem(id, title, link, published, thumb, desc));
                    if (items.size() >= MAX_ITEMS) break;
                }
            }
        }
        return new FeedResult(feedTitle, items);
    }

    private static boolean safeLink(String url){try{Targets.https(url);return true;}catch(IllegalArgumentException e){return false;}}

    private static Element getFirstChildByTag(Element parent, String tag) {
        NodeList list = parent.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;
                String name = e.getLocalName() != null ? e.getLocalName() : e.getTagName();
                if (tag.equalsIgnoreCase(name)) return e;
            }
        }
        return null;
    }

    private static String getChildText(Element parent, String tag) {
        Element child = getFirstChildByTag(parent, tag);
        if (child == null) return "";
        return child.getTextContent() != null ? child.getTextContent().trim() : "";
    }
}

