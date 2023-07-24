package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;

import one.modality.base.shared.entities.markers.EntityHasDate;

/**
 * @author Bruno Salmon
 */
public interface ScheduledResource extends Entity, EntityHasDate {

    default void setResourceConfiguration(Object event) {
        setForeignField("configuration", event);
    }

    default EntityId getResourceConfigurationId() {
        return getForeignEntityId("configuration");
    }

    default ResourceConfiguration getResourceConfiguration() {
        return getForeignEntity("configuration");
    }

    default void setAvailable(Boolean online) {
        setFieldValue("available", online);
    }

    default Boolean isAvailable() {
        return getBooleanFieldValue("available");
    }

    default void setOnline(Boolean online) {
        setFieldValue("online", online);
    }

    default Boolean isOnline() {
        return getBooleanFieldValue("online");
    }

    default void setMax(Integer max) {
        setFieldValue("max", max);
    }

    default Integer getMax() {
        return getIntegerFieldValue("max");
    }
}
