package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.ScheduledResource;

/**
 * @author Bruno Salmon
 */
public final class ScheduledResourceImpl extends DynamicEntity implements ScheduledResource {

  public ScheduledResourceImpl(EntityId id, EntityStore store) {
    super(id, store);
  }

  public static final class ProvidedFactory extends EntityFactoryProviderImpl<ScheduledResource> {
    public ProvidedFactory() {
      super(ScheduledResource.class, ScheduledResourceImpl::new);
    }
  }
}
