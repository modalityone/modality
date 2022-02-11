package mongoose.base.shared.entities.markers;

import mongoose.base.shared.entities.Item;
import dev.webfx.framework.shared.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasItem {

    void setItem(Object item);

    EntityId getItemId();

    Item getItem();

    default boolean hasItem() {
        return getItem() != null;
    }

}
