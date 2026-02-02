package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.ScheduledItem;

/**
 * @author Bruno Salmon
 */
public interface HasScheduledItem {

    void setScheduledItem(Object scheduledItem);

    EntityId getScheduledItemId();

    ScheduledItem getScheduledItem();

}
