package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Organization;

/**
 * @author Bruno Salmon
 */
public interface HasOrganization {

  void setOrganization(Object organization);

  EntityId getOrganizationId();

  Organization getOrganization();
}
