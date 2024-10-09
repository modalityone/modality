package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.*;

/**
 * @author Bruno Salmon
 */
public interface Timeline extends Entity,
    EntityHasName,
    EntityHasLabel,
    EntityHasSiteAndItem,
    EntityHasItemFamily,
    EntityHasEvent,
    EntityHasStartAndEndTime {

    default void setDayTemplate(Object dayTemplate) {
        setForeignField("dayTemplate", dayTemplate);
    }

    default EntityId getDayTemplateId() {
        return getForeignEntityId("dayTemplate");
    }

    default DayTemplate getDayTemplate() {
        return getForeignEntity("dayTemplate");
    }

    default void setAudioOffered(Boolean audioOffered) {
        setFieldValue("audioOffered", audioOffered);
    }

    default Boolean isAudioOffered() {
        return getBooleanFieldValue("audioOffered");
    }

    default void setVideoOffered(Boolean videoOffered) {
        setFieldValue("videoOffered", videoOffered);
    }

    default Boolean isVideoOffered() {
        return getBooleanFieldValue("videoOffered");
    }

    default void setEventTimeline(Object eventTimeline) {
        setForeignField("eventTimeline", eventTimeline);
    }

    default EntityId getEventTimelineId() {
        return getForeignEntityId("eventTimeline");
    }

    default Timeline getEventTimeline() {
        return getForeignEntity("eventTimeline");
    }

}
