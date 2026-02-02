package one.modality.crm.frontoffice.activities.members.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Model class holding all data and state for the Members activity.
 * Contains observable lists and properties that the UI can bind to.
 *
 * @author David Hello
 * @author Bruno Salmon
 */
public class MembersModel {

    // === MEMBERS I CAN BOOK FOR - Three separate lists matching MemberItemType enum ===

    // MemberItemType.DIRECT_MEMBER - Person records with no accountPerson
    private final ObservableList<MemberItem> directMembersList = FXCollections.observableArrayList();

    // MemberItemType.AUTHORIZED_MEMBER - Person records with accountPerson set (linked accounts)
    private final ObservableList<MemberItem> authorizedMembersList = FXCollections.observableArrayList();

    // MemberItemType.PENDING_OUTGOING_INVITATION + PENDING_INCOMING_INVITATION
    // Combined list for both types (single query optimization)
    private final ObservableList<MemberItem> pendingMemberInvitationsList = FXCollections.observableArrayList();

    // === WHO CAN BOOK FOR ME - Three separate lists matching ManagerItemType enum ===

    // ManagerItemType.AUTHORIZED_MANAGER - Person records with accountPerson pointing to me
    private final ObservableList<ManagerItem> authorizedManagersList = FXCollections.observableArrayList();

    // ManagerItemType.PENDING_INCOMING_INVITATION - I'm the invitee, need to approve/decline
    private final ObservableList<ManagerItem> pendingIncomingManagerInvitationsList = FXCollections.observableArrayList();

    // ManagerItemType.PENDING_OUTGOING_INVITATION - I'm the inviter, waiting for their response
    private final ObservableList<ManagerItem> pendingOutgoingManagerInvitationsList = FXCollections.observableArrayList();

    // Loading states
    private final BooleanProperty loadingMembers = new SimpleBooleanProperty(true);
    private final BooleanProperty loadingManagers = new SimpleBooleanProperty(true);

    // User state
    private final StringProperty currentUserEmail = new SimpleStringProperty();

    // Getters for members lists (matching enum names)
    public ObservableList<MemberItem> getDirectMembersList() {
        return directMembersList;
    }

    public ObservableList<MemberItem> getAuthorizedMembersList() {
        return authorizedMembersList;
    }

    public ObservableList<MemberItem> getPendingMemberInvitationsList() {
        return pendingMemberInvitationsList;
    }

    // Getters for managers lists (matching enum names)
    public ObservableList<ManagerItem> getAuthorizedManagersList() {
        return authorizedManagersList;
    }

    public ObservableList<ManagerItem> getPendingIncomingManagerInvitationsList() {
        return pendingIncomingManagerInvitationsList;
    }

    public ObservableList<ManagerItem> getPendingOutgoingManagerInvitationsList() {
        return pendingOutgoingManagerInvitationsList;
    }

    // Getters for loading state properties
    public BooleanProperty loadingMembersProperty() {
        return loadingMembers;
    }

    public BooleanProperty loadingManagersProperty() {
        return loadingManagers;
    }

    public void setCurrentUserEmail(String email) {
        currentUserEmail.set(email);
    }

    // Helper methods to update loading states
    public void setLoadingMembers(boolean loading) {
        loadingMembers.set(loading);
    }

    public void setLoadingManagers(boolean loading) {
        loadingManagers.set(loading);
    }

    /**
     * Clears all data lists (typically called before refreshing data).
     */
    public void clearAllData() {
        directMembersList.clear();
        authorizedMembersList.clear();
        pendingMemberInvitationsList.clear();
        authorizedManagersList.clear();
        pendingIncomingManagerInvitationsList.clear();
        pendingOutgoingManagerInvitationsList.clear();
    }
}
