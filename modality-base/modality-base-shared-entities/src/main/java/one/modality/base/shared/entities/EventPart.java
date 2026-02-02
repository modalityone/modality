package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * @author Bruno Salmon
 */
public interface EventPart extends BoundaryPeriodEntity,
    EntityHasEvent,
    EntityHasName,
    EntityHasLabel
{
    String accommodationChangeAllowed = "accommodationChangeAllowed";

    default void setAccommodationChangeAllowed(Boolean value) {
        setFieldValue(accommodationChangeAllowed, value);
    }

    default Boolean isAccommodationChangeAllowed() {
        return getBooleanFieldValue(accommodationChangeAllowed);
    }
}
