package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.*;

public interface Site extends
    EntityHasName,
    EntityHasLabel,
    EntityHasIcon,
    EntityHasEvent,
    EntityHasOrganization,
    EntityHasItemFamily,
    EntityHasOrd {

    String main = "main";

    default void setMain(Boolean value) {
        setFieldValue(main, value);
    }

    default Boolean isMain() {
        return getBooleanFieldValue(main);
    }
}