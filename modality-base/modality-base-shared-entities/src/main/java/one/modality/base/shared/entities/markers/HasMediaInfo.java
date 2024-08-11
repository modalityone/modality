package one.modality.base.shared.entities.markers;

/**
 * @author Bruno Salmon
 */
public interface HasMediaInfo extends HasLocalDateTime {

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
