package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.ScheduledBoundary;

/**
 * @author Bruno Salmon
 */
public final class ScheduledBoundaryImpl extends DynamicEntity implements ScheduledBoundary {

    public ScheduledBoundaryImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<ScheduledBoundary> {
        public ProvidedFactory() {
            super(ScheduledBoundary.class, ScheduledBoundaryImpl::new);
        }
    }
}
