package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.AuthorizationOrganizationUserAccess;

/**
 * @author Claude Code
 */
public final class AuthorizationOrganizationUserAccessImpl extends DynamicEntity implements AuthorizationOrganizationUserAccess {

    public AuthorizationOrganizationUserAccessImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<AuthorizationOrganizationUserAccess> {
        public ProvidedFactory() {
            super(AuthorizationOrganizationUserAccess.class, AuthorizationOrganizationUserAccessImpl::new);
        }
    }
}
