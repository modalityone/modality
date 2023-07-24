package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.EntityId;

import one.modality.base.shared.entities.Site;

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
