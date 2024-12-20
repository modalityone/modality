package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface EntityHasOnline extends Entity, HasOnline {

    String online = "online";

    @Override
    default void setOnline(Boolean value) {
        setFieldValue(online, value);
    }

    @Override
    default Boolean isOnline() {
        return getBooleanFieldValue(online);
    }
}