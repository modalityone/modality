package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.Letter;

/**
 * @author Bruno Salmon
 */
public final class LetterImpl extends DynamicEntity implements Letter {

    public LetterImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Letter> {
        public ProvidedFactory() {
            super(Letter.class, LetterImpl::new);
        }
    }
}
