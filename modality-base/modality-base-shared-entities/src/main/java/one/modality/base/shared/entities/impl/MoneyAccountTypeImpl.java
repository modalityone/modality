package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;

import one.modality.base.shared.entities.MoneyAccountType;

/**
 * @author Bruno Salmon
 */
public class MoneyAccountTypeImpl extends DynamicEntity implements MoneyAccountType {

    public MoneyAccountTypeImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<MoneyAccountType> {
        public ProvidedFactory() {
            super(MoneyAccountType.class, MoneyAccountTypeImpl::new);
        }
    }
}
