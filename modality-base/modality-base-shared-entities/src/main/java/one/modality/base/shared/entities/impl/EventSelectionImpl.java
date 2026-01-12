package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.EventSelection;

/**
 * @author Bruno Salmon
 */
public final class EventSelectionImpl extends DynamicEntity implements EventSelection {

    public EventSelectionImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<EventSelection> {
        public ProvidedFactory() {
            super(EventSelection.class, EventSelectionImpl::new);
        }
    }
}
