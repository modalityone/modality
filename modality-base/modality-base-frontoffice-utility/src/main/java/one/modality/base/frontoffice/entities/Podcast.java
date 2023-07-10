package one.modality.base.frontoffice.entities;

import dev.webfx.platform.json.ReadOnlyJsonObject;
import dev.webfx.platform.util.Dates;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class Podcast {
    public String title;
    public String excerpt;
    public String image;
    public String link;
    public String date;

    public Podcast(ReadOnlyJsonObject o) {
        title = o.getObject("title").getString("rendered");
        excerpt = o.getObject("excerpt").getString("rendered");
        image = o.getString("episode_featured_image");
        link = o.getString("player_link");
        date = o.getString("date");
        LocalDateTime localDateTime = Dates.parseIsoLocalDateTime(date);
        date = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(localDateTime);
    }

}
