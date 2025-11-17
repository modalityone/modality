package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.Operation;

/**
 * @author Claude Code
 */
public final class OperationImpl extends DynamicEntity implements Operation {

    public OperationImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Operation> {
        public ProvidedFactory() {
            super(Operation.class, OperationImpl::new);
        }
    }
}
