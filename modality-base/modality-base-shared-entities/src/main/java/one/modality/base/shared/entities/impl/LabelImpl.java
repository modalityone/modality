package one.modality.base.shared.entities.impl;

import one.modality.base.shared.entities.Label;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.functions.I18nFunction;

/**
 * @author Bruno Salmon
 */
public final class LabelImpl extends DynamicEntity implements Label {

    public LabelImpl(EntityId id, EntityStore store) {
        super(id, store);
        // Not sure if it's the best place to register this function, but ok for now.
        new I18nFunction().register();
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Label> {
        public ProvidedFactory() {
            super(Label.class, LabelImpl::new);
        }
    }
}
