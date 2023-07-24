package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;

import one.modality.base.shared.entities.ScheduledItem;

/**
 * @author Bruno Salmon
 */
public final class ScheduledItemImpl extends DynamicEntity implements ScheduledItem {

    public ScheduledItemImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<ScheduledItem> {
        public ProvidedFactory() {
            super(ScheduledItem.class, ScheduledItemImpl::new);
        }
    }
}
