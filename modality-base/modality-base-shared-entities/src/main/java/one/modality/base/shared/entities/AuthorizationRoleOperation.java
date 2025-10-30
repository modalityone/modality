package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;

import java.time.LocalDate;

/**
 * Entity representing the relationship between authorization roles and operations/operation groups.
 * Links a role to either an individual operation or an operation group.
 *
 * @author Claude Code
 */
public interface AuthorizationRoleOperation extends Entity {

    // Field name constants
    String ROLE = "role";
    String OPERATION = "operation";
    String OPERATION_GROUP = "operationGroup";

    // Role (required)

    default void setRole(Object value) {
        setForeignField(ROLE, value);
    }

    default EntityId getRoleId() {
        return getForeignEntityId(ROLE);
    }

    default AuthorizationRole getRole() {
        return getForeignEntity(ROLE);
    }

    // Operation (optional - either operation or operationGroup should be set)

    default void setOperation(Object value) {
        setForeignField(OPERATION, value);
    }

    default EntityId getOperationId() {
        return getForeignEntityId(OPERATION);
    }

    default Operation getOperation() {
        return getForeignEntity(OPERATION);
    }

    // Operation Group (optional - either operation or operationGroup should be set)

    default void setOperationGroup(Object value) {
        setForeignField(OPERATION_GROUP, value);
    }

    default EntityId getOperationGroupId() {
        return getForeignEntityId(OPERATION_GROUP);
    }

    default OperationGroup getOperationGroup() {
        return getForeignEntity(OPERATION_GROUP);
    }

}
