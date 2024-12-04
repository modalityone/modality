package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasItemFamily;
import one.modality.base.shared.entities.markers.EntityHasSite;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public interface SiteItemFamily extends EntityHasSite, EntityHasItemFamily {

    String scheduledEndDate = "scheduledEndDate";
    String autoRelease = "autoRelease";

    default void setScheduledEndDate(LocalDate value) {
        setFieldValue(scheduledEndDate, value);
    }

    default LocalDate getScheduledEndDate() {
        return getLocalDateFieldValue(scheduledEndDate);
    }

    default void setAutoRelease(Boolean value) {
        setFieldValue(autoRelease, value);
    }

    default Boolean isAutoRelease() {
        return getBooleanFieldValue(autoRelease);
    }
}