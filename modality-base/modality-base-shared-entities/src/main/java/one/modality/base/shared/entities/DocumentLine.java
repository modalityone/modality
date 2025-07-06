package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.*;

/**
 * @author Bruno Salmon
 */
public interface DocumentLine extends
    EntityHasDocument,
    EntityHasCancelled,
    EntityHasRead,
    EntityHasArrivalSiteAndItem,
    EntityHasResourceConfiguration {
    String cleaned = "cleaned";
    String bedNumber = "bedNumber";
    String timeline = "timeline";

    default boolean isCleaned() {
        return getBooleanFieldValue(cleaned);
    }

    default void setCleaned(boolean cleaned) {
        setFieldValue(cleaned, cleaned);
    }

    // Non-persistent bedNumber field using by Household screen (allocated arbitrary at runtime)
    default Integer getBedNumber() {
        return getIntegerFieldValue(bedNumber);
    }

    default Integer getPriceNet() {
        return getIntegerFieldValue("price_net");
    }

    default void setBedNumber(Integer bedNumber) {
        setFieldValue(bedNumber, bedNumber);
    }

    default void setTimeLine(Object value) {
        setForeignField(timeline, value);
    }

    default EntityId getTimelineId() {
        return getForeignEntityId(timeline);
    }

    default Timeline getTimeline() {
        return getForeignEntity(timeline);
    }
}