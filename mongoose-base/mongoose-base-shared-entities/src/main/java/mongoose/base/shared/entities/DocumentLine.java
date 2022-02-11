package mongoose.base.shared.entities;

import mongoose.base.shared.entities.markers.EntityHasArrivalSiteAndItem;
import mongoose.base.shared.entities.markers.EntityHasCancelled;
import mongoose.base.shared.entities.markers.EntityHasDocument;
import dev.webfx.framework.shared.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface DocumentLine extends Entity, EntityHasDocument, EntityHasCancelled, EntityHasArrivalSiteAndItem {

}
