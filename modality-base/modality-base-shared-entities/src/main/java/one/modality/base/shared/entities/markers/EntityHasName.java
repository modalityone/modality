package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface EntityHasName extends Entity, HasName {

    String name = "name";

    @Override
    default void setName(String value) {
        setFieldValue(name, value);
    }

    @Override
    default String getName() {
        return getStringFieldValue(name);
    }
}