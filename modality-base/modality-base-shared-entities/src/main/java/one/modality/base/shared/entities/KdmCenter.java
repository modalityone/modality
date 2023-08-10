package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * @author Bruno Salmon
 */
public interface KdmCenter extends
        EntityHasName {

    default Integer getKdmId() {
        return getIntegerFieldValue("kdmId");
    }

    default void setKdmId(Integer kdmId) {
        setFieldValue("kdmId", kdmId);
    }

    default Float getLat() {
        return getFloatFieldValue("lat");
    }

    default void setLat(Float lat) {
        setFieldValue("lat", lat);
    }

    default Float getLng() {
        return getFloatFieldValue("lng");
    }

    default void setLng(Float lng) {
        setFieldValue("lng", lng);
    }

    default String getType() {
        return getStringFieldValue("type");
    }

    default void setType(String type) {
        setFieldValue("type", type);
    }

    default Boolean isMothercenter() {
        return getBooleanFieldValue("mothercenter");
    }

    default void setMothercenter(Boolean mothercenter) {
        setFieldValue("mothercenter", mothercenter);
    }

    default String getAddress() {
        return getStringFieldValue("address");
    }

    default void setAddress(String address) {
        setFieldValue("address", address);
    }

    default String getAddress2() {
        return getStringFieldValue("address2");
    }

    default void setAddress2(String address2) {
        setFieldValue("address2", address2);
    }

    default String getAddress3() {
        return getStringFieldValue("address3");
    }

    default void setAddress3(String address3) {
        setFieldValue("address3", address3);
    }

    default String getCity() {
        return getStringFieldValue("city");
    }

    default void setCity(String city) {
        setFieldValue("city", city);
    }

    default String getState() {
        return getStringFieldValue("state");
    }

    default void setState(String state) {
        setFieldValue("state", state);
    }

    default String getPostal() {
        return getStringFieldValue("postal");
    }

    default void setPostal(String postal) {
        setFieldValue("postal", postal);
    }

    default String getEmail() {
        return getStringFieldValue("email");
    }

    default void setEmail(String email) {
        setFieldValue("email", email);
    }

    default String getPhone() {
        return getStringFieldValue("phone");
    }

    default void setPhone(String phone) {
        setFieldValue("phone", phone);
    }

    default String getPhoto() {
        return getStringFieldValue("photo");
    }

    default void setPhoto(String photo) {
        setFieldValue("photo", photo);
    }

    default String getWeb() {
        return getStringFieldValue("web");
    }

    default void setWeb(String web) {
        setFieldValue("web", web);
    }

}
