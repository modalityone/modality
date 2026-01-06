package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.ItemPolicy;

/**
 * @author Bruno Salmon
 */
public final class ItemPolicyImpl extends DynamicEntity implements ItemPolicy {

    public ItemPolicyImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<ItemPolicy> {
        public ProvidedFactory() {
            super(ItemPolicy.class, ItemPolicyImpl::new);
        }
    }
}
