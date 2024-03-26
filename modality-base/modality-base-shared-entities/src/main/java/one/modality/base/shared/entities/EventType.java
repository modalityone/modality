package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;
import one.modality.base.shared.entities.markers.EntityHasOrganization;

/**
 * @author Bruno Salmon
 */
public interface EventType extends Entity,
        EntityHasName,
        EntityHasLabel,
        EntityHasOrganization {

    default void setRecurringItem(Object recurringItem) {
        setForeignField("recurringItem", recurringItem);
    }

    default EntityId getRecurringItemId() {
        return getForeignEntityId("recurringItem");
    }

    default Item getRecurringItem() {
        return getForeignEntity("recurringItem");
    }


    default void setOrd(Integer ord) {
        setFieldValue("ord", ord);
    }

    default Integer getOrd() {
        return getIntegerFieldValue("ord");
    }

}
