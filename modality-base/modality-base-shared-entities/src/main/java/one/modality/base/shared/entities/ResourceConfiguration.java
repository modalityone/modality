package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasOnline;
import one.modality.base.shared.entities.markers.EntityHasSiteAndItem;
import one.modality.base.shared.entities.markers.HasName;

import java.time.LocalDate;

public interface ResourceConfiguration extends Entity,
    EntityHasSiteAndItem,
    EntityHasOnline,
    HasName {

    String resource = "resource";
    String name = "name";
    String endDate = "endDate";
    String startDate = "startDate";
    String lastCleaningDate = "lastCleaningDate";
    String allowsMale = "allowsMale";
    String allowsFemale = "allowsFemale";
    String allowsGuest = "allowsGuest";
    String allowsSpecialGuest = "allowsSpecialGuest";
    String allowsVolunteer = "allowsVolunteer";
    String allowsResident = "allowsResident";
    String allowsResidentFamily = "allowsResidentFamily";
    String max = "max";

    default void setResource(Object value) {
        setForeignField(resource, value);
    }

    default EntityId getResourceId() {
        return getForeignEntityId(resource);
    }

    default Resource getResource() {
        return getForeignEntity(resource);
    }

    @Override
    default String getName() {
        return (String) evaluate(name);
    }

    @Override
    default void setName(String value) {
        setExpressionValue(parseExpression(name), value);
    }

    default void setEndDate(LocalDate value) {
        setFieldValue(endDate, value);
    }

    default LocalDate getStartDate() {
        return getLocalDateFieldValue(startDate);
    }

    default void setStartDate(LocalDate value) {
        setFieldValue(startDate, value);
    }

    default LocalDate getEndDate() {
        return getLocalDateFieldValue(endDate);
    }

    default void setLastCleaningDate(LocalDate value) {
        setFieldValue(lastCleaningDate, value);
    }

    default LocalDate getLastCleaningDate() {
        return getLocalDateFieldValue(lastCleaningDate);
    }

    default Boolean allowsMale() {
        return getBooleanFieldValue(allowsMale);
    }

    default void setAllowsMale(Boolean value) {
        setFieldValue(allowsMale, value);
    }

    default Boolean allowsFemale() {
        return getBooleanFieldValue(allowsFemale);
    }

    default void setAllowsFemale(Boolean value) {
        setFieldValue(allowsFemale, value);
    }

    default Boolean allowsGuest() {
        return getBooleanFieldValue(allowsGuest);
    }

    default void setAllowsGuest(Boolean value) {
        setFieldValue(allowsGuest, value);
    }

    default Boolean allowsSpecialGuest() {
        return getBooleanFieldValue(allowsSpecialGuest);
    }

    default void setAllowsSpecialGuest(Boolean value) {
        setFieldValue(allowsSpecialGuest, value);
    }

    default Boolean allowsVolunteer() {
        return getBooleanFieldValue(allowsVolunteer);
    }

    default void setAllowsVolunteer(Boolean value) {
        setFieldValue(allowsVolunteer, value);
    }

    default Boolean allowsResident() {
        return getBooleanFieldValue(allowsResident);
    }

    default void setAllowsResident(Boolean value) {
        setFieldValue(allowsResident, value);
    }

    default Boolean allowsResidentFamily() {
        return getBooleanFieldValue(allowsResidentFamily);
    }

    default void setAllowsResidentFamily(Boolean value) {
        setFieldValue(allowsResidentFamily, value);
    }

    default Integer getMax() {
        return getIntegerFieldValue(max);
    }

    default void setMax(int value) {
        setFieldValue(max, value);
    }
}