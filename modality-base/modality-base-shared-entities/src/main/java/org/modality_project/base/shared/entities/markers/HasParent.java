package org.modality_project.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasParent<P extends Entity> {

    void setParent(Object parent);

    EntityId getParentId();

    P getParent();

}
