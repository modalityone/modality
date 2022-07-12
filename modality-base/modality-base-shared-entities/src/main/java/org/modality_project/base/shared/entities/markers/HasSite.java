package org.modality_project.base.shared.entities.markers;

import org.modality_project.base.shared.entities.Site;
import dev.webfx.stack.orm.entity.EntityId;

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
