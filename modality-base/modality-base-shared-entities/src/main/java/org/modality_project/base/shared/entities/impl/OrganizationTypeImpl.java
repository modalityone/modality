package org.modality_project.base.shared.entities.impl;

import org.modality_project.base.shared.entities.OrganizationType;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;

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
