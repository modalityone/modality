package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasArrivalSiteAndItem;
import one.modality.base.shared.entities.markers.EntityHasCancelled;
import one.modality.base.shared.entities.markers.EntityHasDocument;

/**
 * @author Bruno Salmon
 */
public interface DocumentLine extends
        EntityHasDocument,
        EntityHasCancelled,
        EntityHasArrivalSiteAndItem {

}
