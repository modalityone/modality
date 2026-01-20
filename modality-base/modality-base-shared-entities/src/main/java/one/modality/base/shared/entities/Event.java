package one.modality.base.shared.entities;

import dev.webfx.platform.util.Strings;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.*;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author Bruno Salmon
 */
public interface Event extends Entity,
    EntityHasName,
    EntityHasLabel,
    EntityHasIcon,
    EntityHasOrganization,
    EntityHasCorporation {

    String state = "state";
    String advertised = "advertised";
    String type = "type";
    String startDate = "startDate";
    String endDate = "endDate";
    String openingDate = "openingDate";
    String bookingProcessStart = "bookingProcessStart";
    String vodExpirationDate = "vodExpirationDate";
    String vodProcessingTimeMinutes = "vodProcessingTimeMinutes";
    String audioExpirationDate = "audioExpirationDate";
    String audioClosingDate = "audioClosingDate";
    String livestreamUrl = "livestreamUrl";
    String livestreamMessageLabel = "livestreamMessageLabel";
    String live = "live";
    String feesBottomLabel = "feesBottomLabel";
    String kbs3 = "kbs3";
    String description = "description";
    String shortDescription = "shortDescription";
    String shortDescriptionLabel = "shortDescriptionLabel";
    String longDescriptionLabel = "longDescriptionLabel";
    String externalLink = "externalLink";
    String venue = "venue";
    String teachingsDayTicket = "teachingsDayTicket";
    String audioRecordingsDayTicket = "audioRecordingsDayTicket";
    String recurringWithAudio = "recurringWithAudio";
    String recurringWithVideo = "recurringWithVideo";
    String repeatable = "repeatable";
    String repeatedEvent = "repeatedEvent";
    String repeatAudio = "repeatAudio";
    String repeatVideo = "repeatVideo";
    String timezone = "timezone";
    String termsUrlEn = "termsUrlEn";

    // Dynamic field used for messaging purpose only (not persisted in the database)
    String queueProgress = "queueProgress";

    default void setState(Object value) {
        setFieldValue(state, Strings.stringValue(value));
    }

    default EventState getState() {
        return EventState.of(getStringFieldValue(state));
    }

    default void setAdvertised(Boolean value) {
        setFieldValue(advertised, value);
    }

    default Boolean isAdvertised() {
        return getBooleanFieldValue(advertised);
    }


    default void setType(Object value) {
        setForeignField(type, value);
    }

    default EntityId getTypeId() {
        return getForeignEntityId(type);
    }

    default EventType getType() {
        return getForeignEntity(type);
    }


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

    default void setOpeningDate(LocalDateTime value) {
        setFieldValue(openingDate, value);
    }

    default LocalDateTime getOpeningDate() {
        return getLocalDateTimeFieldValue(openingDate);
    }

    default void setBookingProcessStart(LocalDateTime value) {
        setFieldValue(bookingProcessStart, value);
    }

    default LocalDateTime getBookingProcessStart() {
        return getLocalDateTimeFieldValue(bookingProcessStart);
    }

    default void setVodExpirationDate(LocalDateTime value) {
        setFieldValue(vodExpirationDate, value);
    }

    default Integer getVodProcessingTimeMinutes() {
        return getIntegerFieldValue(vodProcessingTimeMinutes);
    }

    default void setVodProcessingTimeMinutes(Integer value) {
        setFieldValue(vodProcessingTimeMinutes,value);
    }

    default LocalDateTime getVodExpirationDate() {
        return getLocalDateTimeFieldValue(vodExpirationDate);
    }

    default void setAudioExpirationDate(LocalDateTime value) {
        setFieldValue(audioExpirationDate, value);
    }

    default LocalDateTime getAudioExpirationDate() {
        return getLocalDateTimeFieldValue(audioExpirationDate);
    }

    default void setAudioClosingDate(LocalDateTime value) {
        setFieldValue(audioClosingDate, value);
    }

    default LocalDateTime getAudioClosingDate() {
        return getLocalDateTimeFieldValue(audioClosingDate);
    }

    default void setLivestreamUrl(String value) {
        setFieldValue(livestreamUrl, value);
    }

    default String getLivestreamUrl() {
        return getStringFieldValue(livestreamUrl);
    }

    default void setLivestreamMessageLabel(Object value) {
        setForeignField(livestreamMessageLabel, value);
    }

    default EntityId getLivestreamMessageLabelId() {
        return getForeignEntityId(livestreamMessageLabel);
    }

    default Label getLivestreamMessageLabel() {
        return getForeignEntity(livestreamMessageLabel);
    }

    default void setLive(Boolean value) {
        setFieldValue(live, value);
    }

    default Boolean isLive() {
        return getBooleanFieldValue(live);
    }

    default void setFeesBottomLabel(Object value) {
        setForeignField(feesBottomLabel, value);
    }

    default EntityId getFeesBottomLabelId() {
        return getForeignEntityId(feesBottomLabel);
    }

    default Label getFeesBottomLabel() {
        return getForeignEntity(feesBottomLabel);
    }

    default void setKbs3(Boolean value) {
        setFieldValue(kbs3, value);
    }

    default Boolean isKbs3() {
        return getBooleanFieldValue(kbs3);
    }

    default void setDescription(String value) {
        setFieldValue(description, value);
    }

    default String getDescription() {
        return getStringFieldValue(description);
    }

    default void setShortDescription(String value) {
        setFieldValue(shortDescription, value);
    }

    default String getShortDescription() {
        return getStringFieldValue(shortDescription);
    }

    default void setShortDescriptionLabel(Object value) {
        setForeignField(shortDescriptionLabel, value);
    }

    default EntityId getShortDescriptionLabelId() {
        return getForeignEntityId(shortDescriptionLabel);
    }

    default Label getShortDescriptionLabel() {
        return getForeignEntity(shortDescriptionLabel);
    }

    default void setLongDescriptionLabel(Object value) {
        setForeignField(longDescriptionLabel, value);
    }

    default EntityId getLongDescriptionLabelId() {
        return getForeignEntityId(longDescriptionLabel);
    }

    default Label getLongDescriptionLabel() {
        return getForeignEntity(longDescriptionLabel);
    }

    default void setExternalLink(String value) {
        setFieldValue(externalLink, value);
    }

    default String getExternalLink() {
        return getStringFieldValue(externalLink);
    }

    default void setVenue(Object value) {
        setForeignField(venue, value);
    }

    default EntityId getVenueId() {
        return getForeignEntityId(venue);
    }

    default Site getVenue() {
        return getForeignEntity(venue);
    }

    default void setTeachingsDayTicket(Boolean value) {
        setFieldValue(teachingsDayTicket, value);
    }

    default Boolean isTeachingsDayTicket() {
        return getBooleanFieldValue(teachingsDayTicket);
    }

    default void setAudioRecordingsDayTicket(Boolean value) {
        setFieldValue(audioRecordingsDayTicket, value);
    }

    default Boolean isAudioRecordingsDayTicket() {
        return getBooleanFieldValue(audioRecordingsDayTicket);
    }

    default void setRecurringWithAudio(Boolean value) {
        setFieldValue(recurringWithAudio, value);
    }

    default Boolean isRecurringWithAudio() {
        return getBooleanFieldValue(recurringWithAudio);
    }

    default void setRecurringWithVideo(Boolean value) {
        setFieldValue(recurringWithVideo, value);
    }

    default Boolean isRecurringWithVideo() {
        return getBooleanFieldValue(recurringWithVideo);
    }

    default void setRepeatedEvent(Object value) {
        setForeignField(repeatedEvent, value);
    }

    default void setRepeatable(Boolean value) {
        setFieldValue(repeatable, value);
    }

    default Boolean isRepeatable() {
        return getBooleanFieldValue(repeatable);
    }

    default EntityId getRepeatedEventId() {
        return getForeignEntityId(repeatedEvent);
    }

    default Event getRepeatedEvent() {
        return getForeignEntity(repeatedEvent);
    }

    default void setRepeatAudio(Boolean value) {
        setFieldValue(repeatAudio, value);
    }

    default Boolean isRepeatAudio() {
        return getBooleanFieldValue(repeatAudio);
    }

    default void setRepeatVideo(Boolean value) {
        setFieldValue(repeatVideo, value);
    }

    default Boolean isRepeatVideo() {
        return getBooleanFieldValue(repeatVideo);
    }

    default Boolean isRecurring() {
        EventType type = getType();
        return type == null ? null : type.isRecurring();
    }

    default Boolean isOnlineEvent() { // Temporary method while in-person and online events are separated
        String name = getName();
        return name == null ? null : name.toLowerCase().contains("online");
    }

    default void setTimezone(String value) {
        setFieldValue(timezone, value);
    }

    default String getTimezone() {
        return getStringFieldValue(timezone);
    }

    ZoneId getEventZoneId(); // requires some logic => implemented in EventImpl

    Clock getEventClock(); // requires some logic => implemented in EventImpl

    default LocalDateTime nowInEventTimezone() {
        return LocalDateTime.now(getEventClock());
    }

    default LocalDate todayInEventTimezone() {
        return nowInEventTimezone().toLocalDate();
    }

    default void setTermsUrlEn(String value) {
        setFieldValue(termsUrlEn, value);
    }

    default String getTermsUrlEn() {
        return getStringFieldValue(termsUrlEn);
    }

}