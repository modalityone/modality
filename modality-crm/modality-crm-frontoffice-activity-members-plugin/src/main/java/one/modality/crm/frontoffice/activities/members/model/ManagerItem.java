package one.modality.crm.frontoffice.activities.members.model;

import one.modality.base.shared.entities.Invitation;
import one.modality.base.shared.entities.Person;

/**
 * Wrapper class for displaying people who can manage my bookings.
 *
 * @author David Hello
 * @author Bruno Salmon
 */
public class ManagerItem {

    /**
     * Enum representing the different types of manager items.
     * Each type determines what actions are available in the UI.
     */
    public enum ManagerItemType {
        /** Authorized manager (has Person record with accountPerson pointing to me) - can Revoke */
        AUTHORIZED_MANAGER,

        /** Pending invitation where I'm the inviter (I invited them to manage my bookings) - can Cancel */
        PENDING_OUTGOING_INVITATION,

        /** Pending invitation where I'm the invitee (they want to manage my bookings) - can Approve/Decline */
        PENDING_INCOMING_INVITATION
    }

    private final Invitation invitation; // may be null if only Person record exists
    private final Person authorizedPerson; // may be null if only invitation exists (pending)
    private final Person accountOwner; // the actual account owner (for display purposes)
    private final ManagerItemType type;

    /**
     * Primary constructor - use this for new code.
     */
    public ManagerItem(Invitation invitation, Person authorizedPerson, Person accountOwner, ManagerItemType type) {
        this.invitation = invitation;
        this.authorizedPerson = authorizedPerson;
        this.accountOwner = accountOwner;
        this.type = type;
    }

    /**
     * Backward compatibility constructor.
     * @deprecated Use the constructor with ManagerItemType instead
     */
    @Deprecated
    public ManagerItem(Invitation invitation, Person authorizedPerson) {
        this(invitation, authorizedPerson, null);
    }

    /**
     * Backward compatibility constructor - infers type from fields.
     * @deprecated Use the constructor with ManagerItemType instead
     */
    @Deprecated
    public ManagerItem(Invitation invitation, Person authorizedPerson, Person accountOwner) {
        this.invitation = invitation;
        this.authorizedPerson = authorizedPerson;
        this.accountOwner = accountOwner;

        // Infer type from fields
        if (authorizedPerson != null) {
            this.type = ManagerItemType.AUTHORIZED_MANAGER;
        } else if (invitation != null && invitation.isPending()) {
            // For now, assume outgoing invitation (this will be updated when we refactor the controller)
            this.type = ManagerItemType.PENDING_OUTGOING_INVITATION;
        } else {
            this.type = ManagerItemType.AUTHORIZED_MANAGER; // fallback
        }
    }

    public Invitation getInvitation() {
        return invitation;
    }

    public Person getAuthorizedPerson() {
        return authorizedPerson;
    }

    public ManagerItemType getType() {
        return type;
    }

    // ========== Backward Compatibility Methods ==========

    /**
     * @deprecated Use getType() == ManagerItemType.AUTHORIZED_MANAGER instead
     */
    @Deprecated
    public boolean isAuthorized() {
        return type == ManagerItemType.AUTHORIZED_MANAGER;
    }

    /**
     * @deprecated Use getType() == PENDING_OUTGOING_INVITATION or PENDING_INCOMING_INVITATION instead
     */
    @Deprecated
    public boolean isPending() {
        return type == ManagerItemType.PENDING_OUTGOING_INVITATION ||
               type == ManagerItemType.PENDING_INCOMING_INVITATION;
    }

    /**
     * Returns the display name for this manager.
     * Prioritizes account owner's name over Person link record name.
     *
     * @return the manager's full name, or "Unknown" if no name is available
     */
    public String getManagerName() {
        // For authorized managers, show the account owner's name (not the Person link record)
        if (accountOwner != null) {
            return accountOwner.getFullName();
        }
        // Fallback to Person record name if account owner not loaded
        if (authorizedPerson != null) {
            return authorizedPerson.getFullName();
        }
        // For pending invitations, show the invitee's name
        if (invitation != null && invitation.getInvitee() != null) {
            return invitation.getInvitee().getFullName();
        }
        // For invitations without invitee, use alias names
        if (invitation != null) {
            return invitation.getAliasFirstName() + " " + invitation.getAliasLastName();
        }
        return "Unknown";
    }

    /**
     * Returns the email address for this manager.
     * Prioritizes account owner's email over Person link record email.
     *
     * @return the manager's email address, or null if no email is available
     */
    public String getManagerEmail() {
        // For authorized managers, show the account owner's email (not the Person link record)
        if (accountOwner != null && accountOwner.getEmail() != null) {
            return accountOwner.getEmail();
        }
        // Fallback to Person record email if account owner not loaded
        if (authorizedPerson != null && authorizedPerson.getEmail() != null) {
            return authorizedPerson.getEmail();
        }
        // For pending invitations, show the invitee's email
        if (invitation != null && invitation.getInvitee() != null) {
            return invitation.getInvitee().getEmail();
        }
        return null;
    }
}
