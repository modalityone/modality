package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasCountry;
import one.modality.base.shared.entities.markers.EntityHasIcon;
import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * @author Bruno Salmon
 */
public interface Organization extends
    EntityHasName,
    EntityHasLabel,
    EntityHasIcon,
    EntityHasCountry {

    String closed = "closed";
    String type = "type";
    String kdmCenter = "kdmCenter";
    String latitude = "latitude";
    String longitude = "longitude";
    String importIssue = "importIssue";
    String language = "language";
    String teachingsDayTicketItem = "teachingsDayTicketItem";
    String globalSite = "globalSite";
    String termsUrlLabel = "termsUrlLabel";
    String privacyUrlLabel = "privacyUrlLabel";

    default void setClosed(boolean value) { setFieldValue(closed, value); }

    default void setType(Object value) {
        setForeignField(type, value);
    }

    default EntityId getTypeId() {
        return getForeignEntityId(type);
    }

    default OrganizationType getType() {
        return getForeignEntity(type);
    }

    default void setKdmCenter(Object value) {
        setForeignField(kdmCenter, value);
    }

    default EntityId getKdmCenterId() {
        return getForeignEntityId(kdmCenter);
    }

    default KdmCenter getKdmCenter() {
        return getForeignEntity(kdmCenter);
    }

    default Float getLatitude() {
        return getFloatFieldValue(latitude);
    }

    default void setLatitude(Float value) {
        setFieldValue(latitude, value);
    }

    default Float getLongitude() {
        return getFloatFieldValue(longitude);
    }

    default void setLongitude(Float value) {
        setFieldValue(longitude, value);
    }

    default void setImportIssue(String value) {
        setFieldValue(importIssue, value);
    }

    default String getImportIssue() {
        return getStringFieldValue(importIssue);
    }

    default void setLanguage(Object value) {
        setForeignField(language, value);
    }

    default EntityId getLanguageId() {
        return getForeignEntityId(language);
    }

    default Language getLanguage() {
        return getForeignEntity(language);
    }

    default void setTeachingsDayTicketItem(Object value) {
        setForeignField(teachingsDayTicketItem, value);
    }

    default EntityId getTeachingsDayTicketItemId() {
        return getForeignEntityId(teachingsDayTicketItem);
    }

    default Item getTeachingsDayTicketItem() {
        return getForeignEntity(teachingsDayTicketItem);
    }

    default void setGlobalSite(Object value) {
        setForeignField(globalSite, value);
    }

    default EntityId getGlobalSiteId() {
        return getForeignEntityId(globalSite);
    }

    default Site getGlobalSite() {
        return getForeignEntity(globalSite);
    }

    default void setTermsUrlLabel(Object value) {
        setForeignField(termsUrlLabel, value);
    }

    default EntityId getTermsUrlLabelId() {
        return getForeignEntityId(termsUrlLabel);
    }

    default Label getTermsUrlLabel() {
        return getForeignEntity(termsUrlLabel);
    }

    default void setPrivacyUrlLabel(Object value) {
        setForeignField(privacyUrlLabel, value);
    }

    default EntityId getPrivacyUrlLabelId() {
        return getForeignEntityId(privacyUrlLabel);
    }

    default Label getPrivacyUrlLabel() {
        return getForeignEntity(privacyUrlLabel);
    }

}