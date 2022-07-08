package org.modality_project.base.shared.entities;

import org.modality_project.base.shared.entities.markers.*;
import org.modality_project.base.shared.entities.markers.*;

/**
 * @author Bruno Salmon
 */
public interface Site extends
        EntityHasName,
        EntityHasLabel,
        EntityHasIcon,
        EntityHasEvent,
        EntityHasItemFamily {

    //// Domain fields

    default void setMain(Boolean main) {
        setFieldValue("main", main);
    }

    default Boolean isMain() {
        return getBooleanFieldValue("main");
    }

}
