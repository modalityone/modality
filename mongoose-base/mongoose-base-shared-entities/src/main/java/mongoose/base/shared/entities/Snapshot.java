package mongoose.base.shared.entities;

import mongoose.base.shared.entities.markers.EntityHasEvent;
import mongoose.base.shared.entities.markers.EntityHasIcon;
import mongoose.base.shared.entities.markers.EntityHasName;
import mongoose.base.shared.entities.markers.EntityHasOrganization;

public interface Snapshot extends
        EntityHasName,
        EntityHasOrganization,
        EntityHasIcon {
}
