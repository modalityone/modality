package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.KdmCenter;

/**
 * @author Bruno Salmon
 */
public final class KdmCenterImpl extends DynamicEntity implements KdmCenter {

    public KdmCenterImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<KdmCenter> {
        public ProvidedFactory() {
            super(KdmCenter.class, KdmCenterImpl::new);
        }
    }
}
