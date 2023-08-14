package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasIcon;
import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * @author Bruno Salmon
 */
public interface Organization extends
        EntityHasName,
        EntityHasLabel,
        EntityHasIcon {

    default void setKdmCenter(KdmCenter kdmCenter) {
        setForeignField("kdmCenter", kdmCenter);
    }

    default EntityId getKdmCenterId() {
        return getForeignEntityId("kdmCenter");
    }

    default KdmCenter getKdmCenter() {
        return getForeignEntity("kdmCenter");
    }
}
