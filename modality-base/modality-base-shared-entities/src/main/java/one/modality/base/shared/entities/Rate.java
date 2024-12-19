package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasArrivalSiteAndItem;
import one.modality.base.shared.entities.markers.EntityHasIcon;
import one.modality.base.shared.entities.markers.EntityHasSiteAndItem;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public interface Rate extends
    EntityHasIcon,
    EntityHasSiteAndItem,
    EntityHasArrivalSiteAndItem {

    String startDate = "startDate";
    String endDate = "endDate";
    String minDay = "minDay";
    String maxDay = "maxDay";
    String perDay = "perDay";
    String perPerson = "perPerson";
    String price = "price";
    String age1_max = "age1_max";
    String age1_price = "age1_price";
    String age1_discount = "age1_discount";
    String age2_max = "age2_max";
    String age2_price = "age2_price";
    String age2_discount = "age2_discount";
    String age3_max = "age3_max";
    String age3_price = "age3_price";
    String age3_discount = "age3_discount";
    String workingVisit_price = "workingVisit_price";
    String workingVisit_discount = "workingVisit_discount";
    String guest_price = "guest_price";
    String guest_discount = "guest_discount";
    String resident_price = "resident_price";
    String resident_discount = "resident_discount";
    String resident2_price = "resident2_price";
    String resident2_discount = "resident2_discount";
    String discovery_price = "discovery_price";
    String discovery_discount = "discovery_discount";
    String discoveryReduced_price = "discoveryReduced_price";
    String discoveryReduced_discount = "discoveryReduced_discount";
    String unemployed_price = "unemployed_price";
    String unemployed_discount = "unemployed_discount";
    String facilityFee_price = "facilityFee_price";
    String facilityFee_discount = "facilityFee_discount";

    default void setStartDate(LocalDate value) {
        setFieldValue(startDate, value);
    }

    default LocalDate getStartDate() {
        return getLocalDateFieldValue(startDate);
    }

    default void setEndDate(LocalDate value) {
        setFieldValue(endDate, value);
    }

    default LocalDate getEndDate() {
        return getLocalDateFieldValue(endDate);
    }

    default void setMinDay(Integer value) {
        setFieldValue(minDay, value);
    }

    default Integer getMinDay() {
        return getIntegerFieldValue(minDay);
    }

    default void setMaxDay(Integer value) {
        setFieldValue(maxDay, value);
    }

    default Integer getMaxDay() {
        return getIntegerFieldValue(maxDay);
    }

    default void setPerDay(Boolean value) {
        setFieldValue(perDay, value);
    }

    default Boolean isPerDay() {
        return getBooleanFieldValue(perDay);
    }

    default void setPerPerson(Boolean value) {
        setFieldValue(perPerson, value);
    }

    default Boolean isPerPerson() {
        return getBooleanFieldValue(perPerson);
    }

    default void setPrice(Object value) {
        setFieldValue(price, value);
    }

    default Integer getPrice() {
        return getIntegerFieldValue(price);
    }

    default void setAge1Max(Integer value) {
        setFieldValue(age1_max, value);
    }

    default Integer getAge1Max() {
        return getIntegerFieldValue(age1_max);
    }

    default void setAge1Price(Integer value) {
        setFieldValue(age1_price, value);
    }

    default Integer getAge1Price() {
        return getIntegerFieldValue(age1_price);
    }

    default void setAge1Discount(Integer value) {
        setFieldValue(age1_discount, value);
    }

    default Integer getAge1Discount() {
        return getIntegerFieldValue(age1_discount);
    }

    default void setAge2Max(Integer value) {
        setFieldValue(age2_max, value);
    }

    default Integer getAge2Max() {
        return getIntegerFieldValue(age2_max);
    }

    default void setAge2Price(Integer value) {
        setFieldValue(age2_price, value);
    }

    default Integer getAge2Price() {
        return getIntegerFieldValue(age2_price);
    }

    default void setAge2Discount(Integer value) {
        setFieldValue(age2_discount, value);
    }

    default Integer getAge2Discount() {
        return getIntegerFieldValue(age2_discount);
    }

    default void setAge3Max(Integer value) {
        setFieldValue(age3_max, value);
    }

    default Integer getAge3Max() {
        return getIntegerFieldValue(age3_max);
    }

    default void setAge3Price(Integer value) {
        setFieldValue(age3_price, value);
    }

    default Integer getAge3Price() {
        return getIntegerFieldValue(age3_price);
    }

    default void setAge3Discount(Integer value) {
        setFieldValue(age3_discount, value);
    }

    default Integer getAge3Discount() {
        return getIntegerFieldValue(age3_discount);
    }

    default void setWorkingVisitPrice(Integer value) {
        setFieldValue(workingVisit_price, value);
    }

    default Integer getWorkingVisitPrice() {
        return getIntegerFieldValue(workingVisit_price);
    }

    default void setWorkingVisitDiscount(Integer value) {
        setFieldValue(workingVisit_discount, value);
    }

    default Integer getWorkingVisitDiscount() {
        return getIntegerFieldValue(workingVisit_discount);
    }

    default void setGuestPrice(Integer value) {
        setFieldValue(guest_price, value);
    }

    default Integer getGuestPrice() {
        return getIntegerFieldValue(guest_price);
    }

    default void setGuestDiscount(Integer value) {
        setFieldValue(guest_discount, value);
    }

    default Integer getGuestDiscount() {
        return getIntegerFieldValue(guest_discount);
    }

    default void setResidentPrice(Integer value) {
        setFieldValue(resident_price, value);
    }

    default Integer getResidentPrice() {
        return getIntegerFieldValue(resident_price);
    }

    default void setResidentDiscount(Integer value) {
        setFieldValue(resident_discount, value);
    }

    default Integer getResidentDiscount() {
        return getIntegerFieldValue(resident_discount);
    }

    default void setResident2Price(Integer value) {
        setFieldValue(resident2_price, value);
    }

    default Integer getResident2Price() {
        return getIntegerFieldValue(resident2_price);
    }

    default void setResident2Discount(Integer value) {
        setFieldValue(resident2_discount, value);
    }

    default Integer getResident2Discount() {
        return getIntegerFieldValue(resident2_discount);
    }

    default void setDiscoveryPrice(Integer value) {
        setFieldValue(discovery_price, value);
    }

    default Integer getDiscoveryPrice() {
        return getIntegerFieldValue(discovery_price);
    }

    default void setDiscoveryDiscount(Integer value) {
        setFieldValue(discovery_discount, value);
    }

    default Integer getDiscoveryDiscount() {
        return getIntegerFieldValue(discovery_discount);
    }

    default void setDiscoveryReducedPrice(Integer value) {
        setFieldValue(discoveryReduced_price, value);
    }

    default Integer getDiscoveryReducedPrice() {
        return getIntegerFieldValue(discoveryReduced_price);
    }

    default void setDiscoveryReducedDiscount(Integer value) {
        setFieldValue(discoveryReduced_discount, value);
    }

    default Integer getDiscoveryReducedDiscount() {
        return getIntegerFieldValue(discoveryReduced_discount);
    }

    default void setUnemployedPrice(Integer value) {
        setFieldValue(unemployed_price, value);
    }

    default Integer getUnemployedPrice() {
        return getIntegerFieldValue(unemployed_price);
    }

    default void setUnemployedDiscount(Integer value) {
        setFieldValue(unemployed_discount, value);
    }

    default Integer getUnemployedDiscount() {
        return getIntegerFieldValue(unemployed_discount);
    }

    default void setFacilityFeePrice(Integer value) {
        setFieldValue(facilityFee_price, value);
    }

    default Integer getFacilityFeePrice() {
        return getIntegerFieldValue(facilityFee_price);
    }

    default void setFacilityFeeDiscount(Integer value) {
        setFieldValue(facilityFee_discount, value);
    }

    default Integer getFacilityFeeDiscount() {
        return getIntegerFieldValue(facilityFee_discount);
    }
}