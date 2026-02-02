package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.AuthorizationOrganizationAdmin;

/**
 * @author Claude Code
 */
public final class AuthorizationOrganizationAdminImpl extends DynamicEntity implements AuthorizationOrganizationAdmin {

    public AuthorizationOrganizationAdminImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<AuthorizationOrganizationAdmin> {
        public ProvidedFactory() {
            super(AuthorizationOrganizationAdmin.class, AuthorizationOrganizationAdminImpl::new);
        }
    }
}
