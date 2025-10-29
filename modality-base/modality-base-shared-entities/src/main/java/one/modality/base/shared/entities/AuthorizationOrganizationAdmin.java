package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * Entity representing the relationship between organizations and their administrators.
 * Links an organization to a person who has administrative privileges.
 *
 * @author Claude Code
 */
public interface AuthorizationOrganizationAdmin extends Entity {

    // Field name constants
    String ORGANIZATION = "organization";
    String ADMIN = "admin";

    // Organization (required)

    default void setOrganization(Object value) {
        setForeignField(ORGANIZATION, value);
    }

    default EntityId getOrganizationId() {
        return getForeignEntityId(ORGANIZATION);
    }

    default Organization getOrganization() {
        return getForeignEntity(ORGANIZATION);
    }

    // Admin (required)

    default void setAdmin(Object value) {
        setForeignField(ADMIN, value);
    }

    default EntityId getAdminId() {
        return getForeignEntityId(ADMIN);
    }

    default Person getAdmin() {
        return getForeignEntity(ADMIN);
    }
}
