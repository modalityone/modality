package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Organization;

/**
 * @author Bruno Salmon
 */
public interface HasCorporation {

    void setCorporation(Object corporation);

    EntityId getCorporationId();

    Organization getCorporation();

}
