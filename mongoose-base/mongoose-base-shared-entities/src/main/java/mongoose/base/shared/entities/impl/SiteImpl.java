package mongoose.base.shared.entities.impl;

import mongoose.base.shared.entities.Site;
import dev.webfx.framework.shared.orm.entity.EntityId;
import dev.webfx.framework.shared.orm.entity.EntityStore;
import dev.webfx.framework.shared.orm.entity.impl.DynamicEntity;
import dev.webfx.framework.shared.orm.entity.impl.EntityFactoryProviderImpl;

/**
 * @author Bruno Salmon
 */
public final class SiteImpl extends DynamicEntity implements Site {

    public SiteImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Site> {
        public ProvidedFactory() {
            super(Site.class, SiteImpl::new);
        }
    }
}
