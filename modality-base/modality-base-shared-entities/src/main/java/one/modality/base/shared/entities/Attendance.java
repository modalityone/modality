package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasLocalDate;
import one.modality.base.shared.entities.markers.EntityHasDocumentLine;
import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface Attendance extends Entity, EntityHasDocumentLine, EntityHasLocalDate {
    String attended = "attended";
    String scheduledResource = "scheduledResource";
    String scheduledItem = "scheduledItem";

    default Boolean isAttended() {
        return getBooleanFieldValue(attended);
    }

    default void setAttended(Boolean value) {
        setFieldValue(attended, value);
    }

    default void setScheduledResource(Object value) {
        setForeignField(scheduledResource, value);
    }

    default EntityId getScheduledResourceId() {
        return getForeignEntityId(scheduledResource);
    }

    default ScheduledResource getScheduledResource() {
        return getForeignEntity(scheduledResource);
    }

    default void setScheduledItem(Object value) {
        setForeignField(scheduledItem, value);
    }

    default EntityId getScheduledItemId() {
        return getForeignEntityId(scheduledItem);
    }

    default ScheduledItem getScheduledItem() {
        return getForeignEntity(scheduledItem);
    }
}