package one.modality.crm.frontoffice.activities.members.view;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.shared.entities.Invitation;
import one.modality.base.shared.entities.Person;
import one.modality.crm.frontoffice.activities.members.MembersI18nKeys;
import one.modality.crm.frontoffice.activities.members.model.MemberItem;
import one.modality.crm.frontoffice.activities.members.model.ManagerItem;

/**
 * Factory class for creating UI elements for the Members activity.
 * Handles rendering of member items, manager items, pending requests, and common UI components.
 *
 * @author David Hello
 * @author Bruno Salmon
 */
public record MembersItemRendererFactory(MemberActionHandler memberActionHandler,
                                         ManagerActionHandler managerActionHandler,
                                         PendingRequestActionHandler pendingRequestActionHandler) {

    /**
     * Callback interface for handling user actions on member items.
     */
    public interface MemberActionHandler {
        void onRemoveLinkedAccount(Person person);

        void onResendInvitation(Invitation invitation);

        void onCancelInvitation(Invitation invitation);

        void onEditMember(Person person);

        void onRemoveMember(Person person);
    }

    /**
     * Callback interface for handling user actions on manager items.
     */
    public interface ManagerActionHandler {
        void onResendManagerInvitation(Invitation invitation);

        void onCancelManagerInvitation(Invitation invitation);

        void onRevokeManagerAccess(ManagerItem managerItem);
    }

    /**
     * Callback interface for handling user actions on pending requests.
     */
    public interface PendingRequestActionHandler {
        void onApproveManagingAuthorizationRequest(Invitation invitation);

        void onDeclineManagingAuthorizationRequest(Invitation invitation);

        void onApproveMemberInvitation(Invitation invitation);

        void onDeclineMemberInvitation(Invitation invitation);
    }

    /**
     * Creates the UI for a single member item.
     * Handles different states: linked accounts, invitations, own accounts, no accounts.
     */
    public Node createMemberItem(MemberItem memberItem) {
        Person person = memberItem.getPerson();
        Invitation invitation = memberItem.getInvitation();
        MemberItem.MemberItemType type = memberItem.getType();

        HBox itemBox = new HBox(20);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setPadding(new Insets(16, 0, 16, 0));
        itemBox.getStyleClass().add("member-item-box");

        // Member info
        VBox infoBox = new VBox(8);

        // Name with badge
        HBox nameWithBadge = new HBox(12);
        nameWithBadge.setAlignment(Pos.CENTER_LEFT);

        // Display name: Use alias names from invitation if available, otherwise use person's full name
        String displayName;
        if (invitation != null && invitation.getAliasFirstName() != null && invitation.getAliasLastName() != null) {
            displayName = invitation.getAliasFirstName() + " " + invitation.getAliasLastName();
        } else {
            displayName = person != null && person.getFullName() != null ? person.getFullName() : "";
        }

        Label nameLabel = Bootstrap.strong(Bootstrap.h4(new Label(displayName)));
        nameWithBadge.getChildren().add(nameLabel);

        // Add badge based on type
        switch (type) {
            case AUTHORIZED_MEMBER:
                Label linkedBadge = new Label(I18n.getI18nText(MembersI18nKeys.BadgeAuthorized));
                ModalityStyle.authorizationBadgeActive(linkedBadge);
                nameWithBadge.getChildren().add(linkedBadge);
                break;

            case PENDING_OUTGOING_INVITATION:
            case PENDING_INCOMING_INVITATION:
                if (invitation != null) {
                    Label badge = createStatusBadge(invitation);
                    nameWithBadge.getChildren().add(badge);
                }
                break;

            case DIRECT_MEMBER:
                // No badge for direct members
                break;
        }

        // Display email and status message
        if (type == MemberItem.MemberItemType.AUTHORIZED_MEMBER) {
            // Linked account - show name with badge and linked account status
            Label linkedAccountLabel = Bootstrap.small(Bootstrap.textSecondary(
                I18nControls.newLabel(MembersI18nKeys.LinkedToTheirKBSAccount)));
            linkedAccountLabel.getStyleClass().add("text-italic");
            infoBox.getChildren().addAll(nameWithBadge, linkedAccountLabel);
        } else {
            // For all other cases, show email
            String email = person != null && person.getEmail() != null ? person.getEmail() : "";
            Label emailLabel = Bootstrap.textSecondary(new Label(email));
            emailLabel.getStyleClass().add("text-size-13");
            infoBox.getChildren().addAll(nameWithBadge, emailLabel);

            // Status messages for pending invitations
            if (type == MemberItem.MemberItemType.PENDING_OUTGOING_INVITATION) {
                Label statusLabel = Bootstrap.small(Bootstrap.textSecondary(
                    I18nControls.newLabel(MembersI18nKeys.WaitingForAcceptance)));
                statusLabel.getStyleClass().add("text-italic");
                infoBox.getChildren().add(statusLabel);
            } else if (type == MemberItem.MemberItemType.PENDING_INCOMING_INVITATION) {
                // Get the inviter's name (the person who wants to add you as a member)
                String inviterName = invitation != null && invitation.getInviter() != null
                        ? invitation.getInviter().getFullName()
                        : "";
                Label statusLabel = Bootstrap.small(Bootstrap.textSecondary(
                    new Label(I18n.getI18nText(MembersI18nKeys.WantsToAddYou, inviterName))));
                infoBox.getChildren().add(statusLabel);

                // Add action buttons below the status message for incoming invitations
                VBox authInfoBox = new VBox(8);
                authInfoBox.setPadding(new Insets(8, 0, 0, 0));

                HBox authActions = new HBox(12);
                authActions.setAlignment(Pos.CENTER_LEFT);

                Button approveButton = Bootstrap.successButton(
                    I18nControls.newButton(MembersI18nKeys.ApproveAction));
                approveButton.setPadding(new Insets(6, 16, 6, 16));
                approveButton.getStyleClass().add("action-button");
                approveButton.setCursor(Cursor.HAND);
                approveButton.setOnAction(e -> pendingRequestActionHandler.onApproveMemberInvitation(invitation));

                Button declineButton = Bootstrap.dangerButton(
                    I18nControls.newButton(MembersI18nKeys.DeclineAction));
                declineButton.setPadding(new Insets(6, 16, 6, 16));
                declineButton.getStyleClass().add("action-button");
                declineButton.setCursor(Cursor.HAND);
                declineButton.setOnAction(e -> pendingRequestActionHandler.onDeclineMemberInvitation(invitation));

                authActions.getChildren().addAll(approveButton, declineButton);
                authInfoBox.getChildren().add(authActions);
                infoBox.getChildren().add(authInfoBox);
            }
        }

        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // Actions based on member type (using clean switch statement)
        HBox actions = new HBox(18);
        actions.setAlignment(Pos.CENTER_RIGHT);

        switch (memberItem.getType()) {
            case DIRECT_MEMBER:
                // Direct member (no accountPerson) - can Edit and Remove
                Hyperlink editLink = Bootstrap.textPrimary(
                    I18nControls.newHyperlink(MembersI18nKeys.EditMemberDetails));
                editLink.setCursor(Cursor.HAND);
                editLink.setOnAction(e -> memberActionHandler.onEditMember(person));

                Hyperlink removeDirectLink = Bootstrap.textDanger(
                    I18nControls.newHyperlink(MembersI18nKeys.RemoveMember));
                removeDirectLink.setCursor(Cursor.HAND);
                removeDirectLink.setOnAction(e -> memberActionHandler.onRemoveMember(person));

                actions.getChildren().addAll(editLink, removeDirectLink);
                break;

            case AUTHORIZED_MEMBER:
                // Authorized member (has accountPerson) - can Edit local name and Remove
                Hyperlink editAuthorizedLink = Bootstrap.textPrimary(
                    I18nControls.newHyperlink(MembersI18nKeys.EditMemberDetails));
                editAuthorizedLink.setCursor(Cursor.HAND);
                editAuthorizedLink.setOnAction(e -> memberActionHandler.onEditMember(person));

                Hyperlink removeAuthorizedLink = Bootstrap.textDanger(
                    I18nControls.newHyperlink(MembersI18nKeys.RemoveMember));
                removeAuthorizedLink.setCursor(Cursor.HAND);
                removeAuthorizedLink.setOnAction(e -> memberActionHandler.onRemoveLinkedAccount(person));

                actions.getChildren().addAll(editAuthorizedLink, removeAuthorizedLink);
                break;

            case PENDING_OUTGOING_INVITATION:
                // Pending invitation where I'm the inviter - can Resend and Cancel
                Hyperlink resendLink = Bootstrap.textPrimary(
                    I18nControls.newHyperlink(MembersI18nKeys.Resend));
                resendLink.setCursor(Cursor.HAND);
                resendLink.setOnAction(e -> memberActionHandler.onResendInvitation(invitation));

                Hyperlink cancelLink = Bootstrap.textSecondary(
                    I18nControls.newHyperlink(MembersI18nKeys.CancelAction));
                cancelLink.setCursor(Cursor.HAND);
                cancelLink.setOnAction(e -> memberActionHandler.onCancelInvitation(invitation));

                actions.getChildren().addAll(resendLink, cancelLink);
                break;

            case PENDING_INCOMING_INVITATION:
                // Incoming invitation - buttons are now displayed below the name in infoBox
                // No actions needed on the right side
                break;
        }

        itemBox.getChildren().addAll(infoBox, actions);
        return itemBox;
    }

    /**
     * Creates the UI for a single manager item using the enum type system.
     * Handles all three cases: AUTHORIZED_MANAGER, PENDING_OUTGOING_INVITATION, PENDING_INCOMING_INVITATION
     */
    public Node createManagerItem(ManagerItem managerItem) {
        Invitation invitation = managerItem.getInvitation();
        ManagerItem.ManagerItemType type = managerItem.getType();

        HBox itemBox = new HBox(20);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setPadding(new Insets(16, 0, 16, 0));
        itemBox.getStyleClass().add("member-item-box");

        // Manager info
        VBox infoBox = new VBox(8);

        // Name with badge
        HBox nameWithBadge = new HBox(12);
        nameWithBadge.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = Bootstrap.strong(Bootstrap.h4(new Label(managerItem.getManagerName())));
        nameWithBadge.getChildren().add(nameLabel);

        // Add badge based on type
        switch (type) {
            case AUTHORIZED_MANAGER:
                // No badge needed for authorized managers
                break;

            case PENDING_OUTGOING_INVITATION:
                Label pendingBadge = new Label(I18n.getI18nText(MembersI18nKeys.BadgePending));
                ModalityStyle.authorizationBadgePending(pendingBadge);
                nameWithBadge.getChildren().add(pendingBadge);
                break;

            case PENDING_INCOMING_INVITATION:
                Label needsApprovalBadge = new Label(I18n.getI18nText(MembersI18nKeys.BadgeNeedsApproval));
                ModalityStyle.authorizationBadgePending(needsApprovalBadge);
                nameWithBadge.getChildren().add(needsApprovalBadge);
                break;
        }

        // Email
        String email = managerItem.getManagerEmail();
        if (email != null) {
            Label emailLabel = Bootstrap.textSecondary(new Label(email));
            emailLabel.getStyleClass().add("text-size-13");
            infoBox.getChildren().addAll(nameWithBadge, emailLabel);
        } else {
            infoBox.getChildren().add(nameWithBadge);
        }

        // Status message for pending invitations
        if (type == ManagerItem.ManagerItemType.PENDING_OUTGOING_INVITATION) {
            Label statusLabel = Bootstrap.small(Bootstrap.textSecondary(
                I18nControls.newLabel(MembersI18nKeys.WaitingForAcceptance)));
            statusLabel.getStyleClass().add("text-italic");
            infoBox.getChildren().add(statusLabel);
        } else if (type == ManagerItem.ManagerItemType.PENDING_INCOMING_INVITATION) {
            // Get the inviter's name (the person who wants to manage my bookings)
            String inviterName = invitation != null && invitation.getInviter() != null
                    ? invitation.getInviter().getFullName()
                    : managerItem.getManagerName();
            Label statusLabel = Bootstrap.small(Bootstrap.textSecondary(
                new Label(I18n.getI18nText(MembersI18nKeys.WantsToManageBookings, inviterName))));
            infoBox.getChildren().add(statusLabel);

            // Add action buttons below the status message for incoming invitations
            VBox authInfoBox = new VBox(8);
            authInfoBox.setPadding(new Insets(8, 0, 0, 0));

            HBox authActions = new HBox(12);
            authActions.setAlignment(Pos.CENTER_LEFT);

            Button approveButton = Bootstrap.successButton(
                I18nControls.newButton(MembersI18nKeys.ApproveAction));
            approveButton.setPadding(new Insets(6, 16, 6, 16));
            approveButton.getStyleClass().add("action-button");
            approveButton.setCursor(Cursor.HAND);
            approveButton.setOnAction(e -> pendingRequestActionHandler.onApproveManagingAuthorizationRequest(invitation));

            Button declineButton = Bootstrap.dangerButton(
                I18nControls.newButton(MembersI18nKeys.DeclineAction));
            declineButton.setPadding(new Insets(6, 16, 6, 16));
            declineButton.getStyleClass().add("action-button");
            declineButton.setCursor(Cursor.HAND);
            declineButton.setOnAction(e -> pendingRequestActionHandler.onDeclineManagingAuthorizationRequest(invitation));

            authActions.getChildren().addAll(approveButton, declineButton);
            authInfoBox.getChildren().add(authActions);
            infoBox.getChildren().add(authInfoBox);
        }

        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // Actions based on type (using clean switch statement)
        HBox actions = new HBox(18);
        actions.setAlignment(Pos.CENTER_RIGHT);

        switch (type) {
            case AUTHORIZED_MANAGER:
                // Authorized manager - can Revoke
                Hyperlink revokeLink = Bootstrap.textDanger(
                    I18nControls.newHyperlink(MembersI18nKeys.RevokeAccess));
                revokeLink.setCursor(Cursor.HAND);
                revokeLink.setOnAction(e -> managerActionHandler.onRevokeManagerAccess(managerItem));
                actions.getChildren().add(revokeLink);
                break;

            case PENDING_OUTGOING_INVITATION:
                // Outgoing invitation - can Resend and Cancel
                Hyperlink resendLink = Bootstrap.textPrimary(
                    I18nControls.newHyperlink(MembersI18nKeys.Resend));
                resendLink.setCursor(Cursor.HAND);
                resendLink.setOnAction(e -> managerActionHandler.onResendManagerInvitation(invitation));

                Hyperlink cancelLink = Bootstrap.textSecondary(
                    I18nControls.newHyperlink(MembersI18nKeys.CancelAction));
                cancelLink.setCursor(Cursor.HAND);
                cancelLink.setOnAction(e -> managerActionHandler.onCancelManagerInvitation(invitation));

                actions.getChildren().addAll(resendLink, cancelLink);
                break;

            case PENDING_INCOMING_INVITATION:
                // Incoming invitation - buttons are now displayed below the name in infoBox
                // No actions needed on the right side
                break;
        }

        itemBox.getChildren().addAll(infoBox, actions);
        return itemBox;
    }

    /**
     * Creates the UI for a pending authorization request item.
     * These are requests from people who want to manage my bookings.
     *
     * @deprecated Use createManagerItem(ManagerItem) with PENDING_INCOMING_INVITATION type instead
     */
    @Deprecated
    public Node createPendingAuthorizationItem(Invitation invitation) {
        HBox itemBox = new HBox(20);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setPadding(new Insets(16, 0, 16, 0));
        itemBox.getStyleClass().add("member-item-box");

        // Inviter info (person who wants to manage my bookings)
        VBox infoBox = new VBox(8);

        // Name with badge
        HBox nameWithBadge = new HBox(12);
        nameWithBadge.setAlignment(Pos.CENTER_LEFT);

        Person inviter = invitation.getInviter();
        Label nameLabel = Bootstrap.strong(Bootstrap.h4(new Label(inviter.getFullName())));

        Label badge = new Label(I18n.getI18nText(MembersI18nKeys.BadgeNeedsApproval));
        ModalityStyle.authorizationBadgePending(badge);

        nameWithBadge.getChildren().addAll(nameLabel, badge);

        Label emailLabel = Bootstrap.textSecondary(new Label(inviter.getEmail()));
        emailLabel.getStyleClass().add("text-size-13");

        infoBox.getChildren().addAll(nameWithBadge, emailLabel);

        // Authorization info section
        VBox authInfoBox = new VBox(8);
        authInfoBox.setPadding(new Insets(8, 0, 0, 0));

        // Descriptive text explaining what this request is
        Label authDetailLabel = Bootstrap.small(Bootstrap.textSecondary(
            I18nControls.newLabel(MembersI18nKeys.WantsToManageBookings)));
        authInfoBox.getChildren().add(authDetailLabel);

        // Action buttons
        HBox authActions = new HBox(12);
        authActions.setAlignment(Pos.CENTER_LEFT);

        Button approveButton = Bootstrap.successButton(
            I18nControls.newButton(MembersI18nKeys.ApproveAction));
        approveButton.setPadding(new Insets(6, 16, 6, 16));
        approveButton.getStyleClass().add("action-button");
        approveButton.setCursor(Cursor.HAND);
        approveButton.setOnAction(e -> pendingRequestActionHandler.onApproveManagingAuthorizationRequest(invitation));

        Button declineButton = Bootstrap.dangerButton(
            I18nControls.newButton(MembersI18nKeys.DeclineAction));
        declineButton.setPadding(new Insets(6, 16, 6, 16));
        declineButton.getStyleClass().add("action-button");
        declineButton.setCursor(Cursor.HAND);
        declineButton.setOnAction(e -> pendingRequestActionHandler.onDeclineManagingAuthorizationRequest(invitation));

        authActions.getChildren().addAll(approveButton, declineButton);
        authInfoBox.getChildren().add(authActions);

        infoBox.getChildren().add(authInfoBox);

        HBox.setHgrow(infoBox, Priority.ALWAYS);

        itemBox.getChildren().add(infoBox);
        return itemBox;
    }

    /**
     * Creates the UI for a pending outgoing manager invitation item.
     * These are invitations where I invited someone to manage MY bookings.
     * Display shows the invitee's name (the person I invited).
     *
     * @deprecated Use createManagerItem(ManagerItem) with PENDING_OUTGOING_INVITATION type instead
     */
    @Deprecated
    public Node createPendingMemberInvitationItem(Invitation invitation) {
        HBox itemBox = new HBox(20);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setPadding(new Insets(16, 0, 16, 0));
        itemBox.getStyleClass().add("member-item-box");

        // Invitee info (person I invited to manage my bookings)
        VBox infoBox = new VBox(8);

        // Name with badge
        HBox nameWithBadge = new HBox(12);
        nameWithBadge.setAlignment(Pos.CENTER_LEFT);

        // Show INVITEE's name (the person I invited), not inviter
        Person invitee = invitation.getInvitee();
        String displayName = invitee != null ? invitee.getFullName() :
            (invitation.getAliasFirstName() + " " + invitation.getAliasLastName());

        Label nameLabel = Bootstrap.strong(Bootstrap.h4(new Label(displayName)));

        Label badge = new Label(I18n.getI18nText(MembersI18nKeys.BadgeNeedsApproval));
        ModalityStyle.authorizationBadgePending(badge);

        nameWithBadge.getChildren().addAll(nameLabel, badge);

        String email = invitee != null ? invitee.getEmail() : null;
        if (email != null) {
            Label emailLabel = Bootstrap.textSecondary(new Label(email));
            emailLabel.getStyleClass().add("text-size-13");
            infoBox.getChildren().addAll(nameWithBadge, emailLabel);
        } else {
            infoBox.getChildren().add(nameWithBadge);
        }

        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // Actions - Only show Cancel for outgoing invitations (I'm the inviter)
        HBox actions = new HBox(18);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Hyperlink cancelLink = Bootstrap.textDanger(
            I18nControls.newHyperlink(MembersI18nKeys.CancelAction));
        cancelLink.setCursor(Cursor.HAND);
        cancelLink.setOnAction(e -> managerActionHandler.onCancelManagerInvitation(invitation));

        actions.getChildren().add(cancelLink);

        itemBox.getChildren().addAll(infoBox, actions);
        return itemBox;
    }

    /**
     * Creates a status badge for an invitation.
     */
    public Label createStatusBadge(Invitation invitation) {
        Label badge = new Label();

        if (invitation.isPending()) {
            badge.setText(I18n.getI18nText(MembersI18nKeys.BadgePending));
            ModalityStyle.authorizationBadgePending(badge);
        } else if (invitation.isAccepted()) {
            badge.setText(I18n.getI18nText(MembersI18nKeys.BadgeActive));
            ModalityStyle.authorizationBadgeActive(badge);
        } else {
            // Needs validation or other state
            badge.setText(I18n.getI18nText(MembersI18nKeys.BadgeNeedsValidation));
            ModalityStyle.authorizationBadgeNeedsValidation(badge);
        }

        return badge;
    }

    /**
     * Creates an empty state UI with icon, title, and description.
     */
    public VBox createEmptyState(SVGPath icon, Object titleKey, Object descriptionKey) {
        VBox emptyState = new VBox();
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(60, 0, 60, 0));
        emptyState.setMaxWidth(600);

        // Icon - 80x80 with opacity 0.3
        icon.setScaleX(3.33); // Scale to 80px (24px viewBox * 3.33)
        icon.setScaleY(3.33);
        icon.setOpacity(0.3);
        VBox.setMargin(icon, new Insets(0, 0, 24, 0));

        // Title - Poppins 600, 20px, #4a4748
        Label titleLabel = I18nControls.newLabel(titleKey);
        titleLabel.getStyleClass().addAll("empty-state-title");
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        VBox.setMargin(titleLabel, new Insets(0, 0, 12, 0));

        // Description - Poppins 400, 14px, #838788, line-height 1.6
        Label descLabel = I18nControls.newLabel(descriptionKey);
        descLabel.getStyleClass().add("empty-state-description");
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(TextAlignment.CENTER);
        descLabel.setMaxWidth(500);
        VBox.setMargin(descLabel, new Insets(0, 0, 24, 0));

        emptyState.getChildren().addAll(icon, titleLabel, descLabel);
        return emptyState;
    }

    /**
     * Creates a visual divider between sections.
     */
    public Node createSectionDivider() {
        Region divider = new Region();
        divider.getStyleClass().add("section-divider");
        divider.setPrefHeight(1);
        divider.setMaxWidth(600);
        VBox.setMargin(divider, new Insets(48, 0, 48, 0));
        return divider;
    }
}
