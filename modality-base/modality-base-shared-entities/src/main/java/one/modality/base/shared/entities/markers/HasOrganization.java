package one.modality.base.shared.entities.markers;

import one.modality.base.shared.entities.Organization;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasOrganization {

    void setOrganization(Object organization);

    EntityId getOrganizationId();

    Organization getOrganization();

}
