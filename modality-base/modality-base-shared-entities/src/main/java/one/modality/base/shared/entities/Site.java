package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.*;

/**
 * @author Bruno Salmon
 */
public interface Site
        extends EntityHasName, EntityHasLabel, EntityHasIcon, EntityHasEvent, EntityHasItemFamily {

    //// Domain fields

    default void setMain(Boolean main) {
        setFieldValue("main", main);
    }

    default Boolean isMain() {
        return getBooleanFieldValue("main");
    }
}
