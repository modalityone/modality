package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.*;

import java.time.LocalDate;

public interface ResourceConfiguration extends Entity,
    EntityHasSiteAndItem,
    EntityHasResource,
    EntityHasOnline,
    EntityHasEvent,
    HasName {

    String name = "name";
    String endDate = "endDate";
    String startDate = "startDate";
    String allowsMale = "allowsMale";
    String allowsFemale = "allowsFemale";
    String allowsGuest = "allowsGuest";
    String allowsSpecialGuest = "allowsSpecialGuest";
    String allowsVolunteer = "allowsVolunteer";
    String allowsResident = "allowsResident";
    String allowsResidentFamily = "allowsResidentFamily";
    String max = "max";
    String comment = "comment";

    @Override
    default String getName() {
        return evaluate(name);
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

    default String getComment() {
        return getStringFieldValue(comment);
    }

    default void setComment(String value) {
        setFieldValue(comment, value);
    }
}