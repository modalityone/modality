package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface EntityHasCode extends Entity, HasCode {

    String code = "code";

    @Override
    default void setCode(String value) {
        setFieldValue(code, value);
    }

    @Override
    default String getCode() {
        return getStringFieldValue(code);
    }
}