package org.modality_project.base.shared.entities;

import org.modality_project.base.shared.entities.markers.EntityHasIcon;
import org.modality_project.base.shared.entities.markers.EntityHasOrganization;
import org.modality_project.base.shared.entities.markers.EntityHasName;

public interface Snapshot extends
        EntityHasName,
        EntityHasOrganization,
        EntityHasIcon {
}
