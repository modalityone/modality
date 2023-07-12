package one.modality.base.frontoffice.entities;

import dev.webfx.platform.json.ReadOnlyJsonObject;
import dev.webfx.platform.util.Dates;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class NewsOld {
    public String title;
    public String content;
    public String date;
    public String excerpt;
    public String mediaId;
    public String url;

    public NewsOld(ReadOnlyJsonObject o) {
        title = o.getObject("title").getString("rendered");
        content = o.getObject("content").getString("rendered");
        date = o.getString("date");
        excerpt = o.getObject("excerpt").getString("rendered");
        mediaId = o.getString("featured_media");

        LocalDateTime localDateTime = Dates.parseIsoLocalDateTime(date);
        date = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(localDateTime);

        url = o.getObject("guid").getString("rendered");
    }
}
