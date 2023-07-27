package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasArrivalSiteAndItem;
import one.modality.base.shared.entities.markers.EntityHasCancelled;
import one.modality.base.shared.entities.markers.EntityHasDocument;
import one.modality.base.shared.entities.markers.EntityHasResourceConfiguration;

/**
 * @author Bruno Salmon
 */
public interface DocumentLine extends
        EntityHasDocument,
        EntityHasCancelled,
        EntityHasArrivalSiteAndItem,
        EntityHasResourceConfiguration {

    default boolean isCleaned() {
        return getBooleanFieldValue("cleaned");
    }

    default void setCleaned(boolean cleaned) {
        setFieldValue("cleaned", cleaned);
    }

    // Non-persistent bedNumber field using by Household screen (allocated arbitrary at runtime)
    default Integer getBedNumber() {
        return getIntegerFieldValue("bedNumber");
    }

    default void setBedNumber(Integer bedNumber) {
        setFieldValue("bedNumber", bedNumber);
    }

}
