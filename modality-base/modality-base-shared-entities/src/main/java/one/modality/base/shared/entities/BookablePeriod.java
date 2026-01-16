package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * @author Bruno Salmon
 */
@Deprecated // Replaced by EventSelection, and more generally Period (concrete classes: SimplePeriod, EventPart, EventSelection, EventPhase, PhaseCoverage)
public interface BookablePeriod extends Entity,
    EntityHasEvent,
    EntityHasName,
    EntityHasLabel {

    String startScheduledItem = "startScheduledItem";
    String endScheduledItem = "endScheduledItem";

    default void setStartScheduledItem(Object value) {
        setForeignField(startScheduledItem, value);
    }

    default EntityId getStartScheduledItemId() {
        return getForeignEntityId(startScheduledItem);
    }

    default ScheduledItem getStartScheduledItem() {
        return getForeignEntity(startScheduledItem);
    }

    default void setEndScheduledItem(Object value) {
        setForeignField(endScheduledItem, value);
    }

    default EntityId getEndScheduledItemId() {
        return getForeignEntityId(endScheduledItem);
    }

    default ScheduledItem getEndScheduledItem() {
        return getForeignEntity(endScheduledItem);
    }

}
