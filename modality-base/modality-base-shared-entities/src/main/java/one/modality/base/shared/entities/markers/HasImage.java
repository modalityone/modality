package one.modality.base.shared.entities.markers;

import one.modality.base.shared.entities.Image;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasImage {

    void setImage(Object event);

    EntityId getImageId();

    Image getImage();

}
