package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasIcon;
import one.modality.base.shared.entities.markers.EntityHasOrganization;

/**
 * @author Bruno Salmon
 */
public interface Label extends
    EntityHasIcon,
    EntityHasOrganization {
    String de = "de";
    String en = "en";
    String es = "es";
    String fr = "fr";
    String pt = "pt";

    default void setDe(String value) {
        setFieldValue(de, value);
    }

    default String getDe() {
        return getStringFieldValue(de);
    }

    default void setEn(String value) {
        setFieldValue(en, value);
    }

    default String getEn() {
        return getStringFieldValue(en);
    }

    default void setEs(String value) {
        setFieldValue(es, value);
    }

    default String getEs() {
        return getStringFieldValue(es);
    }

    default void setFr(String value) {
        setFieldValue(fr, value);
    }

    default String getFr() {
        return getStringFieldValue(fr);
    }

    default void setPt(String value) {
        setFieldValue(pt, value);
    }

    default String getPt() {
        return getStringFieldValue(pt);
    }
}