package one.modality.base.shared.entities;

import dev.webfx.platform.util.Strings;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Bruno Salmon
 */
public interface Event extends Entity,
        EntityHasName,
        EntityHasLabel,
        EntityHasIcon,
        EntityHasOrganization,
        EntityHasCorporation {


    default void setState(Object state) {
        setFieldValue("state", Strings.stringValue(state));
    }

    default EventState getState() {
        return EventState.of(getStringFieldValue("state"));
    }

    default void setAdvertised(Boolean advertised) {
        setFieldValue("advertised", advertised);
    }

    default Boolean isAdvertised() {
        return getBooleanFieldValue("advertised");
    }


    default void setType(Object type) {
        setForeignField("type", type);
    }

    default EntityId getTypeId() {
        return getForeignEntityId("type");
    }

    default EventType getType() {
        return getForeignEntity("type");
    }


    default void setStartDate(LocalDate startDate) {
        setFieldValue("startDate", startDate);
    }

    default LocalDate getStartDate() {
        return getLocalDateFieldValue("startDate");
    }

    default void setEndDate(LocalDate endDate) {
        setFieldValue("endDate", endDate);
    }

    default LocalDate getEndDate() {
        return getLocalDateFieldValue("endDate");
    }

    default void setOpeningDate(LocalDateTime openingDate) {
        setFieldValue("openingDate", openingDate);
    }

    default LocalDateTime getOpeningDate() {
        return getLocalDateTimeFieldValue("openingDate");
    }

    default void setVodExpirationDate(LocalDateTime vodExpirationDate) {
        setFieldValue("vodExpirationDate", vodExpirationDate);
    }

    default LocalDateTime getVodExpirationDate() {
        return getLocalDateTimeFieldValue("vodExpirationDate");
    }

    default void setAudioExpirationDate(LocalDateTime audioExpirationDate) {
        setFieldValue("audioExpirationDate", audioExpirationDate);
    }

    default LocalDateTime getAudioExpirationDate() {
        return getLocalDateTimeFieldValue("audioExpirationDate");
    }

    default void setLivestreamLink(String livestreamLink) {
        setFieldValue("livestreamLink", livestreamLink);
    }

    default String getLivestreamLink() {
        return getStringFieldValue("livestreamLink");
    }

    default void setLive(Boolean live) {
        setFieldValue("live", live);
    }

    default Boolean isLive() {
        return getBooleanFieldValue("live");
    }

    default void setFeesBottomLabel(Object label) {
        setForeignField("feesBottomLabel", label);
    }

    default EntityId getFeesBottomLabelId() {
        return getForeignEntityId("feesBottomLabel");
    }

    default Label getFeesBottomLabel() {
        return getForeignEntity("feesBottomLabel");
    }

    default void setKbs3(Boolean kbs3) {
        setFieldValue("kbs3", kbs3);
    }

    default Boolean isKbs3() {
        return getBooleanFieldValue("kbs3");
    }

    default void setDescription(String description) {
        setFieldValue("description", description);
    }

    default String getDescription() {
        return getStringFieldValue("description");
    }

    default void setShortDescription(String shortDescription) {
        setFieldValue("shortDescription", shortDescription);
    }

    default String getShortDescription() {
        return getStringFieldValue("shortDescription");
    }

    default void setExternalLink(String externalLink) {
        setFieldValue("externalLink", externalLink);
    }

    default String getExternalLink() {
        return getStringFieldValue("externalLink");
    }

    default void setVenue(Object venue) {
        setForeignField("venue", venue);
    }

    default EntityId getVenueId() {
        return getForeignEntityId("venue");
    }

    default Site getVenue() {
        return getForeignEntity("venue");
    }


}
