package org.modality_project.base.shared.entities.markers;

import org.modality_project.base.shared.entities.Site;
import dev.webfx.stack.framework.shared.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasArrivalSite {

    void setArrivalSite(Object site);

    EntityId getArrivalSiteId();

    Site getArrivalSite();

    default boolean hasArrivalSite() {
        return getArrivalSite() != null;
    }

}
