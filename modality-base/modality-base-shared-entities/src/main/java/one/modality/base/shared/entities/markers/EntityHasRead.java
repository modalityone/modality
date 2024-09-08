package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface EntityHasRead extends Entity, HasRead {

    @Override
    default void setRead(Boolean read) {
        setFieldValue("read", read);
    }

    @Override
    default Boolean isRead() {
        return getBooleanFieldValue("read");
    }

}
