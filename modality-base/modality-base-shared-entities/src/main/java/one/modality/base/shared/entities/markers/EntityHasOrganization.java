package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Organization;

/**
 * @author Bruno Salmon
 */
public interface EntityHasOrganization extends Entity, HasOrganization {

    String organization = "organization";

    @Override
    default void setOrganization(Object value) {
        setForeignField(organization, value);
    }

    @Override
    default EntityId getOrganizationId() {
        return getForeignEntityId(organization);
    }

    @Override
    default Organization getOrganization() {
        return getForeignEntity(organization);
    }
}