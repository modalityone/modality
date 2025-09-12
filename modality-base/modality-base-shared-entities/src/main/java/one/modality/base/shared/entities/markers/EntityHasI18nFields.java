package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface EntityHasI18nFields extends Entity, HasI18nFields {

    String de = "de"; // German
    String en = "en"; // English
    String es = "es"; // Spanish
    String fr = "fr"; // French
    String pt = "pt"; // Portuguese
    String zh = "zh"; // Simplified Chinese (Mandarin)
    String yue = "yue"; // Cantonese
    String it = "it"; // Greek
    String el = "el"; // Greek
    String vi = "vi"; // Vietnamese

    @Override
    default void setDe(String value) {
        setFieldValue(de, value);
    }

    @Override
    default String getDe() {
        return getStringFieldValue(de);
    }

    @Override
    default void setEn(String value) {
        setFieldValue(en, value);
    }

    @Override
    default String getEn() {
        return getStringFieldValue(en);
    }

    @Override
    default void setEs(String value) {
        setFieldValue(es, value);
    }

    @Override
    default String getEs() {
        return getStringFieldValue(es);
    }

    @Override
    default void setFr(String value) {
        setFieldValue(fr, value);
    }

    @Override
    default String getFr() {
        return getStringFieldValue(fr);
    }

    @Override
    default void setPt(String value) {
        setFieldValue(pt, value);
    }

    @Override
    default String getPt() {
        return getStringFieldValue(pt);
    }

    @Override
    default void setZh(String value) {
        setFieldValue(zh, value);
    }

    @Override
    default String getZh() {
        return getStringFieldValue(zh);
    }

    @Override
    default void setYue(String value) {
        setFieldValue(yue, value);
    }

    @Override
    default String getYue() {
        return getStringFieldValue(yue);
    }

    @Override
    default void setEl(String value) {
        setFieldValue(el, value);
    }

    @Override
    default String getEl() {
        return getStringFieldValue(el);
    }

    @Override
    default void setVi(String value) {
        setFieldValue(vi, value);
    }

    @Override
    default String getVi() {
        return getStringFieldValue(vi);
    }

    @Override
    default void setIt(String value) {
        setFieldValue(it, value);
    }

    @Override
    default String getIt() {
        return getStringFieldValue(it);
    }

}
