package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.EntityHasLocalDate;
import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasSiteAndItem;

/**
 * @author Bruno Salmon
 */
public interface ScheduledItem extends Entity,
        EntityHasEvent,
        EntityHasLocalDate,
        EntityHasSiteAndItem {

}
