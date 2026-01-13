package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.ItemFamilyPolicy;

/**
 * @author Bruno Salmon
 */
public final class ItemFamilyPolicyImpl extends DynamicEntity implements ItemFamilyPolicy {

    public ItemFamilyPolicyImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<ItemFamilyPolicy> {
        public ProvidedFactory() {
            super(ItemFamilyPolicy.class, ItemFamilyPolicyImpl::new);
        }
    }
}
