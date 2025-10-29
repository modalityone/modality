package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * Entity representing an authorization rule in the system.
 * Rules define conditions that control access to specific operations or features.
 *
 * @author Claude Code
 */
public interface AuthorizationRule extends Entity, EntityHasName {

    // Field name constants
    String RULE = "rule";
    String ROLE = "role";

    // Rule expression (required)

    default void setRule(String rule) {
        setFieldValue(RULE, rule);
    }

    default String getRule() {
        return getStringFieldValue(RULE);
    }

    // Role (optional foreign key)

    default void setRole(Object value) {
        setForeignField(ROLE, value);
    }

    default EntityId getRoleId() {
        return getForeignEntityId(ROLE);
    }

    default AuthorizationRole getRole() {
        return getForeignEntity(ROLE);
    }
}
