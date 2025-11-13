package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.Recipient;

/**
 * @author David Hello
 */
public final class RecipientImpl extends DynamicEntity implements Recipient {

    public RecipientImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Recipient> {
        public ProvidedFactory() {
            super(Recipient.class, RecipientImpl::new);
        }
    }
}
