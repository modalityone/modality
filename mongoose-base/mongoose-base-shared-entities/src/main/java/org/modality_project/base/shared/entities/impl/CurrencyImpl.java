package org.modality_project.base.shared.entities.impl;

import dev.webfx.framework.shared.orm.entity.EntityId;
import dev.webfx.framework.shared.orm.entity.EntityStore;
import dev.webfx.framework.shared.orm.entity.impl.DynamicEntity;
import dev.webfx.framework.shared.orm.entity.impl.EntityFactoryProviderImpl;
import org.modality_project.base.shared.entities.Currency;

/**
 * @author Bruno Salmon
 */
public final class CurrencyImpl extends DynamicEntity implements Currency {

    public CurrencyImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Currency> {
        public ProvidedFactory() {
            super(Currency.class, CurrencyImpl::new);
        }
    }
}
