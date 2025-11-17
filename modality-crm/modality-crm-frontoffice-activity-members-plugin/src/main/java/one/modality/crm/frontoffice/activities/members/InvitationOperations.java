package one.modality.crm.frontoffice.activities.members;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.shared.entities.Invitation;
import one.modality.base.shared.entities.Person;

/**
 * Central operations class for invitation management.
 * Provides methods for creating, sending, and managing member invitations.
 *
 * @author David Hello
 */
public final class InvitationOperations {

    /**
     * Creates an invitation and sends an authorization request email
     * Authorization request: inviter wants to book for invitee (inviterPayer=true - inviter pays)
     */
    public static Future<Invitation> createAndSendAuthorizationRequest(
            Person inviter,
            Person invitee,
            String clientOrigin,
            DataSourceModel dataSourceModel) {

        return createAndSendAuthorizationRequest(inviter, invitee, null, null, clientOrigin, dataSourceModel);
    }

    /**
     * Creates and sends an authorization request with alias names (local display names).
     * The alias names don't need to match the invitee's actual names - they're used for local display.
     * Authorization request: inviter wants to book for invitee (inviterPayer=true - inviter pays)
     */
    public static Future<Invitation> createAndSendAuthorizationRequest(
            Person inviter,
            Person invitee,
            String aliasFirstName,
            String aliasLastName,
            String clientOrigin,
            DataSourceModel dataSourceModel) {

        return InvitationLinkService.createInvitation(inviter, invitee, aliasFirstName, aliasLastName, true, dataSourceModel)
                .compose(invitation ->
                        InvitationLinkService.sendAuthorizationRequest(inviter, invitee, invitation, clientOrigin, dataSourceModel)
                                .map(ignored -> invitation)
                );
    }

    /**
     * Sends an authorization request (called from SendAuthorizationRequestRequest)
     */
    static Future<Void> sendAuthorizationRequest(SendAuthorizationRequest request) {
        return InvitationLinkService.sendAuthorizationRequest(
                request.inviter(),
                request.invitee(),
                request.invitation(),
                request.clientOrigin(),
                request.dataSourceModel()
        );
    }

    /**
     * Creates an invitation for a member without an existing account
     * This is an authorization request: inviter wants to book for this person (inviterPayer=true - inviter pays)
     */
    public static Future<Invitation> createMemberWithoutAccount(
            Person inviter,
            String firstName,
            String lastName,
            DataSourceModel dataSourceModel) {

        return InvitationLinkService.createInvitation(inviter, null, firstName, lastName, true, dataSourceModel);
    }

    /**
     * Creates and sends a validation request when a member creates their own account
     * This is an authorization request: inviter wants to book for accountOwner (inviterPayer=true - inviter pays)
     */
    public static Future<Invitation> createAndSendValidationRequest(
            Person inviter,
            Person accountOwner,
            String aliasFirstName,
            String aliasLastName,
            String clientOrigin,
            DataSourceModel dataSourceModel) {

        return InvitationLinkService.createInvitation(inviter, accountOwner, aliasFirstName, aliasLastName, true, dataSourceModel)
                .compose(invitation ->
                        InvitationLinkService.sendValidationRequest(inviter, accountOwner, invitation, clientOrigin, dataSourceModel)
                                .map(ignored -> invitation)
                );
    }

    /**
     * Creates and sends an invitation to be a booking manager
     * Manager invitation: inviter asks manager to manage inviter's bookings (inviterPayer=false - manager manages for inviter)
     */
    public static Future<Invitation> createAndSendManagerInvitation(
            Person inviter,
            Person manager,
            String aliasFirstName,
            String aliasLastName,
            String clientOrigin,
            DataSourceModel dataSourceModel) {

        return InvitationLinkService.createInvitation(inviter, manager, aliasFirstName, aliasLastName, false, dataSourceModel)
                .compose(invitation ->
                        InvitationLinkService.sendInvitationToManage(inviter, manager, invitation, clientOrigin, dataSourceModel)
                                .map(ignored -> invitation)
                );
    }

    /**
     * Resends an existing invitation email
     */
    public static Future<Void> resendInvitation(
            Invitation invitation,
            String clientOrigin,
            DataSourceModel dataSourceModel) {

        Person inviter = invitation.getInviter();
        Person invitee = invitation.getInvitee();

        // Determine invitation type based on relationship
        // If invitee is set, it's either an authorization request or manager invitation
        // We can distinguish by checking which person initiated it
        if (invitee != null) {
            // This is an authorization request (inviter wants to add invitee as member)
            return InvitationLinkService.sendAuthorizationRequest(inviter, invitee, invitation, clientOrigin, dataSourceModel);
        } else {
            // This is a member without account - no email to resend
            return Future.succeededFuture();
        }
    }

    /**
     * Cancels/declines an invitation
     */
    public static Future<Void> cancelInvitation(
            Invitation invitation,
            DataSourceModel dataSourceModel) {

        return InvitationLinkService.declineInvitation(invitation);
    }

    /**
     * Removes an invitation entirely (deletes it)
     */
    public static Future<Void> removeInvitation(
            Invitation invitation) {

        UpdateStore updateStore = UpdateStore.createAbove(invitation.getStore());
        updateStore.deleteEntity(invitation);
        return updateStore.submitChanges().mapEmpty();
    }

    /**
     * Approves an invitation and sends notification
     */
    public static Future<Void> approveInvitation(
            Invitation invitation,
            String clientOrigin,
            DataSourceModel dataSourceModel) {

        Person inviter = invitation.getInviter();
        Person invitee = invitation.getInvitee();

        return InvitationLinkService.approveInvitation(invitation)
                .compose(ignored ->
                        InvitationLinkService.sendRequestApproved(inviter, invitee, clientOrigin, dataSourceModel)
                );
    }

    /**
     * Finds an invitation by approve token
     */
    public static Future<Invitation> findByApproveToken(String token, DataSourceModel dataSourceModel) {
        return InvitationLinkService.findInvitationByApproveToken(token, dataSourceModel);
    }

    /**
     * Finds an invitation by decline token
     */
    public static Future<Invitation> findByDeclineToken(String token, DataSourceModel dataSourceModel) {
        return InvitationLinkService.findInvitationByDeclineToken(token, dataSourceModel);
    }

    /**
     * Validates if a token is still valid (not expired)
     */
    public static boolean isTokenValid(Invitation invitation) {
        return InvitationLinkService.isTokenValid(invitation);
    }
}
