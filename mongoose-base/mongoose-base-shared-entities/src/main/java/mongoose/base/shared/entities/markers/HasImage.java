package mongoose.base.shared.entities.markers;

import mongoose.base.shared.entities.Image;
import dev.webfx.framework.shared.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasImage {

    void setImage(Object event);

    EntityId getImageId();

    Image getImage();

}
