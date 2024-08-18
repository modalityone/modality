package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.MagicLink;

/**
 * @author Bruno Salmon
 */
public final class MagicLinkImpl extends DynamicEntity implements MagicLink {

    public MagicLinkImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<MagicLink> {
        public ProvidedFactory() {
            super(MagicLink.class, MagicLinkImpl::new);
        }
    }
}
