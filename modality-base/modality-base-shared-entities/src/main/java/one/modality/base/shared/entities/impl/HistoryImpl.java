package one.modality.base.shared.entities.impl;

import one.modality.base.shared.entities.History;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;

/**
 * @author Bruno Salmon
 */
public final class HistoryImpl extends DynamicEntity implements History {

    public HistoryImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<History> {
        public ProvidedFactory() {
            super(History.class, HistoryImpl::new);
        }
    }
}
