package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;

import java.time.LocalDateTime;

/**
 * @author Bruno Salmon
 */
public interface Invitation extends Entity {

    String creationDate = "creationDate";
    String usageDate = "usageDate";
    String token = "token";
    String inviter = "inviter";
    String invitee = "invitee";
    String inviterPayer = "inviterPayer";
    String pending = "pending";
    String accepted = "accepted";
    String aliasFirstName = "aliasFirstName";
    String aliasLastName = "aliasLastName";
    String createdAliasPerson = "createdAliasPerson";

    default void setCreationDate(LocalDateTime value) {
        setFieldValue(creationDate, value);
    }

    default LocalDateTime getCreationDate() {
        return getLocalDateTimeFieldValue(creationDate);
    }

    default void setUsageDate(LocalDateTime value) {
        setFieldValue(usageDate, value);
    }

    default LocalDateTime getUsageDate() {
        return getLocalDateTimeFieldValue(usageDate);
    }

    default void setToken(String value) {
        setFieldValue(token, value);
    }

    default String getToken() {
        return getStringFieldValue(token);
    }

    // Inviter person
    default void setInviter(Object value) {
        setForeignField(inviter, value);
    }

    default EntityId getInviterId() {
        return getForeignEntityId(inviter);
    }

    default Person getInviter() {
        return getForeignEntity(inviter);
    }

    // Invitee person
    default void setInvitee(Object value) {
        setForeignField(invitee, value);
    }

    default EntityId getInviteeId() {
        return getForeignEntityId(invitee);
    }

    default Person getInvitee() {
        return getForeignEntity(invitee);
    }

    default void setInviterPayer(Boolean value) {
        setFieldValue(inviterPayer, value);
    }

    default Boolean isInviterPayer() {
        return getBooleanFieldValue(inviterPayer);
    }

    // Pending and accepted flags
    default void setPending(Boolean value) {
        setFieldValue(pending, value);
    }

    default Boolean isPending() {
        return getBooleanFieldValue(pending);
    }

    default void setAccepted(Boolean value) {
        setFieldValue(accepted, value);
    }

    default Boolean isAccepted() {
        return getBooleanFieldValue(accepted);
    }

    // Alias first name and last name
    default void setAliasFirstName(String value) {
        setFieldValue(aliasFirstName, value);
    }

    default String getAliasFirstName() {
        return getStringFieldValue(aliasFirstName);
    }

    default void setAliasLastName(String value) {
        setFieldValue(aliasLastName, value);
    }

    default String getAliasLastName() {
        return getStringFieldValue(aliasLastName);
    }

    // Created alias person
    default void setCreatedAliasPerson(Object value) {
        setForeignField(createdAliasPerson, value);
    }

    default EntityId getCreatedAliasPersonId() {
        return getForeignEntityId(createdAliasPerson);
    }

    default Person getCreatedAliasPerson() {
        return getForeignEntity(createdAliasPerson);
    }

    // Token expiry is calculated dynamically: creationDate + 7 days
    default LocalDateTime getTokenExpiry() {
        LocalDateTime creationDate = getCreationDate();
        return creationDate != null ? creationDate.plusDays(7) : null;
    }

    // Check if token is still valid (not expired)
    default boolean isTokenValid() {
        LocalDateTime expiry = getTokenExpiry();
        return expiry != null && LocalDateTime.now().isBefore(expiry);
    }

}
