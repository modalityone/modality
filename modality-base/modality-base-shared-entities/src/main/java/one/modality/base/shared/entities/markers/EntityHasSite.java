package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Site;

/**
 * @author Bruno Salmon
 */
public interface EntityHasSite extends Entity, HasSite {

    String site = "site";

    @Override
    default void setSite(Object value) {
        setForeignField(site, value);
    }

    @Override
    default EntityId getSiteId() {
        return getForeignEntityId(site);
    }

    @Override
    default Site getSite() {
        return getForeignEntity(site);
    }
}