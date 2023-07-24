package one.modality.base.shared.entities.impl;

import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.impl.DynamicEntity;
import dev.webfx.stack.orm.entity.impl.EntityFactoryProviderImpl;
import one.modality.base.shared.entities.ResourceConfiguration;

/**
 * @author Bruno Salmon
 */
public final class ResourceConfigurationImpl extends DynamicEntity
    implements ResourceConfiguration {

  public ResourceConfigurationImpl(EntityId id, EntityStore store) {
    super(id, store);
  }

  public static final class ProvidedFactory
      extends EntityFactoryProviderImpl<ResourceConfiguration> {
    public ProvidedFactory() {
      super(ResourceConfiguration.class, ResourceConfigurationImpl::new);
    }
  }
}
