package mongoose.shared.entities.impl;

import mongoose.shared.entities.Organization;
import dev.webfx.framework.shared.orm.entity.EntityId;
import dev.webfx.framework.shared.orm.entity.EntityStore;
import dev.webfx.framework.shared.orm.entity.impl.DynamicEntity;
import dev.webfx.framework.shared.orm.entity.impl.EntityFactoryProviderImpl;

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
