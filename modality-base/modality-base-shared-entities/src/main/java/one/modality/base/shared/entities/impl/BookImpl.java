package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Book;

/**
 * @author Bruno Salmon
 */
public final class BookImpl extends DynamicEntity implements Book {

    public BookImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Book> {
        public ProvidedFactory() {
            super(Book.class, BookImpl::new);
        }
    }
}
