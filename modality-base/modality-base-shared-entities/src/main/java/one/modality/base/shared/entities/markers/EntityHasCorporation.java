package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Organization;

/**
 * @author Bruno Salmon
 */
public interface EntityHasCorporation extends Entity, HasCorporation {

    @Override
    default void setCorporation(Object corporation) {
        setForeignField("corporation", corporation);
    }

    @Override
    default EntityId getCorporationId() {
        return getForeignEntityId("corporation");
    }

    @Override
    default Organization getCorporation() {
        return getForeignEntity("corporation");
    }

}
