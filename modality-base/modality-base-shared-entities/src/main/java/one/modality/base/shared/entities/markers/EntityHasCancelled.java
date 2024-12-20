package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface EntityHasCancelled extends Entity, HasCancelled {

    String cancelled = "cancelled";

    @Override
    default void setCancelled(Boolean value) {
        setFieldValue(cancelled, value);
    }

    @Override
    default Boolean isCancelled() {
        return getBooleanFieldValue(cancelled);
    }
}