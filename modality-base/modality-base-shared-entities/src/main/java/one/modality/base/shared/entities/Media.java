package one.modality.base.shared.entities;

import dev.webfx.platform.util.Strings;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface Media extends Entity {

    default void setScheduledItem(Object scheduledItem) {
        setForeignField("scheduledItem", scheduledItem);
    }

    default EntityId getScheduledItemId() {
        return getForeignEntityId("scheduledItem");
    }

    default ScheduledItem getScheduledItem() {
        return getForeignEntity("scheduledItem");
    }

    default void setType(Object type) {
        setFieldValue("type", Strings.stringValue(type));
    }

    default MediaType getType() {
        return MediaType.of(getStringFieldValue("type"));
    }

    default void setLang(String lang) {
        setFieldValue("lang", lang);
    }

    default String getLang() {
        return getStringFieldValue("lang");
    }

    default void setUrl(String url) {
        setFieldValue("url", url);
    }

    default String getUrl() {
        return getStringFieldValue("url");
    }

    default void setMultilang(Boolean multilang) {
        setFieldValue("multilang", multilang);
    }

    default Boolean isMultilang() {
        return getBooleanFieldValue("multilang");
    }

    default void setPublished(Boolean published) {
        setFieldValue("published", published);
    }

    default Boolean isPublished() {
        return getBooleanFieldValue("published");
    }

}

