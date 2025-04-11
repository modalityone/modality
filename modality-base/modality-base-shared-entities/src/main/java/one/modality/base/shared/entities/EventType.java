package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;
import one.modality.base.shared.entities.markers.EntityHasOrd;
import one.modality.base.shared.entities.markers.EntityHasOrganization;

/**
 * @author Bruno Salmon
 */
public interface EventType extends Entity,
    EntityHasName,
    EntityHasLabel,
    EntityHasOrganization,
    EntityHasOrd {

    String recurringItem = "recurringItem";
    String ord = "ord";

    default void setRecurringItem(Object value) {
        setForeignField(recurringItem, value);
    }

    default EntityId getRecurringItemId() {
        return getForeignEntityId(recurringItem);
    }

    default Item getRecurringItem() {
        return getForeignEntity(recurringItem);
    }


    default void setOrd(Integer value) {
        setFieldValue(ord, value);
    }

    default Integer getOrd() {
        return getIntegerFieldValue(ord);
    }

    default Boolean isRecurring() {
        if (getRecurringItemId() != null)
            return true;
        if (isFieldLoaded(recurringItem))
            return false;
        return null;
    }
}