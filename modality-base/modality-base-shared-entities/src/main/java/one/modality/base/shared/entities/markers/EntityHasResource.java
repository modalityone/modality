package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Resource;

/**
 * @author Bruno Salmon
 */
public interface EntityHasResource extends Entity, HasResource {

    String resource = "resource";

    @Override
    default void setResource(Object value) {
        setForeignField(resource, value);
    }

    @Override
    default EntityId getResourceId() {
        return getForeignEntityId(resource);
    }

    @Override
    default Resource getResource() {
        return getForeignEntity(resource);
    }
}