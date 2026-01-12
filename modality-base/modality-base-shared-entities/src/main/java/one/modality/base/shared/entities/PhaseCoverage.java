package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * @author Bruno Salmon
 */
public interface PhaseCoverage extends Entity,
    EntityHasEvent,
    EntityHasName,
    EntityHasLabel
{
    String phase1 = "phase1";
    String phase2 = "phase2";
    String phase3 = "phase3";

    default void setPhase1(Object value) {
        setForeignField(phase1, value);
    }

    default EntityId getPhase1Id() {
        return getForeignEntityId(phase1);
    }

    default EventPhase getPhase1() {
        return getForeignEntity(phase1);
    }

    default void setPhase2(Object value) {
        setForeignField(phase2, value);
    }

    default EntityId getPhase2Id() {
        return getForeignEntityId(phase2);
    }

    default EventPhase getPhase2() {
        return getForeignEntity(phase2);
    }

    default void setPhase3(Object value) {
        setForeignField(phase3, value);
    }

    default EntityId getPhase3Id() {
        return getForeignEntityId(phase3);
    }

    default EventPhase getPhase3() {
        return getForeignEntity(phase3);
    }

}
