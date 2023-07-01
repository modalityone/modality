package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.EntityHasSiteAndItem;
import one.modality.base.shared.entities.markers.HasName;

import java.time.LocalDate;

public interface ResourceConfiguration extends Entity,
        EntityHasSiteAndItem,
        HasName {

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

    default LocalDate getEndDate() {
        return getLocalDateFieldValue("endDate");
    }

    default void setLastCleaningDate(LocalDate endDate) {
        setFieldValue("lastCleaningDate", endDate);
    }

    default LocalDate getLastCleaningDate() {
        return getLocalDateFieldValue("lastCleaningDate");
    }

    default boolean allowsMale() {
        return getBooleanFieldValue("allowsMale");
    }

    default void setAllowsMale(boolean allowsMale) {
        setFieldValue("allowsMale", allowsMale);
    }

    default boolean allowsFemale() {
        return getBooleanFieldValue("allowsFemale");
    }

    default void setAllowsFemale(boolean allowsFemale) {
        setFieldValue("allowsFemale", allowsFemale);
    }

    default boolean allowsGuest() {
        return getBooleanFieldValue("allowsGuest");
    }

    default void setAllowsGuest(boolean allowsGuest) {
        setFieldValue("allowsGuest", allowsGuest);
    }

    default boolean allowsSpecialGuest() {
        return getBooleanFieldValue("allowsSpecialGuest");
    }

    default void setAllowsSpecialGuest(boolean allowsSpecialGuest) {
        setFieldValue("allowsSpecialGuest", allowsSpecialGuest);
    }

    default boolean allowsVolunteer() {
        return getBooleanFieldValue("allowsVolunteer");
    }

    default void setAllowsVolunteer(boolean allowsVolunteer) {
        setFieldValue("allowsVolunteer", allowsVolunteer);
    }

    default boolean allowsResident() {
        return getBooleanFieldValue("allowsResident");
    }

    default void setAllowsResident(boolean allowsResident) {
        setFieldValue("allowsResident", allowsResident);
    }

    default boolean allowsResidentFamily() {
        return getBooleanFieldValue("allowsResidentFamily");
    }

    default void setAllowsResidentFamily(boolean allowsResidentFamily) {
        setFieldValue("allowsResidentFamily", allowsResidentFamily);
    }
}
