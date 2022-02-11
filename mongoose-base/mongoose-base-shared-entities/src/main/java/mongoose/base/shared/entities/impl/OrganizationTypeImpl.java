package mongoose.base.shared.entities.impl;

import mongoose.base.shared.entities.OrganizationType;
import dev.webfx.framework.shared.orm.entity.EntityId;
import dev.webfx.framework.shared.orm.entity.EntityStore;
import dev.webfx.framework.shared.orm.entity.impl.DynamicEntity;
import dev.webfx.framework.shared.orm.entity.impl.EntityFactoryProviderImpl;

/**
 * @author Bruno Salmon
 */
public final class OrganizationTypeImpl extends DynamicEntity implements OrganizationType {

    public OrganizationTypeImpl(EntityId id, EntityStore store) {
        super(id, store);
    }

    public static final class ProvidedFactory extends EntityFactoryProviderImpl<OrganizationType> {
        public ProvidedFactory() {
            super(OrganizationType.class, OrganizationTypeImpl::new);
        }
    }
}
