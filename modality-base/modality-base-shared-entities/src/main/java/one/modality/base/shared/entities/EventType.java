package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;
import one.modality.base.shared.entities.markers.EntityHasOrganization;

/**
 * @author Bruno Salmon
 */
public interface EventType extends Entity,
        EntityHasName,
        EntityHasLabel,
        EntityHasOrganization {

    default void setRecurring(Boolean live) {
        setFieldValue("recurring", live);
    }

    default Boolean isRecurring() {
        return getBooleanFieldValue("recurring");
    }

    default void setOrd(Integer ord) {
        setFieldValue("ord", ord);
    }

    default Integer getOrd() {
        return getIntegerFieldValue("ord");
    }

}
