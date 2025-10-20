package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasI18nFields;
import one.modality.base.shared.entities.markers.EntityHasIcon;
import one.modality.base.shared.entities.markers.EntityHasOrganization;

/**
 * @author Bruno Salmon
 */
public interface Label extends
    EntityHasIcon,
    EntityHasOrganization,
    EntityHasI18nFields {

    String livestreamMessage = "livestreamMessage";

    default void setLivestreamMessage(Boolean value) {
        setFieldValue(livestreamMessage, value);
    }

    default Boolean isLivestreamMessage() {
        return getBooleanFieldValue(livestreamMessage);
    }

}