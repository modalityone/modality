package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface Cart extends Entity {
    String uuid = "uuid";

    default void setUuid(String value) {
        setFieldValue(uuid, value);
    }

    default String getUuid() {
        return getStringFieldValue(uuid);
    }
}