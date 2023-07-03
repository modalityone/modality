package one.modality.base.frontoffice.entities;

import dev.webfx.platform.json.ReadOnlyJsonObject;

public class Podcast {
    public String title;
    public String excerpt;
    public String image;
    public String link;

    public Podcast(ReadOnlyJsonObject o) {
        title = o.getObject("title").getString("rendered");
        excerpt = o.getObject("excerpt").getString("rendered");
        image = o.getString("episode_featured_image");
        link = o.getString("player_link");
    }

}
