package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * Entity representing an operation (permission) in the system.
 * Operations define specific actions that can be performed (e.g., "Create Booking", "Edit Document").
 * They can be assigned to roles individually or grouped together.
 *
 * @author Claude Code
 */
public interface Operation extends Entity,
    EntityHasName,
    EntityHasLabel {

    // Field name constants
    String OPERATION_CODE = "operationCode";
    String I18N_CODE = "i18nCode";
    String BACKEND = "backend";
    String FRONTEND = "frontend";
    String PUBLIC = "public";
    String READ_ONLY = "readOnly";
    String GRANT_ROUTE = "grantRoute";
    String GUEST = "guest";
    String GROUP = "group";

    // Operation Code (required)

    default void setOperationCode(String operationCode) {
        setFieldValue(OPERATION_CODE, operationCode);
    }

    default String getOperationCode() {
        return getStringFieldValue(OPERATION_CODE);
    }

    // i18n Code (optional)

    default void setI18nCode(String i18nCode) {
        setFieldValue(I18N_CODE, i18nCode);
    }

    default String getI18nCode() {
        return getStringFieldValue(I18N_CODE);
    }

    // Backend flag

    default void setBackend(Boolean backend) {
        setFieldValue(BACKEND, backend);
    }

    default Boolean isBackend() {
        return getBooleanFieldValue(BACKEND);
    }

    // Frontend flag

    default void setFrontend(Boolean frontend) {
        setFieldValue(FRONTEND, frontend);
    }

    default Boolean isFrontend() {
        return getBooleanFieldValue(FRONTEND);
    }

    // Public flag

    default void setPublic(Boolean publicAccess) {
        setFieldValue(PUBLIC, publicAccess);
    }

    default Boolean isPublic() {
        return getBooleanFieldValue(PUBLIC);
    }

    // Read Only flag

    default void setReadOnly(Boolean readOnly) {
        setFieldValue(READ_ONLY, readOnly);
    }

    default Boolean isReadOnly() {
        return getBooleanFieldValue(READ_ONLY);
    }

    // Grant Route (optional)

    default void setGrantRoute(String grantRoute) {
        setFieldValue(GRANT_ROUTE, grantRoute);
    }

    default String getGrantRoute() {
        return getStringFieldValue(GRANT_ROUTE);
    }

    // Guest flag

    default void setGuest(Boolean guest) {
        setFieldValue(GUEST, guest);
    }

    default Boolean isGuest() {
        return getBooleanFieldValue(GUEST);
    }

    // Group (optional)

    default void setGroup(Object value) {
        setForeignField(GROUP, value);
    }

    default EntityId getGroupId() {
        return getForeignEntityId(GROUP);
    }

    default OperationGroup getGroup() {
        return getForeignEntity(GROUP);
    }
}
