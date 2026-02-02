package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasEvent;
import one.modality.base.shared.entities.markers.EntityHasPersonalDetails;
import dev.webfx.stack.orm.entity.Entity;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public interface Person extends Entity, EntityHasPersonalDetails, EntityHasEvent {
    // Field name constants
    String birthDate = "birthdate";
    String frontendAccount = "frontendAccount";
    String accountPerson = "accountPerson";
    String branch = "branch";
    String thirdParty = "thirdParty";
    String language = "language";
    String carer1 = "carer1";
    String carer2 = "carer2";
    String name = "name";
    String abcNames = "abc_names";
    String countryGeonameid = "countryGeonameid";
    String countryCode = "countryCode";
    String cityGeonameid = "cityGeonameid";
    String cityLatitude = "cityLatitude";
    String cityLongitude = "cityLongitude";
    String cityTimezone = "cityTimezone";
    String latitude = "latitude";
    String longitude = "longitude";
    String removed = "removed";
    String neverBooked = "neverBooked";
    String organizationName = "organizationName";
    String nationality = "nationality";
    String passport = "passport";
    String emailingList = "emailingList";
    String owner = "owner";

    // Birth date
    default void setBirthDate(LocalDate value) {
        setFieldValue(birthDate, value);
    }

    default LocalDate getBirthDate() {
        return getLocalDateFieldValue(birthDate);
    }

    // Frontend account
    default void setFrontendAccount(Object value) {
        setForeignField(frontendAccount, value);
    }

    default EntityId getFrontendAccountId() {
        return getForeignEntityId(frontendAccount);
    }

    default FrontendAccount getFrontendAccount() {
        return getForeignEntity(frontendAccount);
    }

    // Account person
    default void setAccountPerson(Object value) {
        setForeignField(accountPerson, value);
    }

    default EntityId getAccountPersonId() {
        return getForeignEntityId(accountPerson);
    }

    default Person getAccountPerson() {
        return getForeignEntity(accountPerson);
    }

    // Branch
    default void setBranch(Object value) {
        setForeignField(branch, value);
    }

    default EntityId getBranchId() {
        return getForeignEntityId(branch);
    }

    default Entity getBranch() {
        return getForeignEntity(branch);
    }

    // Third party
    default void setThirdParty(Object value) {
        setForeignField(thirdParty, value);
    }

    default EntityId getThirdPartyId() {
        return getForeignEntityId(thirdParty);
    }

    default Entity getThirdParty() {
        return getForeignEntity(thirdParty);
    }

    // Language
    default void setLanguage(Object value) {
        setForeignField(language, value);
    }

    default EntityId getLanguageId() {
        return getForeignEntityId(language);
    }

    default Entity getLanguage() {
        return getForeignEntity(language);
    }

    // Carer1
    default void setCarer1(Object value) {
        setForeignField(carer1, value);
    }

    default EntityId getCarer1Id() {
        return getForeignEntityId(carer1);
    }

    default Person getCarer1() {
        return getForeignEntity(carer1);
    }

    // Carer2
    default void setCarer2(Object value) {
        setForeignField(carer2, value);
    }

    default EntityId getCarer2Id() {
        return getForeignEntityId(carer2);
    }

    default Person getCarer2() {
        return getForeignEntity(carer2);
    }

    // Full name
    default void setName(String value) {
        setFieldValue(name, value);
    }

    default String getName() {
        return getStringFieldValue(name);
    }

    // ABC names (for search)
    default void setAbcNames(String value) {
        setFieldValue(abcNames, value);
    }

    default String getAbcNames() {
        return getStringFieldValue(abcNames);
    }

    // Country geoname ID
    default void setCountryGeonameid(Integer value) {
        setFieldValue(countryGeonameid, value);
    }

    default Integer getCountryGeonameid() {
        return getIntegerFieldValue(countryGeonameid);
    }

    // Country code
    default void setCountryCode(String value) {
        setFieldValue(countryCode, value);
    }

    default String getCountryCode() {
        return getStringFieldValue(countryCode);
    }

    // City geoname ID
    default void setCityGeonameid(Integer value) {
        setFieldValue(cityGeonameid, value);
    }

    default Integer getCityGeonameid() {
        return getIntegerFieldValue(cityGeonameid);
    }

    // City latitude
    default void setCityLatitude(Double value) {
        setFieldValue(cityLatitude, value);
    }

    default Double getCityLatitude() {
        return getDoubleFieldValue(cityLatitude);
    }

    // City longitude
    default void setCityLongitude(Double value) {
        setFieldValue(cityLongitude, value);
    }

    default Double getCityLongitude() {
        return getDoubleFieldValue(cityLongitude);
    }

    // City timezone
    default void setCityTimezone(String value) {
        setFieldValue(cityTimezone, value);
    }

    default String getCityTimezone() {
        return getStringFieldValue(cityTimezone);
    }

    // Latitude
    default void setLatitude(Double value) {
        setFieldValue(latitude, value);
    }

    default Double getLatitude() {
        return getDoubleFieldValue(latitude);
    }

    // Longitude
    default void setLongitude(Double value) {
        setFieldValue(longitude, value);
    }

    default Double getLongitude() {
        return getDoubleFieldValue(longitude);
    }

    // Removed flag
    default void setRemoved(Boolean value) {
        setFieldValue(removed, value);
    }

    default Boolean isRemoved() {
        return getBooleanFieldValue(removed);
    }

    // Never booked flag
    default void setNeverBooked(Boolean value) {
        setFieldValue(neverBooked, value);
    }

    default Boolean isNeverBooked() {
        return getBooleanFieldValue(neverBooked);
    }

    // Organization name
    default void setOrganizationName(String value) {
        setFieldValue(organizationName, value);
    }

    default String getOrganizationName() {
        return getStringFieldValue(organizationName);
    }

    // Nationality
    default void setNationality(String value) {
        setFieldValue(nationality, value);
    }

    default String getNationality() {
        return getStringFieldValue(nationality);
    }

    // Passport
    default void setPassport(String value) {
        setFieldValue(passport, value);
    }

    default String getPassport() {
        return getStringFieldValue(passport);
    }

    // Emailing list flag
    default void setEmailingList(Boolean value) {
        setFieldValue(emailingList, value);
    }

    default Boolean isEmailingList() {
        return getBooleanFieldValue(emailingList);
    }

    // Owner flag
    default void setOwner(Boolean value) {
        setFieldValue(owner, value);
    }

    default Boolean isOwner() {
        return getBooleanFieldValue(owner);
    }
}