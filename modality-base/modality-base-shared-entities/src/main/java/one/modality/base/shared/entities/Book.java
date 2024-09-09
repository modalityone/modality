package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;

import java.time.LocalDate;

public interface Book extends Entity {

    default void setLang(String lang) {
        setFieldValue("lang", lang);
    }

    default String getLang() {
        return getStringFieldValue("lang");
    }

    default void setTitle(String title) {
        setFieldValue("title", title);
    }

    default String getTitle() {
        return getStringFieldValue("title");
    }

    default void setDescription(String description) {
        setFieldValue("description", description);
    }

    default String getDescription() {
        return getStringFieldValue("description");
    }

    default void setPublishDate(LocalDate publishDate) {
        setFieldValue("publishDate", publishDate);
    }

    default LocalDate getPublishDate() {
        return getLocalDateFieldValue("publishDate");
    }

    default void setImageUrl(String imageUrl) {
        setFieldValue("imageUrl", imageUrl);
    }

    default String getImageUrl() {
        return getStringFieldValue("imageUrl");
    }

    default void setOrderUrl(String orderUrl) {
        setFieldValue("orderUrl", orderUrl);
    }

    default String getOrderUrl() {
        return getStringFieldValue("orderUrl");
    }

    default void setFreeUrl(String url) {
        setFieldValue("freeUrl", url);
    }

    default String getFreeUrl() {
        return getStringFieldValue("freeUrl");
    }

    default void setOrd(Integer ord) {
        setFieldValue("ord", ord);
    }

    default Integer getOrd() {
        return getIntegerFieldValue("ord");
    }

}
