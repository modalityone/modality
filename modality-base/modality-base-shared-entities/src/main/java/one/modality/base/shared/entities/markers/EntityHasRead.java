package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface EntityHasRead extends Entity, HasRead {

    String read = "read";

    @Override
    default void setRead(Boolean value) {
        setFieldValue(read, value);
    }

    @Override
    default Boolean isRead() {
        return getBooleanFieldValue(read);
    }
}