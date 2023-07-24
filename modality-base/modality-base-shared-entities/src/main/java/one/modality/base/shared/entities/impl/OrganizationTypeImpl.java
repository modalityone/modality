package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.OrganizationType;

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
