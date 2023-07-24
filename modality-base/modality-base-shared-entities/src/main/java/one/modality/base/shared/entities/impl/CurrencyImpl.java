package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.Currency;

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
