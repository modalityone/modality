package one.modality.crm.frontoffice.activities.members;

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

    // Email template paths and subjects
    private static final String AUTHORIZATION_REQUEST_SUBJECT = "Authorization Request - Kadampa Booking System";
    private static final String AUTHORIZATION_REQUEST_TEMPLATE = "emails/AuthorizationRequestMailBody.html";

    private static final String VALIDATION_REQUEST_SUBJECT = "Validation Required - Kadampa Booking System";
    private static final String VALIDATION_REQUEST_TEMPLATE = "emails/ValidationRequestMailBody.html";

    private static final String INVITATION_TO_MANAGE_SUBJECT = "Booking Manager Invitation - Kadampa Booking System";
    private static final String INVITATION_TO_MANAGE_TEMPLATE = "emails/InvitationToManageMailBody.html";

    private static final String REQUEST_APPROVED_SUBJECT = "Request Approved - Kadampa Booking System";
    private static final String REQUEST_APPROVED_TEMPLATE = "emails/RequestApprovedMailBody.html";

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

        // Load template asynchronously (GWT-compatible)
        Promise<String> promise = Promise.promise();
        Resource.loadText(
                Resource.toUrl(AUTHORIZATION_REQUEST_TEMPLATE, InvitationLinkService.class),
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

            return sendEmail(invitee, AUTHORIZATION_REQUEST_SUBJECT, body, dataSourceModel);
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

        // Load template asynchronously (GWT-compatible)
        Promise<String> promise = Promise.promise();
        Resource.loadText(
                Resource.toUrl(VALIDATION_REQUEST_TEMPLATE, InvitationLinkService.class),
                promise::complete,
                promise::fail
        );
        return promise.future().compose(template -> {
            String body = template
                    .replace("[inviteeName]", invitee.getFullName())
                    .replace("[inviterName]", inviter.getFullName())
                    .replace("[approveLink]", approveLink)
                    .replace("[declineLink]", declineLink);

            return sendEmail(invitee, VALIDATION_REQUEST_SUBJECT, body, dataSourceModel);
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

        // Load template asynchronously (GWT-compatible)
        Promise<String> promise = Promise.promise();
        Resource.loadText(
                Resource.toUrl(INVITATION_TO_MANAGE_TEMPLATE, InvitationLinkService.class),
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

            return sendEmail(manager, INVITATION_TO_MANAGE_SUBJECT, body, dataSourceModel);
        });
    }

    /**
     * Sends a notification that a request was approved
     */
    public static Future<Void> sendRequestApproved(
            Person requester,
            Person approver,
            String clientOrigin,
            DataSourceModel dataSourceModel) {

        String accountLink = clientOrigin + "/members";

        // Load template asynchronously (GWT-compatible)
        Promise<String> promise = Promise.promise();
        Resource.loadText(
                Resource.toUrl(REQUEST_APPROVED_TEMPLATE, InvitationLinkService.class),
                promise::complete,
                promise::fail
        );
        return promise.future().compose(template -> {
            String body = template
                    .replace("[requesterName]", requester.getFullName())
                    .replace("[approverName]", approver.getFullName())
                    .replace("[accountLink]", accountLink);

            return sendEmail(requester, REQUEST_APPROVED_SUBJECT, body, dataSourceModel);
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
     * Approves an invitation and creates the appropriate Person record.
     * Two cases:
     * 1. Authorization request (inviterPayer=true): Inviter wants to book for invitee
     *    - Creates Person in inviter's account with accountPerson pointing to invitee
     * 2. Manager invitation (inviterPayer=false): Invitee will manage inviter's bookings
     *    - Creates Person in invitee's account with accountPerson pointing to inviter
     */
    public static Future<Void> approveInvitation(Invitation invitation) {
        UpdateStore updateStore = UpdateStore.createAbove(invitation.getStore());
        Invitation updated = updateStore.updateEntity(invitation);
        updated.setPending(false);
        updated.setAccepted(true);

        Person inviter = invitation.getInviter();
        Person invitee = invitation.getInvitee();
        Boolean inviterPayer = invitation.isInviterPayer();

        // CASE 1: Authorization request (inviterPayer=true)
        // Inviter wants to book for invitee - create Person in INVITER's account
        if (Boolean.TRUE.equals(inviterPayer)) {
            Person newPerson = updateStore.insertEntity(Person.class);
            newPerson.setFirstName(invitation.getAliasFirstName());
            newPerson.setLastName(invitation.getAliasLastName());
            newPerson.setFrontendAccount(inviter.getFrontendAccount());  // Link to INVITER's account
            newPerson.setAccountPerson(invitee);  // Link to INVITEE (the account owner)
            newPerson.setOwner(false);  // Not the account owner
        }
        // CASE 2: Manager invitation (inviterPayer=false)
        // Invitee will manage inviter's bookings - create Person in INVITEE's account
        else if (Boolean.FALSE.equals(inviterPayer)) {
            Person newPerson = updateStore.insertEntity(Person.class);
            newPerson.setFirstName(invitation.getAliasFirstName());
            newPerson.setLastName(invitation.getAliasLastName());
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
                        "from Invitation where token=? and pending=true limit 1",
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

    private static String buildLink(String clientOrigin, String pathTemplate, String token) {
        if (!clientOrigin.startsWith("http")) {
            clientOrigin = (clientOrigin.contains(":80") ? "http" : "https") + clientOrigin.substring(clientOrigin.indexOf("://"));
        }
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
        // Note: account_id is required - using 27 for kbs@kadampa.net account
        // This matches the value used in ModalityServerMailServiceProvider
        mail.setAccount(27);

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
