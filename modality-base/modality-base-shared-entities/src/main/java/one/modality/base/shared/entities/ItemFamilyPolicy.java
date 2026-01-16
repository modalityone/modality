package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasItemFamily;

import java.util.List;

/**
 * @author Bruno Salmon
 */
public interface ItemFamilyPolicy extends Entity,
    EntityHasItemFamily
{

    String scope = "scope";
    String eventPhaseCoverage1 = "eventPhaseCoverage1";
    String eventPhaseCoverage2 = "eventPhaseCoverage2";
    String eventPhaseCoverage3 = "eventPhaseCoverage3";
    String eventPhaseCoverage4 = "eventPhaseCoverage4";

    default void setScope(Object value) {
        setForeignField(scope, value);
    }

    default EntityId getScopeId() {
        return getForeignEntityId(scope);
    }

    default PolicyScope getScope() {
        return getForeignEntity(scope);
    }

    default void setEventPhaseCoverage1(Object value) {
        setFieldValue(eventPhaseCoverage1, value);
    }

    default EntityId getEventPhaseCoverage1Id() {
        return getForeignEntityId(eventPhaseCoverage1);
    }

    default EventPhaseCoverage getEventPhaseCoverage1() {
        return getForeignEntity(eventPhaseCoverage1);
    }

    default void setEventPhaseCoverage2(Object value) {
        setFieldValue(eventPhaseCoverage2, value);
    }

    default EntityId getEventPhaseCoverage2Id() {
        return getForeignEntityId(eventPhaseCoverage2);
    }

    default EventPhaseCoverage getEventPhaseCoverage2() {
        return getForeignEntity(eventPhaseCoverage2);
    }

    default void setEventPhaseCoverage3(Object value) {
        setFieldValue(eventPhaseCoverage3, value);
    }

    default EntityId getEventPhaseCoverage3Id() {
        return getForeignEntityId(eventPhaseCoverage3);
    }

    default EventPhaseCoverage getEventPhaseCoverage3() {
        return getForeignEntity(eventPhaseCoverage3);
    }

    default void setEventPhaseCoverage4(Object value) {
        setFieldValue(eventPhaseCoverage4, value);
    }

    default EntityId getEventPhaseCoverage4Id() {
        return getForeignEntityId(eventPhaseCoverage4);
    }

    default EventPhaseCoverage getEventPhaseCoverage4() {
        return getForeignEntity(eventPhaseCoverage4);
    }

    List<EventPhaseCoverage> getEventPhaseCoverages(); // implemented in ItemFamilyPolicyImpl

    void setEventPhaseCoverages(List<EventPhaseCoverage> phaseCoverages); // implemented in ItemFamilyPolicyImpl

}
