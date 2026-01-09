package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasLocalDate;
import one.modality.base.shared.entities.markers.EntityHasScheduledItem;
import one.modality.base.shared.entities.markers.EntityHasTimeline;

/**
 * @author Bruno Salmon
 */
public interface ScheduledBoundary extends Entity,
    EntityHasEvent,
    EntityHasScheduledItem,
    EntityHasTimeline,
    EntityHasLocalDate
{

}
