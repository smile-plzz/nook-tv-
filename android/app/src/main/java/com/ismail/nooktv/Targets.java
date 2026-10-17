package com.ismail.nooktv;

import java.net.URI;
import java.util.Locale;

public final class Targets {
    private Targets() {}
    public static URI https(String input) {
        try {
            URI uri = new URI(input);
            if (input.length() > 2048 || !"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getRawUserInfo() != null || uri.getPort() != -1 && uri.getPort() != 443) throw new IllegalArgumentException();
            return uri;
        } catch (Exception e) { throw new IllegalArgumentException("Enter a valid HTTPS URL without credentials or a custom port."); }
    }
    public static boolean youtube(String input) {
        URI u = https(input); String h = u.getHost().toLowerCase(Locale.ROOT);
        return h.equals("youtube.com") || h.equals("www.youtube.com") || h.equals("m.youtube.com") || h.equals("youtu.be");
    }
    public static boolean allowedHost(String host, java.util.Set<String> hosts) { return host != null && hosts.contains(host); }
    public static String imdb(String input) {
        URI u=https(input); if(!u.getHost().equalsIgnoreCase("www.imdb.com")&&!u.getHost().equalsIgnoreCase("imdb.com")) return null;
        java.util.regex.Matcher m=java.util.regex.Pattern.compile("^/title/(tt[0-9]{7,10})/?$").matcher(u.getPath()); return m.matches()?m.group(1):null;
    }
}
