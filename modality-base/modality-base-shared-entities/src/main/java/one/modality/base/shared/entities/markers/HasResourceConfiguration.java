package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.EntityId;

import one.modality.base.shared.entities.ResourceConfiguration;

/**
 * @author Bruno Salmon
 */
public interface HasResourceConfiguration {

    void setResourceConfiguration(Object resourceConfiguration);

    EntityId getResourceConfigurationId();

    ResourceConfiguration getResourceConfiguration();
}
