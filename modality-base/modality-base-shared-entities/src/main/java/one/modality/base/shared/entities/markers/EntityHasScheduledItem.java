package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.ScheduledItem;

/**
 * @author Bruno Salmon
 */
public interface EntityHasScheduledItem extends Entity, HasScheduledItem {

    String scheduledItem = "scheduledItem";

    @Override
    default void setScheduledItem(Object value) {
        setForeignField(scheduledItem, value);
    }

    @Override
    default EntityId getScheduledItemId() {
        return getForeignEntityId(scheduledItem);
    }

    @Override
    default ScheduledItem getScheduledItem() {
        return getForeignEntity(scheduledItem);
    }

}