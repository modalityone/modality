package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * Entity representing an authorization role in the system.
 * Roles are collections of operations and operation groups that define what users can do.
 *
 * @author Claude Code
 */
public interface AuthorizationRole extends Entity, EntityHasName {

    // Field name constants
    String DESCRIPTION = "description";

    // Description (optional)

    default void setDescription(String description) {
        setFieldValue(DESCRIPTION, description);
    }

    default String getDescription() {
        return getStringFieldValue(DESCRIPTION);
    }
}
