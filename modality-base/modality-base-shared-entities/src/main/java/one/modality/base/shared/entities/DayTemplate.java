package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasName;

public interface DayTemplate extends
    EntityHasEvent,
    EntityHasName {

    default void setDayTemplate(Object dayTemplate) {
        setForeignField("dayTemplate", dayTemplate);
    }

    default EntityId getDayTemplateId() {
        return getForeignEntityId("dayTemplate");
    }

    default DayTemplate getDayTemplate() {
        return getForeignEntity("dayTemplate");
    }

}
