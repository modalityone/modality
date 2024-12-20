package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasLocalDate;

/**
 * @author Bruno Salmon
 */
public interface ScheduledResource extends Entity,
    EntityHasLocalDate {

    String configuration = "configuration";
    String available = "available";
    String online = "online";
    String max = "max";

    default void setResourceConfiguration(Object value) {
        setForeignField(configuration, value);
    }

    default EntityId getResourceConfigurationId() {
        return getForeignEntityId(configuration);
    }

    default ResourceConfiguration getResourceConfiguration() {
        return getForeignEntity(configuration);
    }

    default void setAvailable(Boolean value) {
        setFieldValue(available, value);
    }

    default Boolean isAvailable() {
        return getBooleanFieldValue(available);
    }

    default void setOnline(Boolean value) {
        setFieldValue(online, value);
    }

    default Boolean isOnline() {
        return getBooleanFieldValue(online);
    }

    default void setMax(Integer value) {
        setFieldValue(max, value);
    }

    default Integer getMax() {
        return getIntegerFieldValue(max);
    }
}