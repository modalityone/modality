package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.EntityHasCode;
import one.modality.base.shared.entities.markers.EntityHasIcon;
import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * @author Bruno Salmon
 */
public interface Language extends Entity,
    EntityHasName,
    EntityHasIcon,
    EntityHasCode {

    String supported = "supported";

    default Boolean isSupported() {
        return getBooleanFieldValue(supported);
    }

    default void setSupported(Boolean value) {
        setFieldValue(supported, value);
    }

}