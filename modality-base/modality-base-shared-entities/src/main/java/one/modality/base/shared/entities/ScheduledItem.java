package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.EntityHasDate;
import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasSiteAndItem;

/**
 * @author Bruno Salmon
 */
public interface ScheduledItem
    extends Entity, EntityHasEvent, EntityHasDate, EntityHasSiteAndItem {}
