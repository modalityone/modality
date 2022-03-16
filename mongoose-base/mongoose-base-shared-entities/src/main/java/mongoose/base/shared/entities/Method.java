package mongoose.base.shared.entities;

import mongoose.base.shared.entities.markers.EntityHasCode;
import mongoose.base.shared.entities.markers.EntityHasIcon;
import mongoose.base.shared.entities.markers.EntityHasLabel;
import mongoose.base.shared.entities.markers.EntityHasName;

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
