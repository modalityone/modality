package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.Language;

/**
 * @author Bruno Salmon
 */
public final class LanguageImpl extends DynamicEntity implements Language {

    public LanguageImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Language> {
        public ProvidedFactory() {
            super(Language.class, LanguageImpl::new);
        }
    }

}
