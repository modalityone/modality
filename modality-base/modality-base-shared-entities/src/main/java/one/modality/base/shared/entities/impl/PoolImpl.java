package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.Pool;

/**
 * @author Bruno Salmon
 */
public final class PoolImpl extends DynamicEntity implements Pool {

    public PoolImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Pool> {
        public ProvidedFactory() {
            super(Pool.class, PoolImpl::new);
        }
    }
}
