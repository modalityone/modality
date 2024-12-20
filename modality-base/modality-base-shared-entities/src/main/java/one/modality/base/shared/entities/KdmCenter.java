package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * @author Bruno Salmon
 */
public interface KdmCenter extends
    EntityHasName {
    String kdmId = "kdmId";
    String lat = "lat";
    String lng = "lng";
    String type = "type";
    String mothercenter = "mothercenter";
    String address = "address";
    String address2 = "address2";
    String address3 = "address3";
    String city = "city";
    String state = "state";
    String postal = "postal";
    String email = "email";
    String phone = "phone";
    String photo = "photo";
    String web = "web";

    default Integer getKdmId() {
        return getIntegerFieldValue(kdmId);
    }

    default void setKdmId(Integer value) {
        setFieldValue(kdmId, value);
    }

    default Float getLat() {
        return getFloatFieldValue(lat);
    }

    default void setLat(Float value) {
        setFieldValue(lat, value);
    }

    default Float getLng() {
        return getFloatFieldValue(lng);
    }

    default void setLng(Float value) {
        setFieldValue(lng, value);
    }

    default String getType() {
        return getStringFieldValue(type);
    }

    default void setType(String value) {
        setFieldValue(type, value);
    }

    default Boolean isMothercenter() {
        return getBooleanFieldValue(mothercenter);
    }

    default void setMothercenter(Boolean value) {
        setFieldValue(mothercenter, value);
    }

    default String getAddress() {
        return getStringFieldValue(address);
    }

    default void setAddress(String value) {
        setFieldValue(address, value);
    }

    default String getAddress2() {
        return getStringFieldValue(address2);
    }

    default void setAddress2(String value) {
        setFieldValue(address2, value);
    }

    default String getAddress3() {
        return getStringFieldValue(address3);
    }

    default void setAddress3(String value) {
        setFieldValue(address3, value);
    }

    default String getCity() {
        return getStringFieldValue(city);
    }

    default void setCity(String value) {
        setFieldValue(city, value);
    }

    default String getState() {
        return getStringFieldValue(state);
    }

    default void setState(String value) {
        setFieldValue(state, value);
    }

    default String getPostal() {
        return getStringFieldValue(postal);
    }

    default void setPostal(String value) {
        setFieldValue(postal, value);
    }

    default String getEmail() {
        return getStringFieldValue(email);
    }

    default void setEmail(String value) {
        setFieldValue(email, value);
    }

    default String getPhone() {
        return getStringFieldValue(phone);
    }

    default void setPhone(String value) {
        setFieldValue(phone, value);
    }

    default String getPhoto() {
        return getStringFieldValue(photo);
    }

    default void setPhoto(String value) {
        setFieldValue(photo, value);
    }

    default String getWeb() {
        return getStringFieldValue(web);
    }

    default void setWeb(String value) {
        setFieldValue(web, value);
    }
}