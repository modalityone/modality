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
    EntityHasResourceConfiguration,
    EntityHasTimeline {
    String startDate = "startDate";
    String endDate = "endDate";
    String hasAttendanceGap = "hasAttendanceGap";
    String dates = "dates";
    String price_net = "price_net";
    String price_minDeposit = "price_minDeposit";
    String price_custom = "price_custom";
    String price_discount = "price_discount";
    String share_mate = "share_mate";
    String share_mate_charged = "share_mate_charged";
    String share_mate_ownerName = "share_mate_ownerName";
    String share_mate_ownerPerson = "share_mate_ownerPerson";
    String share_mate_ownerDocumentLine = "share_mate_ownerDocumentLine";
    String share_owner = "share_owner";
    String share_owner_mate1Name = "share_owner_mate1Name";
    String share_owner_mate2Name = "share_owner_mate2Name";
    String share_owner_mate3Name = "share_owner_mate3Name";
    String share_owner_mate4Name = "share_owner_mate4Name";
    String share_owner_mate5Name = "share_owner_mate5Name";
    String share_owner_mate6Name = "share_owner_mate6Name";
    String share_owner_mate7Name = "share_owner_mate7Name";
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

    default Boolean isShareMate() {
        return getBooleanFieldValue(share_mate);
    }

    default void setShareMate(Boolean value) {
        setFieldValue(share_mate, value);
    }

    default Boolean isShareMateCharged() {
        return getBooleanFieldValue(share_mate_charged);
    }

    default void setShareMateCharged(Boolean value) {
        setFieldValue(share_mate_charged, value);
    }

    default DocumentLine getShareMateOwnerDocumentLine() {
        return getForeignEntity(share_mate_ownerDocumentLine);
    }

    default EntityId getShareMateOwnerDocumentLineId() {
        return getForeignEntityId(share_mate_ownerDocumentLine);
    }

    default void setShareMateOwnerDocumentLine(Object value) {
        setForeignField(share_mate_ownerDocumentLine, value);
    }

    default String getShareMateOwnerName() {
        return getStringFieldValue(share_mate_ownerName);
    }

    default void setShareMateOwnerName(String value) {
        setFieldValue(share_mate_ownerName, value);
    }

    default Person getShareMateOwnerPerson() {
        return getForeignEntity(share_mate_ownerPerson);
    }

    default EntityId getShareMateOwnerPersonId() {
        return getForeignEntityId(share_mate_ownerPerson);
    }

    default void setShareMateOwnerPerson(Object value) {
        setForeignField(share_mate_ownerPerson, value);
    }

    default Boolean isShareOwner() {
        return getBooleanFieldValue(share_owner);
    }

    default void setShareOwner(Boolean value) {
        setFieldValue(share_owner, value);
    }

    default String getShareOwnerMate1Name() {
        return getStringFieldValue(share_owner_mate1Name);
    }

    default void setShareOwnerMate1Name(String value) {
        setFieldValue(share_owner_mate1Name, value);
    }

    default String getShareOwnerMate2Name() {
        return getStringFieldValue(share_owner_mate2Name);
    }

    default void setShareOwnerMate2Name(String value) {
        setFieldValue(share_owner_mate2Name, value);
    }

    default String getShareOwnerMate3Name() {
        return getStringFieldValue(share_owner_mate3Name);
    }

    default void setShareOwnerMate3Name(String value) {
        setFieldValue(share_owner_mate3Name, value);
    }

    default String getShareOwnerMate4Name() {
        return getStringFieldValue(share_owner_mate4Name);
    }

    default void setShareOwnerMate4Name(String value) {
        setFieldValue(share_owner_mate4Name, value);
    }

    default String getShareOwnerMate5Name() {
        return getStringFieldValue(share_owner_mate5Name);
    }

    default void setShareOwnerMate5Name(String value) {
        setFieldValue(share_owner_mate5Name, value);
    }

    default String getShareOwnerMate6Name() {
        return getStringFieldValue(share_owner_mate6Name);
    }

    default void setShareOwnerMate6Name(String value) {
        setFieldValue(share_owner_mate6Name, value);
    }

    default String getShareOwnerMate7Name() {
        return getStringFieldValue(share_owner_mate7Name);
    }

    default void setShareOwnerMate7Name(String value) {
        setFieldValue(share_owner_mate7Name, value);
    }

    String[] getShareOwnerMatesNames(); // implemented in DocumentLineImpl

    void setShareOwnerMatesNames(String[] matesNames); // implemented in DocumentLineImpl


    // Non-persistent bedNumber field using by Household screen (allocated arbitrary at runtime)
    default Integer getBedNumber() {
        return getIntegerFieldValue(bedNumber);
    }

    default void setBedNumber(Integer value) {
        setFieldValue(bedNumber, value);
    }

}