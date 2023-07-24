package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;

import one.modality.base.shared.entities.ResourceConfiguration;

/**
 * @author Bruno Salmon
 */
public interface EntityHasResourceConfiguration extends Entity, HasResourceConfiguration {

    @Override
    default void setResourceConfiguration(Object resourceConfiguration) {
        setForeignField("resourceConfiguration", resourceConfiguration);
    }

    @Override
    default EntityId getResourceConfigurationId() {
        return getForeignEntityId("resourceConfiguration");
    }

    @Override
    default ResourceConfiguration getResourceConfiguration() {
        return getForeignEntity("resourceConfiguration");
    }
}
