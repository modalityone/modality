package one.modality.base.shared.entities.markers;

/**
 * Author: Bruno Salmon
 */
public interface EntityHasPersonalDetailsCopy extends EntityHasPersonalDetails {

    // Static constants for string literals
    String person_firstName = "person_firstName";
    String person_lastName = "person_lastName";
    String person_layName = "person_layName";
    String person_male = "person_male";
    String person_ordained = "person_ordained";
    String person_age = "person_age";
    String person_carer1Name = "person_carer1Name";
    String person_carer2Name = "person_carer2Name";
    String person_email = "person_email";
    String person_phone = "person_phone";
    String person_street = "person_street";
    String person_postCode = "person_postCode";
    String person_cityName = "person_cityName";
    String person_admin1Name = "person_admin1Name";
    String person_admin2Name = "person_admin2Name";
    String person_countryName = "person_countryName";
    String person_country = "person_country";
    String person_organization = "person_organization";
    String person_unemployed = "person_unemployed";
    String person_facilityFee = "person_facilityFee";
    String person_workingVisit = "person_workingVisit";
    String person_discovery = "person_discovery";
    String person_discoveryReduced = "person_discoveryReduced";
    String person_guest = "person_guest";
    String person_resident = "person_resident";
    String person_resident2 = "person_resident2";

    // Refactored methods
    default Object getFirstNameField() { return person_firstName; }

    default Object getLastNameField() { return person_lastName; }

    default Object getLayNameField() { return person_layName; }

    @Override
    default Object getMaleField() {
        return person_male;
    }

    @Override
    default Object getOrdainedField() {
        return person_ordained;
    }

    default Object getAgeField() { return person_age; }

    @Override
    default Object getCarer1NameField() {
        return person_carer1Name;
    }

    @Override
    default Object getCarer2NameField() {
        return person_carer2Name;
    }

    @Override
    default Object getEmailField() {
        return person_email;
    }

    @Override
    default Object getPhoneField() {
        return person_phone;
    }

    @Override
    default Object getStreetField() {
        return person_street;
    }

    @Override
    default Object getPostCodeField() {
        return person_postCode;
    }

    @Override
    default Object getCityNameField() {
        return person_cityName;
    }

    @Override
    default Object getAdmin1NameField() {
        return person_admin1Name;
    }

    @Override
    default Object getAdmin2NameField() {
        return person_admin2Name;
    }

    @Override
    default Object getCountryNameField() {
        return person_countryName;
    }

    @Override
    default Object getCountryField() {
        return person_country;
    }

    @Override
    default Object getOrganizationField() {
        return person_organization;
    }

    default Object getUnemployedField() { return person_unemployed; }

    default Object getFacilityFeeField() { return person_facilityFee; }

    default Object getWorkingVisitField() { return person_workingVisit; }

    default Object getDiscoveryField() { return person_discovery; }

    default Object getDiscoveryReducedField() { return person_discoveryReduced; }

    default Object getGuestField() { return person_guest; }

    default Object getResidentField() { return person_resident; }

    default Object getResident2Field() { return person_resident2; }

}
