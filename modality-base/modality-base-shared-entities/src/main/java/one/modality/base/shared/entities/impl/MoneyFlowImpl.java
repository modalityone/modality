package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;

import one.modality.base.shared.entities.MoneyFlow;

/**
 * @author Bruno Salmon
 */
public class MoneyFlowImpl extends DynamicEntity implements MoneyFlow {

    public MoneyFlowImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<MoneyFlow> {
        public ProvidedFactory() {
            super(MoneyFlow.class, MoneyFlowImpl::new);
        }
    }
}
