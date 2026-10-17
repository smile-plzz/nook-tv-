package com.ismail.nooktv;
/** Owner-supplied site URL contracts. No built-in provider is declared verified. */
public final class SourceTemplate {
    private SourceTemplate() {}
    public static String build(String template,String imdb,int season,int episode) {
        if(template.length()>2048||!imdb.matches("tt[0-9]{7,10}")||!template.contains("{imdb}")||season<0||episode<0||season>100||episode>10000) throw new IllegalArgumentException("Invalid source template or title");
        if(season>0&&(!template.contains("{season}")||!template.contains("{episode}")||episode<1)) throw new IllegalArgumentException("Series source needs season and episode placeholders");
        String result=template.replace("{imdb}",imdb).replace("{season}",String.valueOf(season)).replace("{episode}",String.valueOf(episode)); Targets.https(result); return result;
    }
}
