package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasName;

public interface BuildingZone extends
    EntityHasName {

    String building = "building";

    default void setBuilding(Object value) {
        setForeignField(building, value);
    }

    default EntityId getBuildingId() {
        return getForeignEntityId(building);
    }

    default Building getBuilding() {
        return getForeignEntity(building);
    }

}