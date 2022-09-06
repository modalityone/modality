package one.modality.base.shared.entities.markers;

import one.modality.base.shared.entities.Site;
import dev.webfx.stack.orm.entity.EntityId;

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
