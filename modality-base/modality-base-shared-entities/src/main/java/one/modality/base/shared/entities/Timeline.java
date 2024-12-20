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

    String dayTemplate = "dayTemplate";
    String audioOffered = "audioOffered";
    String videoOffered = "videoOffered";
    String eventTimeline = "eventTimeline";

    default void setDayTemplate(Object value) {
        setForeignField(dayTemplate, value);
    }

    default EntityId getDayTemplateId() {
        return getForeignEntityId(dayTemplate);
    }

    default DayTemplate getDayTemplate() {
        return getForeignEntity(dayTemplate);
    }

    default void setAudioOffered(Boolean value) {
        setFieldValue(audioOffered, value);
    }

    default Boolean isAudioOffered() {
        return getBooleanFieldValue(audioOffered);
    }

    default void setVideoOffered(Boolean value) {
        setFieldValue(videoOffered, value);
    }

    default Boolean isVideoOffered() {
        return getBooleanFieldValue(videoOffered);
    }

    default void setEventTimeline(Object value) {
        setForeignField(eventTimeline, value);
    }

    default EntityId getEventTimelineId() {
        return getForeignEntityId(eventTimeline);
    }

    default Timeline getEventTimeline() {
        return getForeignEntity(eventTimeline);
    }
}