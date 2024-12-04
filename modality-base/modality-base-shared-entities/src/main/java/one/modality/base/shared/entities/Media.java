package one.modality.base.shared.entities;

import dev.webfx.platform.util.Strings;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface Media extends Entity {
    String scheduledItem = "scheduledItem";
    String type = "type";
    String lang = "lang";
    String url = "url";
    String multilang = "multilang";
    String ord = "ord";
    String durationMillis = "durationMillis";

    default void setScheduledItem(Object value) {
        setForeignField(scheduledItem, value);
    }

    default EntityId getScheduledItemId() {
        return getForeignEntityId(scheduledItem);
    }

    default ScheduledItem getScheduledItem() {
        return getForeignEntity(scheduledItem);
    }

    default void setType(Object value) {
        setFieldValue(type, Strings.stringValue(value));
    }

    default MediaType getType() {
        return MediaType.of(getStringFieldValue(type));
    }

    default void setLang(String value) {
        setFieldValue(lang, value);
    }

    default String getLang() {
        return getStringFieldValue(lang);
    }

    default void setUrl(String value) {
        setFieldValue(url, value);
    }

    default String getUrl() {
        return getStringFieldValue(url);
    }

    default void setMultilang(Boolean value) {
        setFieldValue(multilang, value);
    }

    default Boolean isMultilang() {
        return getBooleanFieldValue(multilang);
    }

    default void setOrd(Integer value) {
        setFieldValue(ord, value);
    }

    default Integer getOrd() {
        return getIntegerFieldValue(ord);
    }

    default void setDurationMillis(Long value) {
        setFieldValue(durationMillis, value);
    }

    default Long getDurationMillis() {
        return getLongFieldValue(durationMillis);
    }
}