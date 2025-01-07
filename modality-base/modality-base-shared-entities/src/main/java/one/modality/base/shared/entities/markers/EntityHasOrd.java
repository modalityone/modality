package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface EntityHasOrd extends Entity, HasOrd {

    String ord = "ord";

    @Override
    default void setOrd(Integer value) {
        setFieldValue(ord, value);
    }

    @Override
    default Integer getOrd() {
        return getIntegerFieldValue(ord);
    }
}