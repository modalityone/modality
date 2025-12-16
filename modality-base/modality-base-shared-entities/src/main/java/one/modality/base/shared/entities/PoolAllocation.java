package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasPerson;
import one.modality.base.shared.entities.markers.EntityHasResource;

public interface PoolAllocation extends
    EntityHasEvent,
    EntityHasResource,
    EntityHasPerson {

    String pool = "pool";
    String quantity = "quantity";

    default void setPool(Object value) {
        setForeignField(pool, value);
    }

    default EntityId getPoolId() {
        return getForeignEntityId(pool);
    }

    default Pool getPool() {
        return getForeignEntity(pool);
    }

    default Integer getQuantity() {
        return getIntegerFieldValue(quantity);
    }

    default void setQuantity(Integer value) {
        setFieldValue(quantity, value);
    }

}