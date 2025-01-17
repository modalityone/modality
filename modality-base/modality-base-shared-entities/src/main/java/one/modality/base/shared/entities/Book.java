package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.EntityHasOrd;

import java.time.LocalDate;

public interface Book extends Entity,
    EntityHasOrd {
    String lang = "lang";
    String title = "title";
    String description = "description";
    String publishDate = "publishDate";
    String imageUrl = "imageUrl";
    String orderUrl = "orderUrl";
    String freeUrl = "freeUrl";

    default void setLang(String value) {
        setFieldValue(lang, value);
    }

    default String getLang() {
        return getStringFieldValue(lang);
    }

    default void setTitle(String value) {
        setFieldValue(title, value);
    }

    default String getTitle() {
        return getStringFieldValue(title);
    }

    default void setDescription(String value) {
        setFieldValue(description, value);
    }

    default String getDescription() {
        return getStringFieldValue(description);
    }

    default void setPublishDate(LocalDate value) {
        setFieldValue(publishDate, value);
    }

    default LocalDate getPublishDate() {
        return getLocalDateFieldValue(publishDate);
    }

    default void setImageUrl(String value) {
        setFieldValue(imageUrl, value);
    }

    default String getImageUrl() {
        return getStringFieldValue(imageUrl);
    }

    default void setOrderUrl(String value) {
        setFieldValue(orderUrl, value);
    }

    default String getOrderUrl() {
        return getStringFieldValue(orderUrl);
    }

    default void setFreeUrl(String value) {
        setFieldValue(freeUrl, value);
    }

    default String getFreeUrl() {
        return getStringFieldValue(freeUrl);
    }

}