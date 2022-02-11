package mongoose.base.shared.entities;

import mongoose.base.shared.entities.markers.EntityHasEvent;
import mongoose.base.shared.entities.markers.EntityHasItemFamily;
import mongoose.base.shared.entities.markers.EntityHasLabel;
import mongoose.base.shared.entities.markers.EntityHasName;
import dev.webfx.framework.shared.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface Site extends Entity, EntityHasName, EntityHasLabel, EntityHasEvent, EntityHasItemFamily {

    //// Domain fields

    default void setMain(Boolean main) {
        setFieldValue("main", main);
    }

    default Boolean isMain() {
        return getBooleanFieldValue("main");
    }

    default String getIcon() { return (String) evaluate("icon"); }

}
