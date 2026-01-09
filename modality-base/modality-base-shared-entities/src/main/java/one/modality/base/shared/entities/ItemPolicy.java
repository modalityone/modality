package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.*;

/**
 * @author Bruno Salmon
 */
public interface ItemPolicy extends Entity,
    EntityHasItem {

    String scope = "scope";
    String minDay = "minDay";
    String _default = "default";

    default void setScope(Object value) {
        setForeignField(scope, value);
    }

    default EntityId getScopeId() {
        return getForeignEntityId(scope);
    }

    default PolicyScope getScope() {
        return getForeignEntity(scope);
    }

    default void setMinDay(Integer value) {
        setFieldValue(minDay, value);
    }

    default Integer getMinDay() {
        return getIntegerFieldValue(minDay);
    }

    default void setDefault(Boolean value) {
        setFieldValue(_default, value);
    }

    default Boolean isDefault() {
        return getBooleanFieldValue(_default);
    }

}
