package one.modality.base.shared.entities.impl;

import one.modality.base.shared.entities.Cart;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;

/**
 * @author Bruno Salmon
 */
public final class CartImpl extends DynamicEntity implements Cart {

    public CartImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Cart> {
        public ProvidedFactory() {
            super(Cart.class, CartImpl::new);
        }
    }

}
