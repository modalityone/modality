package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.SiteItemFamily;

/**
 * @author Bruno Salmon
 */
public final class SiteItemFamilyImpl extends DynamicEntity implements SiteItemFamily {

    public SiteItemFamilyImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<SiteItemFamily> {
        public ProvidedFactory() {
            super(SiteItemFamily.class, SiteItemFamilyImpl::new);
        }
    }
}
