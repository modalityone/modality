package org.modality_project.base.shared.entities;

import org.modality_project.base.shared.entities.markers.EntityHasArrivalSiteAndItem;
import org.modality_project.base.shared.entities.markers.EntityHasCancelled;
import org.modality_project.base.shared.entities.markers.EntityHasDocument;

/**
 * @author Bruno Salmon
 */
public interface DocumentLine extends
        EntityHasDocument,
        EntityHasCancelled,
        EntityHasArrivalSiteAndItem {

}
