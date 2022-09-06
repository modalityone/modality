package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasCode;
import one.modality.base.shared.entities.markers.EntityHasIcon;
import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;

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
