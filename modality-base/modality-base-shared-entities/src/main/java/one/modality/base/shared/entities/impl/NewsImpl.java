package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.News;

/**
 * @author Bruno Salmon
 */
public final class NewsImpl extends DynamicEntity implements News {

    public NewsImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<News> {
        public ProvidedFactory() {
            super(News.class, NewsImpl::new);
        }
    }
}
