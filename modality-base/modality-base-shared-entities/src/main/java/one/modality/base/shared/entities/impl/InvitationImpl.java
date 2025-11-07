package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.Invitation;

/**
 * @author Bruno Salmon
 */
public final class InvitationImpl extends DynamicEntity implements Invitation {

    public InvitationImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Invitation> {
        public ProvidedFactory() {
            super(Invitation.class, InvitationImpl::new);
        }
    }
}
