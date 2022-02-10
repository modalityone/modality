package mongoose.shared.entities.impl;

import mongoose.shared.entities.Cart;
import dev.webfx.framework.shared.orm.entity.EntityId;
import dev.webfx.framework.shared.orm.entity.EntityStore;
import dev.webfx.framework.shared.orm.entity.impl.DynamicEntity;
import dev.webfx.framework.shared.orm.entity.impl.EntityFactoryProviderImpl;

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
