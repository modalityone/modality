package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.EventPhase;

/**
 * @author Bruno Salmon
 */
public final class EventPhaseImpl extends DynamicEntity implements EventPhase {

    public EventPhaseImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<EventPhase> {
        public ProvidedFactory() {
            super(EventPhase.class, EventPhaseImpl::new);
        }
    }
}
