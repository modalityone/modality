package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.AuthorizationRole;

/**
 * @author Claude Code
 */
public final class AuthorizationRoleImpl extends DynamicEntity implements AuthorizationRole {

    public AuthorizationRoleImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<AuthorizationRole> {
        public ProvidedFactory() {
            super(AuthorizationRole.class, AuthorizationRoleImpl::new);
        }
    }
}
