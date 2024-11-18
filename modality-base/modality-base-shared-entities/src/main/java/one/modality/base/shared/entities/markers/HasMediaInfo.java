package one.modality.base.shared.entities.markers;

import dev.webfx.extras.media.metadata.HasDurationMillis;
import dev.webfx.extras.media.metadata.HasTitle;

/**
 * @author Bruno Salmon
 */
public interface HasMediaInfo extends HasLocalDateTime,
    // WebFX Extras MediaMetadata tags for direct compatibility with Player API
    HasTitle, HasDurationMillis {

    void setTitle(String title);

    String getTitle();

    void setExcerpt(String excerpt);

    String getExcerpt();

    void setImageUrl(String imageUrl);

    String getImageUrl();

    void setDurationMillis(Long durationMillis);

    Long getDurationMillis();

    void setLang(String lang);

    String getLang();

}
