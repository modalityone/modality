package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface BoundaryPeriodEntity extends BoundaryPeriod, Entity {

    String startBoundary = "startBoundary";
    String endBoundary = "endBoundary";

    default void setStartBoundary(Object value) {
        setForeignField(startBoundary, value);
    }

    default EntityId getStartBoundaryId() {
        return getForeignEntityId(startBoundary);
    }

    @Override
    default ScheduledBoundary getStartBoundary() {
        return getForeignEntity(startBoundary);
    }

    default void setEndBoundary(Object value) {
        setForeignField(endBoundary, value);
    }

    default EntityId getEndBoundaryId() {
        return getForeignEntityId(endBoundary);
    }

    @Override
    default ScheduledBoundary getEndBoundary() {
        return getForeignEntity(endBoundary);
    }

}
