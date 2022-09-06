package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface Cart extends Entity {

    default void setUuid(String uuid) {
        setFieldValue("uuid", uuid);
    }

    default String getUuid() {
        return getStringFieldValue("uuid");
    }
    
}
