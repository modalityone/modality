package mongoose.base.shared.entities.impl;

import mongoose.base.shared.entities.MoneyTransfer;
import dev.webfx.framework.shared.orm.entity.EntityId;
import dev.webfx.framework.shared.orm.entity.EntityStore;
import dev.webfx.framework.shared.orm.entity.impl.DynamicEntity;
import dev.webfx.framework.shared.orm.entity.impl.EntityFactoryProviderImpl;

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
