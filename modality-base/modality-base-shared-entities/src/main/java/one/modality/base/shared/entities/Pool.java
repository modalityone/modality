package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasName;

public interface Pool extends
    EntityHasName {

    String webColor = "webColor";
    String graphic = "graphic";
    String eventType = "eventType";

    default void setWebColor(String value) {
        setFieldValue(webColor, value);
    }

    default String getWebColor() {
        return getStringFieldValue(webColor);
    }

    default void setGraphic(String value) {
        setFieldValue(graphic, value);
    }

    default String getGraphic() {
        return getStringFieldValue(graphic);
    }

    default void setEventType(Object value) {
        setForeignField(eventType, value);
    }

    default EntityId getEventTypeId() {
        return getForeignEntityId(eventType);
    }

    default EventType getEventType() {
        return getForeignEntity(eventType);
    }


}