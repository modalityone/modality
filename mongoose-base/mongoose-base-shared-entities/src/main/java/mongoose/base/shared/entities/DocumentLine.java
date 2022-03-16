package mongoose.base.shared.entities;

import mongoose.base.shared.entities.markers.EntityHasArrivalSiteAndItem;
import mongoose.base.shared.entities.markers.EntityHasCancelled;
import mongoose.base.shared.entities.markers.EntityHasDocument;

/**
 * @author Bruno Salmon
 */
public interface DocumentLine extends
        EntityHasDocument,
        EntityHasCancelled,
        EntityHasArrivalSiteAndItem {

}
