package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface EntityHasMediaInfo extends Entity, HasMediaInfo, EntityHasLocalDateTime {

    @Override
    default void setTitle(String title) {
        setFieldValue("title", title);
    }

    @Override
    default String getTitle() {
        return getStringFieldValue("title");
    }

    @Override
    default void setExcerpt(String excerpt) {
        setFieldValue("excerpt", excerpt);
    }

    @Override
    default String getExcerpt() {
        return getStringFieldValue("excerpt");
    }

    @Override
    default void setImageUrl(String imageUrl) {
        setFieldValue("imageUrl", imageUrl);
    }

    @Override
    default String getImageUrl() {
        return getStringFieldValue("imageUrl");
    }

    @Override
    default void setDurationMillis(Long durationMillis) {
        setFieldValue("durationMillis", durationMillis);
    }

    @Override
    default Long getDurationMillis() {
        return getLongFieldValue("durationMillis");
    }

    @Override
    default void setLang(String lang) {
        setFieldValue("lang", lang);
    }

    @Override
    default String getLang() {
        return getStringFieldValue("lang");
    }
}
