package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasEventType;
import one.modality.base.shared.entities.markers.EntityHasOrganization;
import one.modality.base.shared.entities.markers.EntityHasSite;

/**
 * @author Bruno Salmon
 */
public interface PolicyScope extends Entity,
    EntityHasOrganization,
    EntityHasSite,
    EntityHasEventType,
    EntityHasEvent {

}
