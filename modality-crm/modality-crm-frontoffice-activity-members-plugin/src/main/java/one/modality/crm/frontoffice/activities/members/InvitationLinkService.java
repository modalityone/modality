package one.modality.crm.frontoffice.activities.members;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.util.Strings;
import dev.webfx.platform.util.uuid.Uuid;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.shared.entities.Invitation;
import one.modality.base.shared.entities.Mail;
import one.modality.base.shared.entities.Person;
import one.modality.base.shared.entities.Recipient;
import one.modality.base.shared.util.ActivityHashUtil;

import java.time.LocalDateTime;

/**
 * Service for managing member invitation workflow including authorization requests,
 * validation requests, and booking manager invitations.
 *
 * @author David Hello
 */
public final class InvitationLinkService {

    private static final String APPROVE_PATH_PREFIX = "/members/approve";
    private static final String DECLINE_PATH_PREFIX = "/members/decline";
    private static final String APPROVE_PATH_FULL = APPROVE_PATH_PREFIX + "/:token";
    private static final String DECLINE_PATH_FULL = DECLINE_PATH_PREFIX + "/:token";

    // Email configuration
    private static final String EMAIL_FROM = "kbs@kadampa.net";
    private static final int KBS_MAIL_ACCOUNT_ID = 27; // Account ID for kbs@kadampa.net

    // Email template base names (language suffix will be added dynamically)
    // Authorization Request: Inviter asks invitee for permission to book for them
    private static final String AUTHORIZATION_REQUEST_TEMPLATE = "emails/AuthorizationRequest_ToInvitee";

    // Validation Request: Account owner needs to validate existing member access
    private static final String VALIDATION_REQUEST_TEMPLATE = "emails/ValidationRequest_ToAccountOwner";

    // Manager Invitation: Inviter invites someone to manage their bookings
    private static final String INVITATION_TO_MANAGE_TEMPLATE = "emails/ManagerInvitation_ToManager";

    // Authorization Approved: Invitee approved inviter's authorization request
    private static final String AUTHORIZATION_APPROVED_TEMPLATE = "emails/AuthorizationApproved_ToInviter";

    // Manager Accepted: Manager accepted the invitation to manage bookings
    private static final String MANAGER_ACCEPTED_TEMPLATE = "emails/ManagerAccepted_ToInviter";

    /**
     * Gets the localized template path based on current i18n locale
     * @param templateBaseName Base template name without language suffix
     * @return Full template path with language code (e.g., "emails/Template_de.html" or "emails/Template.html" for English)
     */
    private static String getLocalizedTemplatePath(String templateBaseName) {
        Object langObj = I18n.getLanguage();
        String languageCode = langObj != null ? langObj.toString() : "en";
        if (languageCode.isEmpty()) {
            languageCode = "en";
        }

        // English templates don't have language suffix
        if ("en".equals(languageCode)) {
            return templateBaseName + ".html";
        }

        // Other languages have _language suffix
        return templateBaseName + "_" + languageCode + ".html";
    }

    /**
     * Sends an authorization request email when someone wants to add a member with an existing account
     */
    public static Future<Void> sendAuthorizationRequest(
            Person inviter,
            Person invitee,
            Invitation invitation,
            String clientOrigin,
            DataSourceModel dataSourceModel) {

        // Use existing token from invitation (already set in createInvitation)
        String token = invitation.getToken();

        String approveLink = buildLink(clientOrigin, APPROVE_PATH_FULL, token);
        String declineLink = buildLink(clientOrigin, DECLINE_PATH_FULL, token);

        // Load localized template asynchronously (GWT-compatible)
        String templatePath = getLocalizedTemplatePath(AUTHORIZATION_REQUEST_TEMPLATE);
        Promise<String> promise = Promise.promise();
        Resource.loadText(
                Resource.toUrl(templatePath, InvitationLinkService.class),
                promise::complete,
                promise::fail
        );
        return promise.future().compose(template -> {
            String body = template
                    .replace("[inviteeName]", invitee.getFullName())
                    .replace("[inviterName]", inviter.getFullName())
                    .replace("[inviterEmail]", inviter.getEmail())
                    .replace("[approveLink]", approveLink)
                    .replace("[declineLink]", declineLink);

            String subject = I18n.getI18nText(MembersI18nKeys.EmailSubjectAuthorizationRequest);
            return sendEmail(invitee, subject, body, dataSourceModel);
        });
    }

    /**
     * Sends a validation request when a member creates an account after being added without email
     */
    public static Future<Void> sendValidationRequest(
            Person inviter,
            Person invitee,
            Invitation invitation,
            String clientOrigin,
            DataSourceModel dataSourceModel) {

        // Use existing token from invitation (already set in createInvitation)
        String token = invitation.getToken();

        String approveLink = buildLink(clientOrigin, APPROVE_PATH_FULL, token);
        String declineLink = buildLink(clientOrigin, DECLINE_PATH_FULL, token);

        // Load localized template asynchronously (GWT-compatible)
        String templatePath = getLocalizedTemplatePath(VALIDATION_REQUEST_TEMPLATE);
        Promise<String> promise = Promise.promise();
        Resource.loadText(
                Resource.toUrl(templatePath, InvitationLinkService.class),
                promise::complete,
                promise::fail
        );
        return promise.future().compose(template -> {
            String body = template
                    .replace("[inviteeName]", invitee.getFullName())
                    .replace("[inviterName]", inviter.getFullName())
                    .replace("[approveLink]", approveLink)
                    .replace("[declineLink]", declineLink);

            String subject = I18n.getI18nText(MembersI18nKeys.EmailSubjectValidationRequest);
            return sendEmail(invitee, subject, body, dataSourceModel);
        });
    }

    /**
     * Sends an invitation to manage bookings
     */
    public static Future<Void> sendInvitationToManage(
            Person inviter,
            Person manager,
            Invitation invitation,
            String clientOrigin,
            DataSourceModel dataSourceModel) {

        // Use existing token from invitation (already set in createInvitation)
        String token = invitation.getToken();

        String acceptLink = buildLink(clientOrigin, APPROVE_PATH_FULL, token);
        String declineLink = buildLink(clientOrigin, DECLINE_PATH_FULL, token);

        // Load localized template asynchronously (GWT-compatible)
        String templatePath = getLocalizedTemplatePath(INVITATION_TO_MANAGE_TEMPLATE);
        Promise<String> promise = Promise.promise();
        Resource.loadText(
                Resource.toUrl(templatePath, InvitationLinkService.class),
                promise::complete,
                promise::fail
        );
        return promise.future().compose(template -> {
            String body = template
                    .replace("[managerName]", manager.getFullName())
                    .replace("[inviterName]", inviter.getFullName())
                    .replace("[inviterEmail]", inviter.getEmail())
                    .replace("[acceptLink]", acceptLink)
                    .replace("[declineLink]", declineLink);

            String subject = I18n.getI18nText(MembersI18nKeys.EmailSubjectManagerInvitation);
            return sendEmail(manager, subject, body, dataSourceModel);
        });
    }

    /**
     * Sends a notification that an authorization request was approved
     * Used when invitee approves inviter's request to book for them
     */
    public static Future<Void> sendRequestApproved(
            Person requester,
            Person approver,
            String clientOrigin,
            DataSourceModel dataSourceModel) {

        String accountLink = clientOrigin + ActivityHashUtil.withHashPrefix("/members");

        // Load localized template asynchronously (GWT-compatible)
        String templatePath = getLocalizedTemplatePath(AUTHORIZATION_APPROVED_TEMPLATE);
        Promise<String> promise = Promise.promise();
        Resource.loadText(
                Resource.toUrl(templatePath, InvitationLinkService.class),
                promise::complete,
                promise::fail
        );
        return promise.future().compose(template -> {
            String body = template
                    .replace("[requesterName]", requester.getFullName())
                    .replace("[approverName]", approver.getFullName())
                    .replace("[accountLink]", accountLink);

            String subject = I18n.getI18nText(MembersI18nKeys.EmailSubjectAuthorizationApproved);
            return sendEmail(requester, subject, body, dataSourceModel);
        });
    }

    /**
     * Sends a notification that a manager invitation was accepted
     * Used when manager accepts inviter's invitation to manage their bookings
     */
    public static Future<Void> sendManagerAccepted(
            Person inviter,
            Person manager,
            String clientOrigin,
            DataSourceModel dataSourceModel) {

        String accountLink = clientOrigin + ActivityHashUtil.withHashPrefix("/members");

        // Load localized template asynchronously (GWT-compatible)
        String templatePath = getLocalizedTemplatePath(MANAGER_ACCEPTED_TEMPLATE);
        Promise<String> promise = Promise.promise();
        Resource.loadText(
                Resource.toUrl(templatePath, InvitationLinkService.class),
                promise::complete,
                promise::fail
        );
        return promise.future().compose(template -> {
            String body = template
                    .replace("[inviterName]", inviter.getFullName())
                    .replace("[managerName]", manager.getFullName())
                    .replace("[accountLink]", accountLink);

            String subject = I18n.getI18nText(MembersI18nKeys.EmailSubjectManagerAccepted);
            return sendEmail(inviter, subject, body, dataSourceModel);
        });
    }

    /**
     * Creates or updates an invitation record
     * @param inviterPayer true if inviter invites invitee to manage inviter's bookings (manager invitation),
     *                     false if inviter wants to manage invitee's bookings (authorization request)
     */
    public static Future<Invitation> createInvitation(
            Person inviter,
            Person invitee,
            String aliasFirstName,
            String aliasLastName,
            boolean inviterPayer,
            DataSourceModel dataSourceModel) {

        EntityStore entityStore = EntityStore.create(dataSourceModel);
        UpdateStore updateStore = UpdateStore.createAbove(entityStore);
        Invitation invitation = updateStore.insertEntity(Invitation.class);
        invitation.setCreationDate(LocalDateTime.now());
        invitation.setInviter(inviter);
        invitation.setInvitee(invitee);
        invitation.setAliasFirstName(aliasFirstName);
        invitation.setAliasLastName(aliasLastName);
        invitation.setInviterPayer(inviterPayer);
        invitation.setPending(true);
        invitation.setAccepted(false);
        invitation.setToken(Uuid.randomUuid()); // Generate token immediately to satisfy database constraint

        return updateStore.submitChanges()
                .map(ignored -> invitation);
    }

    /**
     * Approves an invitation and creates or updates the appropriate Person record.
     * Two cases:
     * 1. Authorization request (inviterPayer=true): Inviter wants to book for invitee
     *    - If Person exists (validation case): updates accountPerson
     *    - If Person doesn't exist: creates new Person in inviter's account
     * 2. Manager invitation (inviterPayer=false): Invitee will manage inviter's bookings
     *    - Creates Person in invitee's account with accountPerson pointing to inviter
     */
    public static Future<Void> approveInvitation(Invitation invitation) {
        EntityStore entityStore = invitation.getStore();
        UpdateStore updateStore = UpdateStore.createAbove(entityStore);
        Invitation updated = updateStore.updateEntity(invitation);
        updated.setPending(false);
        updated.setAccepted(true);
        updated.setUsageDate(java.time.LocalDateTime.now());

        Person inviter = invitation.getInviter();
        Person invitee = invitation.getInvitee();
        Boolean inviterPayer = invitation.isInviterPayer();

        // CASE 1: Authorization request (inviterPayer=true)
        // Inviter wants to book for invitee - check if Person exists first (validation case)
        if (Boolean.TRUE.equals(inviterPayer)) {
            // Query for existing Person in inviter's account with matching email
            return entityStore.<Person>executeQuery(
                    "select id,firstName,lastName,email,frontendAccount,accountPerson,owner " +
                    "from Person " +
                    "where frontendAccount=$1 and lower(email)=$2 and owner=false and removed!=true limit 1",
                    inviter.getFrontendAccount().getId(),
                    invitee.getEmail().toLowerCase())
                .compose(existingPersons -> {
                    if (!existingPersons.isEmpty()) {
                        // Person already exists (validation case) - update accountPerson
                        Person existingPerson = existingPersons.get(0);
                        Person personToUpdate = updateStore.updateEntity(existingPerson);
                        personToUpdate.setAccountPerson(invitee);  // Link to INVITEE (the account owner)
                    } else {
                        // Person doesn't exist - create new Person in INVITER's account
                        Person newPerson = updateStore.insertEntity(Person.class);
                        newPerson.setFirstName(invitation.getAliasFirstName());
                        newPerson.setLastName(invitation.getAliasLastName());
                        newPerson.setFrontendAccount(inviter.getFrontendAccount());  // Link to INVITER's account
                        newPerson.setAccountPerson(invitee);  // Link to INVITEE (the account owner)
                        newPerson.setOwner(false);  // Not the account owner
                    }
                    return updateStore.submitChanges().mapEmpty();
                });
        }
        // CASE 2: Manager invitation (inviterPayer=false)
        // Invitee will manage inviter's bookings - create Person in INVITEE's account
        else if (Boolean.FALSE.equals(inviterPayer)) {
            Person newPerson = updateStore.insertEntity(Person.class);
            // Use INVITER's name (not alias which contains manager's name)
            // The Person record represents the inviter in the invitee's (manager's) account
            newPerson.setFirstName(inviter.getFirstName());
            newPerson.setLastName(inviter.getLastName());
            newPerson.setFrontendAccount(invitee.getFrontendAccount());  // Link to INVITEE's account
            newPerson.setAccountPerson(inviter);  // Link to INVITER (the account owner)
            newPerson.setOwner(false);  // Not the account owner
        }

        return updateStore.submitChanges().mapEmpty();
    }

    /**
     * Declines an invitation
     */
    public static Future<Void> declineInvitation(Invitation invitation) {
        UpdateStore updateStore = UpdateStore.createAbove(invitation.getStore());
        Invitation updated = updateStore.updateEntity(invitation);
        updated.setPending(false);
        updated.setAccepted(false);
        return updateStore.submitChanges().mapEmpty();
    }

    /**
     * Finds an invitation by token (used for both approve and decline actions)
     * Loads all fields needed for approval including frontendAccount and alias names
     */
    public static Future<Invitation> findInvitationByToken(String token, DataSourceModel dataSourceModel) {
        return EntityStore.create(dataSourceModel)
                .<Invitation>executeQuery(
                        "select id,inviter.(id,fullName,email,frontendAccount),invitee.(id,fullName,email,frontendAccount)," +
                        "aliasFirstName,aliasLastName,pending,accepted,token,creationDate,inviterPayer " +
                        "from Invitation where token=$1 and pending=true limit 1",
                        token)
                .map(invitations -> invitations.isEmpty() ? null : invitations.get(0));
    }

    /**
     * Finds an invitation by approve token (kept for backward compatibility, delegates to findInvitationByToken)
     */
    public static Future<Invitation> findInvitationByApproveToken(String token, DataSourceModel dataSourceModel) {
        return findInvitationByToken(token, dataSourceModel);
    }

    /**
     * Finds an invitation by decline token (kept for backward compatibility, delegates to findInvitationByToken)
     */
    public static Future<Invitation> findInvitationByDeclineToken(String token, DataSourceModel dataSourceModel) {
        return findInvitationByToken(token, dataSourceModel);
    }

    /**
     * Validates if a token is still valid (not expired)
     * Token expiry is calculated as creationDate + 7 days
     */
    public static boolean isTokenValid(Invitation invitation) {
        return invitation != null && invitation.isTokenValid();
    }

    // Helper methods

    /**
     * Builds a link with the client origin and token
     */
    private static String buildLink(String clientOrigin, String pathTemplate, String token) {
        return clientOrigin + ActivityHashUtil.withHashPrefix(pathTemplate.replace(":token", token));
    }

    private static Future<Void> sendEmail(Person recipient, String subject, String body, DataSourceModel dataSourceModel) {
        String to = recipient != null ? recipient.getEmail() : null;
        if (Strings.isEmpty(to)) {
            return Future.failedFuture("Email address is required");
        }

        // Create Mail and Recipient records for email processing in a single transaction
        EntityStore entityStore = EntityStore.create(dataSourceModel);
        UpdateStore updateStore = UpdateStore.createAbove(entityStore);

        // Create Mail record
        Mail mail = updateStore.insertEntity(Mail.class);
        mail.setFromName("Kadampa Booking System");
        mail.setFromEmail(EMAIL_FROM);
        mail.setSubject(subject);
        mail.setContent(body);
        mail.setOut(true);
        mail.setAccount(KBS_MAIL_ACCOUNT_ID);

        // Create Recipient record in the same transaction
        Recipient recipientEntity = updateStore.insertEntity(Recipient.class);
        recipientEntity.setMail(mail);
        // Note: setPerson is skipped for now to avoid entity store context issues
        // The person link can be established later if needed
        assert recipient != null;
        recipientEntity.setName(recipient.getFullName());
        recipientEntity.setEmail(to);
        recipientEntity.setTo(true);
        recipientEntity.setCc(false);
        recipientEntity.setBcc(false);
        recipientEntity.setOk(false); // Will be set to true after successful sending

        // Submit both Mail and Recipient in one transaction
        return updateStore.submitChanges().mapEmpty();
    }
}
