package mongoose.shared.entities.markers;

import mongoose.shared.entities.Site;
import dev.webfx.framework.shared.orm.entity.Entity;
import dev.webfx.framework.shared.orm.entity.EntityId;

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
