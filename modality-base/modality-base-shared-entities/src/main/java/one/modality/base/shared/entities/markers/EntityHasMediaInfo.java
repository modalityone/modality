package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

import java.time.LocalDateTime;

/**
 * @author Bruno Salmon
 */
public interface EntityHasMediaInfo extends Entity, HasMediaInfo, EntityHasLocalDateTime {

    String title = "title";
    String excerpt = "excerpt";
    String imageUrl = "imageUrl";
    String durationMillis = "durationMillis";
    String lang = "lang";

    @Override
    default void setTitle(String value) {
        setFieldValue(title, value);
    }

    @Override
    default String getTitle() {
        return getStringFieldValue(title);
    }

    @Override
    default void setExcerpt(String value) {
        setFieldValue(excerpt, value);
    }

    @Override
    default String getExcerpt() {
        return getStringFieldValue(excerpt);
    }

    @Override
    default void setImageUrl(String value) {
        setFieldValue(imageUrl, value);
    }

    @Override
    default String getImageUrl() {
        return getStringFieldValue(imageUrl);
    }

    @Override
    default void setDurationMillis(Long value) {
        setFieldValue(durationMillis, value);
    }

    @Override
    default Long getDurationMillis() {
        return getLongFieldValue(durationMillis);
    }

    @Override
    default void setLang(String value) {
        setFieldValue(lang, value);
    }

    @Override
    default String getLang() {
        return getStringFieldValue(lang);
    }
}