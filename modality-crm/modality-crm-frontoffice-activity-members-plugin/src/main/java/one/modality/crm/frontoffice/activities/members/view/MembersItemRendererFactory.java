package one.modality.crm.frontoffice.activities.members.view;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.responsive.ResponsiveDesign;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.beans.value.ObservableDoubleValue;
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
                                         PendingRequestActionHandler pendingRequestActionHandler,
                                         ObservableDoubleValue responsiveWidthProperty) {

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
     * Extended callback interface for member actions including validation requests.
     */
    public interface MemberActionHandlerWithValidation extends MemberActionHandler {
        void onSendValidationRequest(Person member, Person matchingAccount);
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
     * Helper method to setup responsive max width for name labels.
     * Mobile (<600px): 200px max width
     * Tablet (600-900px): 300px max width
     * Desktop (>900px): 400px max width
     */
    private void setupResponsiveNameLabel(Label nameLabel) {
        nameLabel.setWrapText(true);
        new ResponsiveDesign(responsiveWidthProperty)
                .addResponsiveLayout(width -> width < 600, () -> nameLabel.setMaxWidth(200))
                .addResponsiveLayout(width -> width >= 600 && width < 900, () -> nameLabel.setMaxWidth(300))
                .addResponsiveLayout(width -> width >= 900, () -> nameLabel.setMaxWidth(400))
                .start();
    }

    /**
     * Helper method to create a responsive action container that stacks links vertically on mobile.
     * Returns a VBox that switches between vertical and horizontal orientation based on screen size.
     */
    private VBox createResponsiveActionContainer() {
        VBox actions = new VBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);
        return actions;
    }

    /**
     * Sets up responsive behavior for action links.
     * On mobile: links (and optional badge) are stacked vertically in the VBox
     * On desktop: links are wrapped in an HBox for horizontal layout, badge stays in nameWithBadge
     */
    private void setupResponsiveActions(VBox actionsContainer, java.util.List<Node> actionLinks, Label badge, HBox nameWithBadge) {
        // Create the inner container that will hold the links
        HBox innerContainer = new HBox(18);
        innerContainer.setAlignment(Pos.CENTER_RIGHT);

        // Add all action links to the inner container
        innerContainer.getChildren().addAll(actionLinks);

        // Start with the inner container
        actionsContainer.getChildren().clear();
        actionsContainer.getChildren().add(innerContainer);

        // Make it responsive: change orientation and spacing
        new ResponsiveDesign(responsiveWidthProperty)
                .addResponsiveLayout(width -> width < 600, () -> {
                    // Mobile: vertical stacking - move badge to actionsContainer, then links
                    actionsContainer.getChildren().clear();
                    actionsContainer.setPadding(Insets.EMPTY);

                    // Set width based on badge size (~100px) but allow expansion for longer text
                    actionsContainer.setMinWidth(100);
                    actionsContainer.setPrefWidth(100);
                    actionsContainer.setMaxWidth(Double.MAX_VALUE);

                    if (badge != null && nameWithBadge != null) {
                        // Remove badge from nameWithBadge and add to actionsContainer
                        nameWithBadge.getChildren().remove(badge);
                        actionsContainer.getChildren().add(badge);
                    }
                    actionsContainer.getChildren().addAll(actionLinks);
                    actionsContainer.setSpacing(8);
                    actionsContainer.setAlignment(Pos.TOP_LEFT);
                })
                .addResponsiveLayout(width -> width >= 600, () -> {
                    // Desktop: horizontal layout - move links to innerContainer, badge back to nameWithBadge
                    actionsContainer.setPadding(Insets.EMPTY);

                    // Reset width constraints for desktop
                    actionsContainer.setMinWidth(Region.USE_COMPUTED_SIZE);
                    actionsContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);
                    actionsContainer.setMaxWidth(Double.MAX_VALUE);

                    if (badge != null && nameWithBadge != null) {
                        // Remove badge from actionsContainer and add back to nameWithBadge
                        actionsContainer.getChildren().remove(badge);
                        if (!nameWithBadge.getChildren().contains(badge)) {
                            nameWithBadge.getChildren().add(badge);
                        }
                    }
                    innerContainer.getChildren().clear();
                    innerContainer.getChildren().addAll(actionLinks);
                    innerContainer.setSpacing(18);
                    innerContainer.setAlignment(Pos.CENTER_RIGHT);
                    actionsContainer.getChildren().clear();
                    actionsContainer.getChildren().add(innerContainer);
                })
                .start();
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

        // Display name logic:
        // - For PENDING_INCOMING_INVITATION with inviterPayer=false: Use person's full name (inviter's name)
        //   because alias contains manager's name, but we want to show who invited us
        // - For other cases: Use alias names if available, otherwise use person's full name
        String displayName;
        if (type == MemberItem.MemberItemType.PENDING_INCOMING_INVITATION &&
            invitation != null && Boolean.FALSE.equals(invitation.isInviterPayer())) {
            // Manager invitation (inviterPayer=false) - show inviter's name, not alias
            displayName = person != null && person.getFullName() != null ? person.getFullName() : "";
        } else if (invitation != null && invitation.getAliasFirstName() != null && invitation.getAliasLastName() != null) {
            displayName = invitation.getAliasFirstName() + " " + invitation.getAliasLastName();
        } else {
            displayName = person != null && person.getFullName() != null ? person.getFullName() : "";
        }

        Label nameLabel = Bootstrap.strong(Bootstrap.h4(new Label(displayName)));
        setupResponsiveNameLabel(nameLabel);
        nameWithBadge.getChildren().add(nameLabel);

        // Add badge based on type (track it for responsive layout)
        Label statusBadge = null;
        switch (type) {
            case AUTHORIZED_MEMBER:
                statusBadge = new Label(I18n.getI18nText(MembersI18nKeys.BadgeAuthorized));
                ModalityStyle.authorizationBadgeActive(statusBadge);
                nameWithBadge.getChildren().add(statusBadge);
                break;

            case PENDING_OUTGOING_INVITATION:
            case PENDING_INCOMING_INVITATION:
                if (invitation != null) {
                    statusBadge = createStatusBadge(invitation);
                    nameWithBadge.getChildren().add(statusBadge);
                }
                break;

            case DIRECT_MEMBER:
                // Show "Needs Validation" badge if member has matching account
                if (memberItem.hasMatchingAccount()) {
                    statusBadge = new Label(I18n.getI18nText(MembersI18nKeys.BadgeNeedsValidation));
                    ModalityStyle.authorizationBadgeNeedsValidation(statusBadge);
                    nameWithBadge.getChildren().add(statusBadge);
                }
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
        VBox actionsContainer = createResponsiveActionContainer();
        java.util.List<Node> actionLinks = new java.util.ArrayList<>();

        switch (memberItem.getType()) {
            case DIRECT_MEMBER:
                // Direct member (no accountPerson) - can Edit and Remove
                // If matching account exists, show "Send validation request" instead of Edit
                if (memberItem.hasMatchingAccount()) {
                    // Member has created their own account - offer to send validation request
                    Hyperlink sendValidationLink = Bootstrap.textPrimary(
                        I18nControls.newHyperlink(MembersI18nKeys.SendRequest));
                    sendValidationLink.setCursor(Cursor.HAND);
                    sendValidationLink.setOnAction(e -> {
                        if (memberActionHandler instanceof MemberActionHandlerWithValidation) {
                            ((MemberActionHandlerWithValidation) memberActionHandler)
                                .onSendValidationRequest(person, memberItem.getMatchingAccountPerson());
                        }
                    });
                    actionLinks.add(sendValidationLink);
                } else {
                    // Normal direct member - show Edit link
                    Hyperlink editLink = Bootstrap.textPrimary(
                        I18nControls.newHyperlink(MembersI18nKeys.EditMemberDetails));
                    editLink.setCursor(Cursor.HAND);
                    editLink.setOnAction(e -> memberActionHandler.onEditMember(person));
                    actionLinks.add(editLink);
                }

                Hyperlink removeDirectLink = Bootstrap.textDanger(
                    I18nControls.newHyperlink(MembersI18nKeys.RemoveMember));
                removeDirectLink.setCursor(Cursor.HAND);
                removeDirectLink.setOnAction(e -> memberActionHandler.onRemoveMember(person));

                actionLinks.add(removeDirectLink);
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

                actionLinks.add(editAuthorizedLink);
                actionLinks.add(removeAuthorizedLink);
                break;

            case PENDING_OUTGOING_INVITATION:
                // Pending invitation where I'm the inviter - can Cancel
                Hyperlink cancelLink = Bootstrap.textSecondary(
                    I18nControls.newHyperlink(MembersI18nKeys.CancelAction));
                cancelLink.setCursor(Cursor.HAND);
                cancelLink.setOnAction(e -> memberActionHandler.onCancelInvitation(invitation));

                actionLinks.add(cancelLink);
                break;

            case PENDING_INCOMING_INVITATION:
                // Incoming invitation - buttons are now displayed below the name in infoBox
                // No actions needed on the right side
                break;
        }

        // Add the action links to the container, wrapping in HBox for desktop
        if (!actionLinks.isEmpty()) {
            setupResponsiveActions(actionsContainer, actionLinks, statusBadge, nameWithBadge);
        }

        itemBox.getChildren().addAll(infoBox, actionsContainer);
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
        setupResponsiveNameLabel(nameLabel);
        nameWithBadge.getChildren().add(nameLabel);

        // Add badge based on type (track it for responsive layout)
        Label statusBadge = null;
        switch (type) {
            case AUTHORIZED_MANAGER:
                // No badge needed for authorized managers
                break;

            case PENDING_OUTGOING_INVITATION:
                statusBadge = new Label(I18n.getI18nText(MembersI18nKeys.BadgePending));
                ModalityStyle.authorizationBadgePending(statusBadge);
                nameWithBadge.getChildren().add(statusBadge);
                break;

            case PENDING_INCOMING_INVITATION:
                statusBadge = new Label(I18n.getI18nText(MembersI18nKeys.BadgeNeedsApproval));
                ModalityStyle.authorizationBadgePending(statusBadge);
                nameWithBadge.getChildren().add(statusBadge);
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
        VBox actionsContainer = createResponsiveActionContainer();
        java.util.List<Node> actionLinks = new java.util.ArrayList<>();

        switch (type) {
            case AUTHORIZED_MANAGER:
                // Authorized manager - can Revoke
                Hyperlink revokeLink = Bootstrap.textDanger(
                    I18nControls.newHyperlink(MembersI18nKeys.RevokeAccess));
                revokeLink.setCursor(Cursor.HAND);
                revokeLink.setOnAction(e -> managerActionHandler.onRevokeManagerAccess(managerItem));
                actionLinks.add(revokeLink);
                break;

            case PENDING_OUTGOING_INVITATION:
                // Outgoing invitation - can Cancel
                Hyperlink cancelLink = Bootstrap.textSecondary(
                    I18nControls.newHyperlink(MembersI18nKeys.CancelAction));
                cancelLink.setCursor(Cursor.HAND);
                cancelLink.setOnAction(e -> managerActionHandler.onCancelManagerInvitation(invitation));

                actionLinks.add(cancelLink);
                break;

            case PENDING_INCOMING_INVITATION:
                // Incoming invitation - buttons are now displayed below the name in infoBox
                // No actions needed on the right side
                break;
        }

        // Add the action links to the container, wrapping in HBox for desktop
        if (!actionLinks.isEmpty()) {
            setupResponsiveActions(actionsContainer, actionLinks, statusBadge, nameWithBadge);
        }

        itemBox.getChildren().addAll(infoBox, actionsContainer);
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
