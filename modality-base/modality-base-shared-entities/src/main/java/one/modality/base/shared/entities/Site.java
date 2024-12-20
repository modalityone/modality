package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasIcon;
import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;
import one.modality.base.shared.entities.markers.EntityHasItemFamily;

public interface Site extends
    EntityHasName,
    EntityHasLabel,
    EntityHasIcon,
    EntityHasEvent,
    EntityHasItemFamily {

    String main = "main";

    default void setMain(Boolean value) {
        setFieldValue(main, value);
    }

    default Boolean isMain() {
        return getBooleanFieldValue(main);
    }
}