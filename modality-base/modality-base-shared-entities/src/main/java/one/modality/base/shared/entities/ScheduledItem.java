package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.*;

import java.time.LocalDate;

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

    default void setParent(Object parent) {
        setForeignField("parent", parent);
    }

    default EntityId getParentId() {
        return getForeignEntityId("parent");
    }

    default ScheduledItem getParent() {
        return getForeignEntity("parent");
    }

    default void setTimeLine(Object timeline) {
        setForeignField("timeline", timeline);
    }

    default EntityId getTimelineId() {
        return getForeignEntityId("timeline");
    }

    default Timeline getTimeline() {
        return getForeignEntity("timeline");
    }

    default void setExpirationDate(LocalDate expirationDate) {
        setFieldValue("expirationDate", expirationDate);
    }

    default LocalDate getExpirationDate() {
        return getLocalDateFieldValue("expirationDate");
    }

}
