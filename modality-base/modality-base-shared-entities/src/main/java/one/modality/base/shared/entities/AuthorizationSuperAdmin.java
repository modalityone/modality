package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface AuthorizationSuperAdmin extends Entity {

    // Field name constants
    String superAdmin = "superAdmin";

    // Super Admin (required)

    default void setSuperAdmin(Object value) {
        setForeignField(superAdmin, value);
    }

    default EntityId getSuperAdminId() {
        return getForeignEntityId(superAdmin);
    }

    default Person getSuperAdmin() {
        return getForeignEntity(superAdmin);
    }
}
