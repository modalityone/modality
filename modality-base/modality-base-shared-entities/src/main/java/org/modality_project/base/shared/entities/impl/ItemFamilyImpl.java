package org.modality_project.base.shared.entities.impl;

import org.modality_project.base.shared.entities.ItemFamily;
import dev.webfx.stack.framework.shared.orm.entity.EntityId;
import dev.webfx.stack.framework.shared.orm.entity.EntityStore;
import dev.webfx.stack.framework.shared.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.framework.shared.orm.entity.impl.EntityFactoryProviderImpl;

/**
 * @author Bruno Salmon
 */
public final class ItemFamilyImpl extends DynamicEntity implements ItemFamily {

    public ItemFamilyImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<ItemFamily> {
        public ProvidedFactory() {
            super(ItemFamily.class, ItemFamilyImpl::new);
        }
    }
}
