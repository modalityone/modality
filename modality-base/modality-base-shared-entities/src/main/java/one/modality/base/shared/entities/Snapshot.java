package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasIcon;
import one.modality.base.shared.entities.markers.EntityHasOrganization;
import one.modality.base.shared.entities.markers.EntityHasName;

public interface Snapshot extends
        EntityHasName,
        EntityHasOrganization,
        EntityHasIcon {
}
