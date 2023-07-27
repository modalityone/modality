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

    default void setResource(Object site) {
        setForeignField("resource", site);
    }

    default EntityId getResourceId() {
        return getForeignEntityId("resource");
    }

    default Resource getResource() {
        return getForeignEntity("resource");
    }

    @Override
    default String getName() {
        return (String) evaluate("name");
    }

    @Override
    default void setName(String name) {
        setExpressionValue(parseExpression("name"), name);
    }

    default void setEndDate(LocalDate endDate) {
        setFieldValue("endDate", endDate);
    }

    default LocalDate getStartDate() {
        return getLocalDateFieldValue("startDate");
    }

    default void setStartDate(LocalDate startDate) {
        setFieldValue("startDate", startDate);
    }

    default LocalDate getEndDate() {
        return getLocalDateFieldValue("endDate");
    }

    default void setLastCleaningDate(LocalDate endDate) {
        setFieldValue("lastCleaningDate", endDate);
    }

    default LocalDate getLastCleaningDate() {
        return getLocalDateFieldValue("lastCleaningDate");
    }

    default Boolean allowsMale() {
        return getBooleanFieldValue("allowsMale");
    }

    default void setAllowsMale(Boolean allowsMale) {
        setFieldValue("allowsMale", allowsMale);
    }

    default Boolean allowsFemale() {
        return getBooleanFieldValue("allowsFemale");
    }

    default void setAllowsFemale(Boolean allowsFemale) {
        setFieldValue("allowsFemale", allowsFemale);
    }

    default Boolean allowsGuest() {
        return getBooleanFieldValue("allowsGuest");
    }

    default void setAllowsGuest(Boolean allowsGuest) {
        setFieldValue("allowsGuest", allowsGuest);
    }

    default Boolean allowsSpecialGuest() {
        return getBooleanFieldValue("allowsSpecialGuest");
    }

    default void setAllowsSpecialGuest(Boolean allowsSpecialGuest) {
        setFieldValue("allowsSpecialGuest", allowsSpecialGuest);
    }

    default Boolean allowsVolunteer() {
        return getBooleanFieldValue("allowsVolunteer");
    }

    default void setAllowsVolunteer(Boolean allowsVolunteer) {
        setFieldValue("allowsVolunteer", allowsVolunteer);
    }

    default Boolean allowsResident() {
        return getBooleanFieldValue("allowsResident");
    }

    default void setAllowsResident(Boolean allowsResident) {
        setFieldValue("allowsResident", allowsResident);
    }

    default Boolean allowsResidentFamily() {
        return getBooleanFieldValue("allowsResidentFamily");
    }

    default void setAllowsResidentFamily(Boolean allowsResidentFamily) {
        setFieldValue("allowsResidentFamily", allowsResidentFamily);
    }

    default Integer getMax() {
        return getIntegerFieldValue("max");
    }

    default void setMax(int max) {
        setFieldValue("max", max);
    }
}
