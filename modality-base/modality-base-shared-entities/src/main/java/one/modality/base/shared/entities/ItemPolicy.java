package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.*;

/**
 * @author Bruno Salmon
 */
public interface ItemPolicy extends Entity,
    EntityHasItemFamily,
    EntityHasItem
{

    String scope = "scope";
    String descriptionLabel = "descriptionLabel";
    String noticeLabel = "noticeLabel";
    String minDay = "minDay";
    String _default = "default";
    String earlyAccommodationAllowed = "earlyAccommodationAllowed";
    String lateAccommodationAllowed = "lateAccommodationAllowed";
    String genderInfoRequired = "genderRequired";
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

    default void setDescriptionLabel(Object value) {
        setForeignField(descriptionLabel, value);
    }

    default EntityId getDescriptionLabelId() {
        return getForeignEntityId(descriptionLabel);
    }

    default Label getDescriptionLabel() {
        return getForeignEntity(noticeLabel);
    }

    default void setNoticeLabel(Object value) {
        setForeignField(noticeLabel, value);
    }

    default EntityId getNoticeLabelId() {
        return getForeignEntityId(noticeLabel);
    }

    default Label getNoticeLabel() {
        return getForeignEntity(descriptionLabel);
    }

    default void setMinDay(Integer value) {
        setFieldValue(minDay, value);
    }

    default Integer getMinDay() {
        return getIntegerFieldValue(minDay);
    }

    default void setDefault(Boolean value) {
        setFieldValue(_default, value);
    }

    default Boolean isDefault() {
        return getBooleanFieldValue(_default);
    }

    default void setGenderInfoRequired(Boolean value) {
        setFieldValue(genderInfoRequired, value);
    }

    default Boolean isGenderInfoRequired() {
        return getBooleanFieldValue(genderInfoRequired);
    }

    default void setEarlyAccommodationAllowed(Boolean value) {
        setFieldValue(earlyAccommodationAllowed, value);
    }

    default Boolean isEarlyAccommodationAllowed() {
        return getBooleanFieldValue(earlyAccommodationAllowed);
    }

    default void setLateAccommodationAllowed(Boolean value) {
        setFieldValue(lateAccommodationAllowed, value);
    }

    default Boolean isLateAccommodationAllowed() {
        return getBooleanFieldValue(lateAccommodationAllowed);
    }

    default void setPhaseCoverage1(Boolean value) {
        setFieldValue(phaseCoverage1, value);
    }

    default EntityId getPhaseCoverage1Id() {
        return getForeignEntityId(phaseCoverage1);
    }

    default PhaseCoverage getPhaseCoverage1() {
        return getForeignEntity(phaseCoverage1);
    }

    default void setPhaseCoverage2(Boolean value) {
        setFieldValue(phaseCoverage2, value);
    }

    default EntityId getPhaseCoverage2Id() {
        return getForeignEntityId(phaseCoverage2);
    }

    default PhaseCoverage getPhaseCoverage2() {
        return getForeignEntity(phaseCoverage2);
    }

    default void setPhaseCoverage3(Boolean value) {
        setFieldValue(phaseCoverage3, value);
    }

    default EntityId getPhaseCoverage3Id() {
        return getForeignEntityId(phaseCoverage3);
    }

    default PhaseCoverage getPhaseCoverage3() {
        return getForeignEntity(phaseCoverage3);
    }

    default void setPhaseCoverage4(Boolean value) {
        setFieldValue(phaseCoverage4, value);
    }

    default EntityId getPhaseCoverage4Id() {
        return getForeignEntityId(phaseCoverage4);
    }

    default PhaseCoverage getPhaseCoverage4() {
        return getForeignEntity(phaseCoverage4);
    }

}
