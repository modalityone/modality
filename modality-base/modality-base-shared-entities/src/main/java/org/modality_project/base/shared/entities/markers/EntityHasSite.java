package org.modality_project.base.shared.entities.markers;

import org.modality_project.base.shared.entities.Site;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;

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
