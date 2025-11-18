package one.modality.crm.frontoffice.activities.members.model;

import one.modality.base.shared.entities.Invitation;
import one.modality.base.shared.entities.Person;

/**
 * Wrapper class to hold Person + optional Invitation + account detection.
 * Represents a member that I can book for.
 *
 * @author David Hello
 * @author Bruno Salmon
 */
public class MemberItem {

    /**
     * Enum representing the different types of member items.
     * Each type determines what actions are available in the UI.
     */
    public enum MemberItemType {
        /** Person with no accountPerson - can Edit and Remove */
        DIRECT_MEMBER,

        /** Person with accountPerson set (linked account) - can only Remove */
        AUTHORIZED_MEMBER,

        /** Pending invitation where I'm the inviter (waiting for their response) - can Cancel */
        PENDING_OUTGOING_INVITATION,

        /** Pending invitation where I'm the invitee (I need to respond) - can Approve/Decline */
        PENDING_INCOMING_INVITATION
    }

    private final Person person;
    private final Invitation invitation; // null if no invitation
    private final MemberItemType type;
    private final Person matchingAccountPerson; // null if no matching account exists

    /**
     * Primary constructor - use this for new code.
     */
    public MemberItem(Person person, Invitation invitation, MemberItemType type) {
        this(person, invitation, type, null);
    }

    /**
     * Constructor with matching account detection.
     * @param matchingAccountPerson The account owner (owner=true) with matching email, or null if none exists
     */
    public MemberItem(Person person, Invitation invitation, MemberItemType type, Person matchingAccountPerson) {
        this.person = person;
        this.invitation = invitation;
        this.type = type;
        this.matchingAccountPerson = matchingAccountPerson;
    }

    /**
     * Backward compatibility constructor - converts boolean flags to enum type.
     * @deprecated Use the constructor with MemberItemType instead
     */
    @Deprecated
    public MemberItem(Person person, Invitation invitation, boolean isLinkedAccount, boolean isInvitee) {
        this.person = person;
        this.invitation = invitation;
        this.matchingAccountPerson = null; // No matching account detection in deprecated constructor

        // Convert boolean flags to enum type
        if (invitation != null && invitation.isPending()) {
            this.type = isInvitee ? MemberItemType.PENDING_INCOMING_INVITATION : MemberItemType.PENDING_OUTGOING_INVITATION;
        } else if (isLinkedAccount) {
            this.type = MemberItemType.AUTHORIZED_MEMBER;
        } else {
            this.type = MemberItemType.DIRECT_MEMBER;
        }
    }

    /**
     * Backward compatibility constructor (defaults to isInvitee=false for outgoing invitations)
     * @deprecated Use the constructor with MemberItemType instead
     */
    @Deprecated
    public MemberItem(Person person, Invitation invitation, boolean isLinkedAccount) {
        this(person, invitation, isLinkedAccount, false);
    }

    public Person getPerson() {
        return person;
    }

    public Invitation getInvitation() {
        return invitation;
    }

    public MemberItemType getType() {
        return type;
    }

    public Person getMatchingAccountPerson() {
        return matchingAccountPerson;
    }

    /**
     * Returns true if a matching account exists for this direct member.
     * Only relevant for DIRECT_MEMBER type.
     */
    public boolean hasMatchingAccount() {
        return matchingAccountPerson != null;
    }

    // ========== Backward Compatibility Methods ==========

    /**
     * @deprecated Use getType() == MemberItemType.PENDING_INCOMING_INVITATION instead
     */
    @Deprecated
    public boolean isInvitee() {
        return type == MemberItemType.PENDING_INCOMING_INVITATION;
    }

    /**
     * @deprecated Use getType() == MemberItemType.AUTHORIZED_MEMBER instead
     */
    @Deprecated
    public boolean isLinkedAccount() {
        return type == MemberItemType.AUTHORIZED_MEMBER;
    }

    /**
     * Returns the email for this member.
     */
    public String getEmail() {
        return person != null ? person.getEmail() : null;
    }
}
