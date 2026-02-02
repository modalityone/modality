package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasI18nFields;
import one.modality.base.shared.entities.markers.EntityHasName;
import one.modality.base.shared.entities.markers.EntityHasOrganization;

/**
 * @author Bruno Salmon
 */
public interface Letter extends
    //EntityHasIcon,
    EntityHasName,
    EntityHasOrganization,
    EntityHasEvent,
    EntityHasI18nFields {
}