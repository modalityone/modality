package org.modality_project.base.shared.entities;

import org.modality_project.base.shared.entities.markers.EntityHasCode;
import org.modality_project.base.shared.entities.markers.EntityHasIcon;
import org.modality_project.base.shared.entities.markers.EntityHasLabel;
import org.modality_project.base.shared.entities.markers.EntityHasName;

/**
 * @author Bruno Salmon
 */
public interface Method extends
        EntityHasCode,
        EntityHasName,
        EntityHasLabel,
        EntityHasIcon {

    int ONLINE_METHOD_ID = 5; // hardcoded id that matches the database (not beautiful)

}
