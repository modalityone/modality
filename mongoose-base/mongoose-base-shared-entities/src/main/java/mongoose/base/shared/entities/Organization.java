package mongoose.base.shared.entities;

import mongoose.base.shared.entities.markers.EntityHasLabel;
import mongoose.base.shared.entities.markers.EntityHasName;
import dev.webfx.framework.shared.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface Organization extends Entity, EntityHasName, EntityHasLabel {
}
