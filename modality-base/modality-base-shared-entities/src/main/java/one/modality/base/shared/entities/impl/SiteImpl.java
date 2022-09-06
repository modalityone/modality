package one.modality.base.shared.entities.impl;

import one.modality.base.shared.entities.Site;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;

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
