package org.modality_project.base.shared.entities.markers;

import org.modality_project.base.shared.entities.Item;
import dev.webfx.stack.framework.shared.orm.entity.EntityId;

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
