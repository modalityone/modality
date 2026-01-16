package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.EventPart;

/**
 * @author Bruno Salmon
 */
public final class EventPartImpl extends DynamicEntity implements EventPart {

    public EventPartImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<EventPart> {
        public ProvidedFactory() {
            super(EventPart.class, EventPartImpl::new);
        }
    }
}
