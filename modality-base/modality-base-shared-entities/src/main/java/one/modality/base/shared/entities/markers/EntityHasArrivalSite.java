package one.modality.base.shared.entities.markers;

import one.modality.base.shared.entities.Site;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface EntityHasArrivalSite extends Entity, HasArrivalSite {

    String arrivalSite = "arrivalSite";

    @Override
    default void setArrivalSite(Object value) {
        setForeignField(arrivalSite, value);
    }

    @Override
    default EntityId getArrivalSiteId() {
        return getForeignEntityId(arrivalSite);
    }

    @Override
    default Site getArrivalSite() {
        return getForeignEntity(arrivalSite);
    }
}