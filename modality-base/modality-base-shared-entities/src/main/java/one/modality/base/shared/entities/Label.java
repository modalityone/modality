package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasIcon;
import one.modality.base.shared.entities.markers.EntityHasOrganization;

/**
 * @author Bruno Salmon
 */
public interface Label extends
    EntityHasIcon,
    EntityHasOrganization {
    String de = "de"; // German
    String en = "en"; // English
    String es = "es"; // Spanish
    String fr = "fr"; // French
    String pt = "pt"; // Portuguese
    String zh = "zh"; // Simplified Chinese (Mandarin)
    String yue = "yue"; // Cantonese
    String el = "el"; // Greek
    String vi = "vi"; // Vietnamese

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

    default void setZh(String value) {
        setFieldValue(zh, value);
    }

    default String getZh() {
        return getStringFieldValue(zh);
    }

    default void setYue(String value) {
        setFieldValue(yue, value);
    }

    default String getYue() {
        return getStringFieldValue(yue);
    }

    default void setEl(String value) {
        setFieldValue(el, value);
    }

    default String getEl() {
        return getStringFieldValue(el);
    }

    default void setVi(String value) {
        setFieldValue(vi, value);
    }

    default String getVi() {
        return getStringFieldValue(vi);
    }

}