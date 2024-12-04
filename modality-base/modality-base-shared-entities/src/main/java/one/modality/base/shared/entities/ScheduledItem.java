package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.*;

import java.time.LocalDateTime;

/**
 * @author Bruno Salmon
 */
public interface ScheduledItem extends Entity,
    EntityHasName,
    EntityHasLabel,
    EntityHasEvent,
    EntityHasTeacher,
    EntityHasLocalDate,
    EntityHasSiteAndItem,
    EntityHasStartAndEndTime {

    String programScheduledItem = "programScheduledItem";
    String bookableScheduledItem = "bookableScheduledItem";
    String timeline = "timeline";
    String expirationDate = "expirationDate";
    String comment = "comment";
    String commentLabel = "commentLabel";
    String vodDelayed = "vodDelayed";
    String published = "published";

    default void setProgramScheduledItem(Object value) {
        setForeignField(programScheduledItem, value);
    }

    default EntityId getProgramScheduledItemId() {
        return getForeignEntityId(programScheduledItem);
    }

    default ScheduledItem getProgramScheduledItem() {
        return getForeignEntity(programScheduledItem);
    }

    default void setBookableScheduledItem(Object value) {
        setForeignField(bookableScheduledItem, value);
    }

    default EntityId getBookableScheduledItemId() {
        return getForeignEntityId(bookableScheduledItem);
    }

    default ScheduledItem getBookableScheduledItem() {
        return getForeignEntity(bookableScheduledItem);
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

    default void setExpirationDate(LocalDateTime value) {
        setFieldValue(expirationDate, value);
    }

    default LocalDateTime getExpirationDate() {
        return getLocalDateTimeFieldValue(expirationDate);
    }

    default void setComment(String value) {
        setFieldValue(comment, value);
    }

    default String getComment() {
        return getStringFieldValue(comment);
    }

    default void setCommentLabel(Object value) {
        setForeignField(commentLabel, value);
    }

    default EntityId getCommentLabelId() {
        return getForeignEntityId(commentLabel);
    }

    default Label getCommentLabel() {
        return getForeignEntity(commentLabel);
    }

    default void setVodDelayed(Boolean value) {
        setFieldValue(vodDelayed, value);
    }

    default Boolean isVodDelayed() {
        return getBooleanFieldValue(vodDelayed);
    }

    default void setPublished(Boolean value) {
        setFieldValue(published, value);
    }

    default Boolean isPublished() {
        return getBooleanFieldValue(published);
    }
}