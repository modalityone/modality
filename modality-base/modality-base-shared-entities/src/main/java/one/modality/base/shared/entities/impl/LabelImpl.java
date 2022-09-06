package one.modality.base.shared.entities.impl;

import one.modality.base.shared.entities.Label;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;

/**
 * @author Bruno Salmon
 */
public final class LabelImpl extends DynamicEntity implements Label {

    public LabelImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Label> {
        public ProvidedFactory() {
            super(Label.class, LabelImpl::new);
        }
    }
}
