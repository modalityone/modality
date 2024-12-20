package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.DayTemplate;

/**
 * @author Bruno Salmon
 */
public final class DayTemplateImpl extends DynamicEntity implements DayTemplate {

    public DayTemplateImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<DayTemplate> {
        public ProvidedFactory() {
            super(DayTemplate.class, DayTemplateImpl::new);
        }
    }
}
