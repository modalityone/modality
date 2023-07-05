package one.modality.base.frontoffice.entities;

import dev.webfx.platform.json.ReadOnlyJsonObject;

public class News {
    public String title;
    public String content;
    public String date;
    public String excerpt;
    public String mediaId;
    public News(ReadOnlyJsonObject o) {
        title = o.getObject("title").getString("rendered");
        content = o.getObject("content").getString("rendered");
        date = o.getString("date");
        excerpt = o.getObject("excerpt").getString("rendered");
        mediaId = o.getString("featured_media");
    }
}
