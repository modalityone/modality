package one.modality.base.shared.entities.impl;

import one.modality.base.shared.entities.Item;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;

/**
 * @author Bruno Salmon
 */
public final class ItemImpl extends DynamicEntity implements Item {

    public ItemImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Item> {
        public ProvidedFactory() {
            super(Item.class, ItemImpl::new);
        }
    }
}
