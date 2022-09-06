package one.modality.base.shared.entities.impl;

import one.modality.base.shared.entities.Organization;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;

/**
 * @author Bruno Salmon
 */
public final class OrganizationImpl extends DynamicEntity implements Organization {

    public OrganizationImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<Organization> {
        public ProvidedFactory() {
            super(Organization.class, OrganizationImpl::new);
        }
    }
}
