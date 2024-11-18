package one.modality.base.shared.entities.markers;

import dev.webfx.extras.player.metadata.HasDuration;
import dev.webfx.extras.player.metadata.HasTitle;
import javafx.util.Duration;

/**
 * @author Bruno Salmon
 */
public interface HasMediaInfo extends HasLocalDateTime,
    // WebFX Extras MediaMetadata tags for direct compatibility with Player API
    HasTitle, HasDuration {

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

    @Override
    default Duration getDuration() {
        Long durationMillis = getDurationMillis();
        return durationMillis != null ? Duration.millis(durationMillis) : null;
    }

    default void setDuration(Duration duration) {
        setDurationMillis(duration != null ? (long) duration.toMillis() : null);
    }
}
