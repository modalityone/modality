package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.BookablePeriod;

/**
 * @author Bruno Salmon
 */
public final class BookablePeriodImpl extends DynamicEntity implements BookablePeriod {

    public BookablePeriodImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<BookablePeriod> {
        public ProvidedFactory() {
            super(BookablePeriod.class, BookablePeriodImpl::new);
        }
    }
}
