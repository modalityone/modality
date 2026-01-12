package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * @author Bruno Salmon
 */
public interface EventPart extends Entity,
    EntityHasEvent,
    EntityHasName,
    EntityHasLabel
{
    String startBoundary = "startBoundary";
    String endBoundary = "endBoundary";
    String accommodationChangeAllowed = "accommodationChangeAllowed";

    default void setStartBoundary(Object value) {
        setForeignField(startBoundary, value);
    }

    default EntityId getStartBoundaryId() {
        return getForeignEntityId(startBoundary);
    }

    default Event getStartBoundary() {
        return getForeignEntity(startBoundary);
    }

    default void setEndBoundary(Object value) {
        setForeignField(endBoundary, value);
    }

    default EntityId getEndBoundaryId() {
        return getForeignEntityId(endBoundary);
    }

    default Event getEndBoundary() {
        return getForeignEntity(endBoundary);
    }

    default void setAccommodationChangeAllowed(Boolean value) {
        setFieldValue(accommodationChangeAllowed, value);
    }

    default Boolean isAccommodationChangeAllowed() {
        return getBooleanFieldValue(accommodationChangeAllowed);
    }
}
