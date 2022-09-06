package one.modality.base.shared.entities.impl;

import one.modality.base.shared.entities.MoneyTransfer;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;

/**
 * @author Bruno Salmon
 */
public final class MoneyTransferImpl extends DynamicEntity implements MoneyTransfer {

    public MoneyTransferImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<MoneyTransfer> {
        public ProvidedFactory() {
            super(MoneyTransfer.class, MoneyTransferImpl::new);
        }
    }
}
