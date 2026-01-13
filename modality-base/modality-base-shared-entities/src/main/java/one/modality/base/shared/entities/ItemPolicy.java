package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.*;

/**
 * @author Bruno Salmon
 */
public interface ItemPolicy extends Entity,
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
    String minOccupancy = "minOccupancy";

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

    default void setMinOccupancy(Integer value) {
        setFieldValue(minOccupancy, value);
    }

    default Integer getMinOccupancy() {
        return getIntegerFieldValue(minOccupancy);
    }


}
