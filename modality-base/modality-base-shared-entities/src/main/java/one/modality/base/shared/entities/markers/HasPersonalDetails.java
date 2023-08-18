package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Organization;

/**
 * @author Bruno Salmon
 */
public interface HasPersonalDetails extends HasCountry {

    void setFirstName(String firstName);

    String getFirstName();

    void setLastName(String lastName);

    String getLastName();

    default String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    void setLayName(String layName);

    String getLayName();

    void setMale(Boolean male);

    Boolean isMale();

    void setOrdained(Boolean ordained);

    Boolean isOrdained();

    void setAge(Integer age);

    Integer getAge();

    void setCarer1Name(String carer1Name);

    String getCarer1Name();

    void setCarer2Name(String carer2Name);

    String getCarer2Name();

    void setEmail(String email);

    String getEmail();

    void setPhone(String phone);

    String getPhone();

    void setStreet(String street);

    String getStreet();

    void setPostCode(String postCode);

    String getPostCode();

    void setCityName(String cityName);

    String getCityName();

    void setAdmin1Name(String admin1Name);

    String getAdmin1Name();

    void setAdmin2Name(String admin2Name);

    String getAdmin2Name();

    void setCountryName(String countryName);

    String getCountryName();

    void setOrganization(Object organization);

    Organization getOrganization();

    EntityId getOrganizationId();

    void setUnemployed(Boolean unemployed);

    Boolean isUnemployed();

    void setFacilityFee(Boolean facilityFee);

    Boolean isFacilityFee();

    void setWorkingVisit(Boolean workingVisit);

    Boolean isWorkingVisit();

    void setDiscovery(Boolean discovery);

    Boolean isDiscovery();

    void setDiscoveryReduced(Boolean discoveryReduced);

    Boolean isDiscoveryReduced();

    void setGuest(Boolean guest);

    Boolean isGuest();

    void setResident(Boolean resident);

    Boolean isResident();

    void setResident2(Boolean resident2);

    Boolean isResident2();

}
