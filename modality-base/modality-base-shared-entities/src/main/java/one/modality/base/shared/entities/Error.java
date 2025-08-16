package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.EntityHasLocalDateTime;
import one.modality.base.shared.entities.markers.EntityHasUserPerson;

/**
 * @author Bruno Salmon
 */
public interface Error extends Entity, EntityHasLocalDateTime, EntityHasUserPerson {

    String message = "message";

    default void setMessage(String value) {
        setFieldValue(message, value);
    }

    default String getMessage() {
        return getStringFieldValue(message);
    }

}