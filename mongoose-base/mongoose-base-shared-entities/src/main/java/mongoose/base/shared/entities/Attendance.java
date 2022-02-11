package mongoose.base.shared.entities;

import mongoose.base.shared.entities.markers.EntityHasDate;
import mongoose.base.shared.entities.markers.EntityHasDocumentLine;
import dev.webfx.framework.shared.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface Attendance extends Entity, EntityHasDocumentLine, EntityHasDate {

}
