package mongoose.base.shared.entities.markers;

import dev.webfx.framework.shared.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface EntityHasCancelled extends Entity, HasCancelled {

    @Override
    default void setCancelled(Boolean cancelled) {
        setFieldValue("cancelled", cancelled);
    }

    @Override
    default Boolean isCancelled() {
        return getBooleanFieldValue("cancelled");
    }

}
