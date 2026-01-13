package one.modality.base.shared.entities;

import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;

import java.util.List;

/**
 * @author Bruno Salmon
 */
public interface PhaseCoverage extends Entity,
    EntityHasEvent,
    EntityHasName,
    EntityHasLabel,
    BoundaryPeriod // Can be used as a BoundaryPeriod when all phases are consecutive (no discontinuity)
{
    String phase1 = "phase1";
    String phase2 = "phase2";
    String phase3 = "phase3";
    String phase4 = "phase4";

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

    default void setPhase4(Object value) {
        setForeignField(phase4, value);
    }

    default EntityId getPhase4Id() {
        return getForeignEntityId(phase4);
    }

    default EventPhase getPhase4() {
        return getForeignEntity(phase4);
    }

    List<EventPhase> getPhases(); // implemented in PhaseCoverageImpl

    void setPhases(List<EventPhase> phases); // implemented in PhaseCoverageImpl

    @Override
    default ScheduledBoundary getStartBoundary() {
        return getPhase1().getStartBoundary();
    }

    @Override
    default ScheduledBoundary getEndBoundary() {
        return Collections.last(getPhases()).getEndBoundary();
    }

}
