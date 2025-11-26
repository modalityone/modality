package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.*;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public interface DocumentLine extends
    EntityHasDocument,
    EntityHasCancelled,
    EntityHasRead,
    EntityHasArrivalSiteAndItem,
    EntityHasResourceConfiguration {
    String startDate = "startDate";
    String endDate = "endDate";
    String hasAttendanceGap = "hasAttendanceGap";
    String dates = "dates";
    String price_net = "price_net";
    String price_minDeposit = "price_minDeposit";
    String price_custom = "price_custom";
    String price_discount = "price_discount";
    String timeline = "timeline";
    String cleaned = "cleaned";
    String bedNumber = "bedNumber";

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

    default Boolean hasAttendanceGap() {
        return getBooleanFieldValue(hasAttendanceGap);
    }

    default void setHasAttendanceGap(Boolean value) {
        setFieldValue(hasAttendanceGap, value);
    }

    default String getDates() {
        return getStringFieldValue(dates);
    }

    default Integer getPriceNet() {
        return getIntegerFieldValue(price_net);
    }

    default void setPriceNet(Integer value) {
        setFieldValue(price_net, value);
    }

    default Integer getPriceMinDeposit() {
        return getIntegerFieldValue(price_minDeposit);
    }

    default void setPriceMinDeposit(Integer value) {
        setFieldValue(price_minDeposit, value);
    }

    default Integer getPriceCustom() {
        return getIntegerFieldValue(price_custom);
    }

    default void setPriceCustom(Integer value) {
        setFieldValue(price_custom, value);
    }

    default Integer getPriceDiscount() {
        return getIntegerFieldValue(price_discount);
    }

    default void setPriceDiscount(Integer value) {
        setFieldValue(price_discount, value);
    }

    default Boolean isCleaned() {
        return getBooleanFieldValue(cleaned);
    }

    default void setCleaned(Boolean value) {
        setFieldValue(cleaned, value);
    }

    // Non-persistent bedNumber field using by Household screen (allocated arbitrary at runtime)
    default Integer getBedNumber() {
        return getIntegerFieldValue(bedNumber);
    }

    default void setBedNumber(Integer value) {
        setFieldValue(bedNumber, value);
    }

    default void setTimeLine(Object value) {
        setForeignField(timeline, value);
    }

    default EntityId getTimelineId() {
        return getForeignEntityId(timeline);
    }

    default Timeline getTimeline() {
        return getForeignEntity(timeline);
    }
}