package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.Resource;

/**
 * @author Bruno Salmon
 */
public final class ResourceImpl extends DynamicEntity implements Resource {

    public ResourceImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Resource> {
        public ProvidedFactory() {
            super(Resource.class, ResourceImpl::new);
        }
    }
}
