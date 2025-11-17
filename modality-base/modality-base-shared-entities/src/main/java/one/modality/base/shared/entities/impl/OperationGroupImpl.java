package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.OperationGroup;

/**
 * @author Claude Code
 */
public final class OperationGroupImpl extends DynamicEntity implements OperationGroup {

    public OperationGroupImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<OperationGroup> {
        public ProvidedFactory() {
            super(OperationGroup.class, OperationGroupImpl::new);
        }
    }
}
