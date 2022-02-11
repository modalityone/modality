package mongoose.base.shared.entities.markers;

import dev.webfx.framework.shared.orm.entity.Entity;
import dev.webfx.framework.shared.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasParent<P extends Entity> {

    void setParent(Object parent);

    EntityId getParentId();

    P getParent();

}
