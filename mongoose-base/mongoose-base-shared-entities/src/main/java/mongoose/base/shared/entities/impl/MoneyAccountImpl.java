package mongoose.base.shared.entities.impl;

import dev.webfx.framework.shared.orm.entity.EntityId;
import dev.webfx.framework.shared.orm.entity.EntityStore;
import dev.webfx.framework.shared.orm.entity.impl.DynamicEntity;
import dev.webfx.framework.shared.orm.entity.impl.EntityFactoryProviderImpl;
import mongoose.base.shared.entities.MoneyAccount;

/**
 * @author Bruno Salmon
 */
public class MoneyAccountImpl extends DynamicEntity implements MoneyAccount {

    public MoneyAccountImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<MoneyAccount> {
        public ProvidedFactory() {
            super(MoneyAccount.class, MoneyAccountImpl::new);
        }
    }
}
