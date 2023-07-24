package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;

import one.modality.base.shared.entities.Site;

/**
 * @author Bruno Salmon
 */
public interface EntityHasArrivalSite extends Entity, HasArrivalSite {

    @Override
    default void setArrivalSite(Object arrivalSite) {
        setForeignField("arrivalSite", arrivalSite);
    }

    @Override
    default EntityId getArrivalSiteId() {
        return getForeignEntityId("arrivalSite");
    }

    @Override
    default Site getArrivalSite() {
        return getForeignEntity("arrivalSite");
    }
}
