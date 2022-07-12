package org.modality_project.base.shared.entities.markers;

import org.modality_project.base.shared.entities.Site;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;

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
