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
    String phaseCoverage1 = "phaseCoverage1";
    String phaseCoverage2 = "phaseCoverage2";
    String phaseCoverage3 = "phaseCoverage3";
    String phaseCoverage4 = "phaseCoverage4";

    default void setScope(Object value) {
        setForeignField(scope, value);
    }

    default EntityId getScopeId() {
        return getForeignEntityId(scope);
    }

    default PolicyScope getScope() {
        return getForeignEntity(scope);
    }

    default void setPhaseCoverage1(Object value) {
        setFieldValue(phaseCoverage1, value);
    }

    default EntityId getPhaseCoverage1Id() {
        return getForeignEntityId(phaseCoverage1);
    }

    default EventPhaseCoverage getPhaseCoverage1() {
        return getForeignEntity(phaseCoverage1);
    }

    default void setPhaseCoverage2(Object value) {
        setFieldValue(phaseCoverage2, value);
    }

    default EntityId getPhaseCoverage2Id() {
        return getForeignEntityId(phaseCoverage2);
    }

    default EventPhaseCoverage getPhaseCoverage2() {
        return getForeignEntity(phaseCoverage2);
    }

    default void setPhaseCoverage3(Object value) {
        setFieldValue(phaseCoverage3, value);
    }

    default EntityId getPhaseCoverage3Id() {
        return getForeignEntityId(phaseCoverage3);
    }

    default EventPhaseCoverage getPhaseCoverage3() {
        return getForeignEntity(phaseCoverage3);
    }

    default void setPhaseCoverage4(Object value) {
        setFieldValue(phaseCoverage4, value);
    }

    default EntityId getPhaseCoverage4Id() {
        return getForeignEntityId(phaseCoverage4);
    }

    default EventPhaseCoverage getPhaseCoverage4() {
        return getForeignEntity(phaseCoverage4);
    }

    List<EventPhaseCoverage> getPhaseCoverages(); // implemented in ItemFamilyPolicyImpl

    void setPhaseCoverages(List<EventPhaseCoverage> phaseCoverages); // implemented in ItemFamilyPolicyImpl

}
