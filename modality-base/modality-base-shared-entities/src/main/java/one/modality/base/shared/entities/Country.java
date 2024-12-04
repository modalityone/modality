package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasIcon;
import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * @author Bruno Salmon
 */
public interface Country extends
    EntityHasName,
    EntityHasLabel,
    EntityHasIcon {
    String geonameid = "geonameid";
    String isoAlpha2 = "iso_alpha2";
    String fipsCode = "fipsCode";
    String latitude = "latitude";
    String longitude = "longitude";
    String north = "north";
    String south = "south";
    String west = "west";
    String east = "east";

    default void setGeonameid(Integer value) {
        setFieldValue(geonameid, value);
    }

    default Integer getGeonameid() {
        return getIntegerFieldValue(geonameid);
    }

    default void setIsoAlpha2(String value) {
        setFieldValue(isoAlpha2, value);
    }

    default String getIsoAlpha2() {
        return getStringFieldValue(isoAlpha2);
    }

    default void setFipsCode(String value) {
        setFieldValue(fipsCode, value);
    }

    default String getFipsCode() {
        return getStringFieldValue(fipsCode);
    }

    default Float getLatitude() {
        return getFloatFieldValue(latitude);
    }

    default void setLatitude(Float value) {
        setFieldValue(latitude, value);
    }

    default Float getLongitude() {
        return getFloatFieldValue(longitude);
    }

    default void setLongitude(Float value) {
        setFieldValue(longitude, value);
    }

    default Float getNorth() {
        return getFloatFieldValue(north);
    }

    default void setNorth(Float value) {
        setFieldValue(north, value);
    }

    default Float getSouth() {
        return getFloatFieldValue(south);
    }

    default void setSouth(Float value) {
        setFieldValue(south, value);
    }

    default Float getWest() {
        return getFloatFieldValue(west);
    }

    default void setWest(Float value) {
        setFieldValue(west, value);
    }

    default Float getEast() {
        return getFloatFieldValue(east);
    }

    default void setEast(Float value) {
        setFieldValue(east, value);
    }
}