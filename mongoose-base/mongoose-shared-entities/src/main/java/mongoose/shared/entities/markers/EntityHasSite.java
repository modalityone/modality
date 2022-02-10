package mongoose.shared.entities.markers;

import mongoose.shared.entities.Site;
import dev.webfx.framework.shared.orm.entity.Entity;
import dev.webfx.framework.shared.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface EntityHasSite extends Entity, HasSite {

    @Override
    default void setSite(Object site) {
        setForeignField("site", site);
    }

    @Override
    default EntityId getSiteId() {
        return getForeignEntityId("site");
    }

    @Override
    default Site getSite() {
        return getForeignEntity("site");
    }


}
