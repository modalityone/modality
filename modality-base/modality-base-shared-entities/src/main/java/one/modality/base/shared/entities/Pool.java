package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasEventType;
import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;
import one.modality.base.shared.entities.markers.EntityHasOrd;

public interface Pool extends
    EntityHasName,
    EntityHasLabel,
    EntityHasEventType,
    EntityHasOrd {

    String description = "description";
    String descriptionLabel = "descriptionLabel";
    String webColor = "webColor";
    String graphic = "graphic";
    String eventPool = "eventPool";
    String bookable = "bookable";

    default void setDescription(String value) {
        setFieldValue(description, value);
    }

    default String getDescription() {
        return getStringFieldValue(description);
    }

    default void setDescriptionLabel(Object value) {
        setForeignField(descriptionLabel, value);
    }

    default EntityId getDescriptionLabelId() {
        return getForeignEntityId(descriptionLabel);
    }

    default Label getDescriptionLabel() {
        return getForeignEntity(descriptionLabel);
    }

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

    default void setEventPool(Boolean value) {
        setFieldValue(eventPool, value);
    }

    default Boolean isEventPool() {
        return getBooleanFieldValue(eventPool);
    }

    default void setBookable(Boolean value) {
        setFieldValue(bookable, value);
    }

    default Boolean isBookable() {
        return getBooleanFieldValue(bookable);
    }

}