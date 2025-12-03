package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Resource;

/**
 * @author Bruno Salmon
 */
public interface HasResource {

    void setResource(Object resource);

    EntityId getResourceId();

    Resource getResource();

}
