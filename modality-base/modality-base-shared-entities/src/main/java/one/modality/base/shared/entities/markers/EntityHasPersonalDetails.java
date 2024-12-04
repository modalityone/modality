package one.modality.base.shared.entities.markers;

import one.modality.base.shared.entities.Country;
import one.modality.base.shared.entities.Organization;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * Author: Bruno Salmon
 */
public interface EntityHasPersonalDetails extends EntityHasOrganization, EntityHasCountry, HasPersonalDetails {

    // Static constants for string literals
    String firstName = "firstName";
    String lastName = "lastName";
    String layName = "layName";
    String male = "male";
    String ordained = "ordained";
    String age = "age";
    String carer1Name = "carer1Name";
    String carer2Name = "carer2Name";
    String email = "email";
    String phone = "phone";
    String street = "street";
    String postCode = "postCode";
    String cityName = "cityName";
    String admin1Name = "admin1Name";
    String admin2Name = "admin2Name";
    String countryName = "countryName";
    String country = "country";
    String organization = "organization";
    String unemployed = "unemployed";
    String facilityFee = "facilityFee";
    String workingVisit = "workingVisit";
    String discovery = "discovery";
    String discoveryReduced = "discoveryReduced";
    String guest = "guest";
    String resident = "resident";
    String resident2 = "resident2";

    // Refactored methods
    default Object getFirstNameField() { return firstName; }

    default void setFirstName(String value) {
        setFieldValue(getFirstNameField(), value);
    }

    default String getFirstName() {
        return getStringFieldValue(getFirstNameField());
    }

    default Object getLastNameField() { return lastName; }

    default void setLastName(String value) {
        setFieldValue(getLastNameField(), value);
    }

    default String getLastName() {
        return getStringFieldValue(getLastNameField());
    }

    default Object getLayNameField() { return layName; }

    default void setLayName(String value) {
        setFieldValue(getLayNameField(), value);
    }

    default String getLayName() {
        return getStringFieldValue(getLayNameField());
    }

    default Object getMaleField() { return male; }

    default void setMale(Boolean value) {
        setFieldValue(getMaleField(), value);
    }

    default Boolean isMale() {
        return getBooleanFieldValue(getMaleField());
    }

    default Object getOrdainedField() { return ordained; }

    default void setOrdained(Boolean value) {
        setFieldValue(getOrdainedField(), value);
    }

    default Boolean isOrdained() {
        return getBooleanFieldValue(getOrdainedField());
    }

    default Object getAgeField() { return age; }

    default void setAge(Integer value) {
        setFieldValue(getAgeField(), value);
    }

    default Integer getAge() {
        return getIntegerFieldValue(getAgeField());
    }

    default Object getCarer1NameField() { return carer1Name; }

    default void setCarer1Name(String value) {
        setFieldValue(getCarer1NameField(), value);
    }

    default String getCarer1Name() {
        return getStringFieldValue(getCarer1NameField());
    }

    default Object getCarer2NameField() { return carer2Name; }

    default void setCarer2Name(String value) {
        setFieldValue(getCarer2NameField(), value);
    }

    default String getCarer2Name() {
        return getStringFieldValue(getCarer2NameField());
    }

    default Object getEmailField() { return email; }

    default void setEmail(String value) {
        setFieldValue(getEmailField(), value);
    }

    default String getEmail() {
        return getStringFieldValue(getEmailField());
    }

    default Object getPhoneField() { return phone; }

    default void setPhone(String value) {
        setFieldValue(getPhoneField(), value);
    }

    default String getPhone() {
        return getStringFieldValue(getPhoneField());
    }

    default Object getStreetField() { return street; }

    default void setStreet(String value) {
        setFieldValue(getStreetField(), value);
    }

    default String getStreet() {
        return getStringFieldValue(getStreetField());
    }

    default Object getPostCodeField() { return postCode; }

    default void setPostCode(String value) {
        setFieldValue(getPostCodeField(), value);
    }

    default String getPostCode() {
        return getStringFieldValue(getPostCodeField());
    }

    default Object getCityNameField() { return cityName; }

    default void setCityName(String value) {
        setFieldValue(getCityNameField(), value);
    }

    default String getCityName() {
        return getStringFieldValue(getCityNameField());
    }

    default Object getAdmin1NameField() { return admin1Name; }

    default void setAdmin1Name(String value) {
        setFieldValue(getAdmin1NameField(), value);
    }

    default String getAdmin1Name() {
        return getStringFieldValue(getAdmin1NameField());
    }

    default Object getAdmin2NameField() { return admin2Name; }

    default void setAdmin2Name(String value) {
        setFieldValue(getAdmin2NameField(), value);
    }

    default String getAdmin2Name() {
        return getStringFieldValue(getAdmin2NameField());
    }

    default Object getCountryNameField() { return countryName; }

    default void setCountryName(String value) {
        setFieldValue(getCountryNameField(), value);
    }

    default String getCountryName() {
        return getStringFieldValue(getCountryNameField());
    }

    default Object getCountryField() { return country; }

    default void setCountry(Object value) {
        setForeignField(getCountryField(), value);
    }

    default Country getCountry() {
        return getForeignEntity(getCountryField());
    }

    default EntityId getCountryId() {
        return getForeignEntityId(getCountryField());
    }

    default Object getOrganizationField() { return organization; }

    default void setOrganization(Object value) {
        setForeignField(getOrganizationField(), value);
    }

    default Organization getOrganization() {
        return getForeignEntity(getOrganizationField());
    }

    default EntityId getOrganizationId() {
        return getForeignEntityId(getOrganizationField());
    }

    default Object getUnemployedField() { return unemployed; }

    default void setUnemployed(Boolean value) {
        setFieldValue(getUnemployedField(), value);
    }

    default Boolean isUnemployed() {
        return getBooleanFieldValue(getUnemployedField());
    }

    default Object getFacilityFeeField() { return facilityFee; }

    default void setFacilityFee(Boolean value) {
        setFieldValue(getFacilityFeeField(), value);
    }

    default Boolean isFacilityFee() {
        return getBooleanFieldValue(getFacilityFeeField());
    }

    default Object getWorkingVisitField() { return workingVisit; }

    default void setWorkingVisit(Boolean value) {
        setFieldValue(getWorkingVisitField(), value);
    }

    default Boolean isWorkingVisit() {
        return getBooleanFieldValue(getWorkingVisitField());
    }

    default Object getDiscoveryField() { return discovery; }

    default void setDiscovery(Boolean value) {
        setFieldValue(getDiscoveryField(), value);
    }

    default Boolean isDiscovery() {
        return getBooleanFieldValue(getDiscoveryField());
    }

    default Object getDiscoveryReducedField() { return discoveryReduced; }

    default void setDiscoveryReduced(Boolean value) {
        setFieldValue(getDiscoveryReducedField(), value);
    }

    default Boolean isDiscoveryReduced() {
        return getBooleanFieldValue(getDiscoveryReducedField());
    }

    default Object getGuestField() { return guest; }

    default void setGuest(Boolean value) {
        setFieldValue(getGuestField(), value);
    }

    default Boolean isGuest() {
        return getBooleanFieldValue(getGuestField());
    }

    default Object getResidentField() { return resident; }

    default void setResident(Boolean value) {
        setFieldValue(getResidentField(), value);
    }

    default Boolean isResident() {
        return getBooleanFieldValue(getResidentField());
    }

    default Object getResident2Field() { return resident2; }

    default void setResident2(Boolean value) {
        setFieldValue(getResident2Field(), value);
    }

    default Boolean isResident2() {
        return getBooleanFieldValue(getResident2Field());
    }
}
