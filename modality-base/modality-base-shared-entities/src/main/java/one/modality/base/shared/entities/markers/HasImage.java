package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.EntityId;

import one.modality.base.shared.entities.Image;

/**
 * @author Bruno Salmon
 */
public interface HasImage {

    void setImage(Object event);

    EntityId getImageId();

    Image getImage();
}
