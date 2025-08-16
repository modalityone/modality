package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.Error;

/**
 * @author Bruno Salmon
 */
public final class ErrorImpl extends DynamicEntity implements Error {

    public ErrorImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Error> {
        public ProvidedFactory() {
            super(Error.class, ErrorImpl::new);
        }
    }
}
