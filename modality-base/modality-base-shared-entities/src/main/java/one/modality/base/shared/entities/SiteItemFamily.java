package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasItemFamily;
import one.modality.base.shared.entities.markers.EntityHasSite;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public interface SiteItemFamily extends EntityHasSite, EntityHasItemFamily {

    default void setScheduledEndDate(LocalDate date) {
        setFieldValue("scheduledEndDate", date);
    }

    default LocalDate getScheduledEndDate() {
        return getLocalDateFieldValue("scheduledEndDate");
    }

    default void setAutoRelease(Boolean autoRelease) {
        setFieldValue("autoRelease", autoRelease);
    }

    default Boolean isAutoRelease() {
        return getBooleanFieldValue("autoRelease");
    }

}
