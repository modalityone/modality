package mongoose.base.shared.entities.markers;

import mongoose.base.shared.entities.Site;
import dev.webfx.framework.shared.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasSite {

    void setSite(Object site);

    EntityId getSiteId();

    Site getSite();

    default boolean hasSite() {
        return getSite() != null;
    }

}
