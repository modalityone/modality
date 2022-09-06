package one.modality.base.shared.entities.markers;

import one.modality.base.shared.entities.Item;
import dev.webfx.stack.orm.entity.EntityId;

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
