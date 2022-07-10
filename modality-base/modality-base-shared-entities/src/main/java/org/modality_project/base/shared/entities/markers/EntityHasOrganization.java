package org.modality_project.base.shared.entities.markers;

import org.modality_project.base.shared.entities.Organization;
import dev.webfx.stack.framework.shared.orm.entity.Entity;
import dev.webfx.stack.framework.shared.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface EntityHasOrganization extends Entity, HasOrganization {

    @Override
    default void setOrganization(Object organization) {
        setForeignField("organization", organization);
    }

    @Override
    default EntityId getOrganizationId() {
        return getForeignEntityId("organization");
    }

    @Override
    default Organization getOrganization() {
        return getForeignEntity("organization");
    }


}
