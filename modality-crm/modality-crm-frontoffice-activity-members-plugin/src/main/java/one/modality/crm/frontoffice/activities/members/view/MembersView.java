package one.modality.crm.frontoffice.activities.members.view;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.builder.DialogBuilderUtil;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Person;
import one.modality.crm.frontoffice.activities.members.MembersI18nKeys;
import one.modality.crm.frontoffice.activities.members.model.MembersModel;
import one.modality.crm.frontoffice.help.HelpPanel;

import java.util.function.Consumer;

/**
 * View class for the Members activity.
 * Handles all UI construction and rendering.
 *
 * @author David Hello
 * @author Bruno Salmon
 */
public class MembersView {

    private final MembersModel model;
    private final MembersItemRendererFactory rendererFactory;

    // UI components
    private final MonoPane mainContent = new MonoPane();
    private final VBox membersSection = new VBox(10);
    private final VBox managersSection = new VBox(10);
    private final VBox messageContainer = new VBox(8);

    // Action handlers (set by the activity/controller)
    private Runnable onAddMemberRequested;
    private Runnable onInviteManagerRequested;

    public MembersView(MembersModel model, MembersItemRendererFactory rendererFactory) {
        this.model = model;
        this.rendererFactory = rendererFactory;
    }

    /**
     * Set the handler for when "Add Member" button is clicked.
     */
    public void setOnAddMemberRequested(Runnable handler) {
        this.onAddMemberRequested = handler;
    }

    /**
     * Set the handler for when "Invite Manager" button is clicked.
     */
    public void setOnInviteManagerRequested(Runnable handler) {
        this.onInviteManagerRequested = handler;
    }

    /**
     * Build the complete UI for the Members activity.
     */
    public Node buildUi() {
        // Main title
        Label titleLabel = Bootstrap.textPrimary(Bootstrap.strong(Bootstrap.h2(
                I18nControls.newLabel(MembersI18nKeys.PageTitle))));
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setPadding(new Insets(100, 0, 48, 0));

        // Build all sections
        buildMembersSection();
        buildManagersSection();

        // Message container styling
        messageContainer.setMaxWidth(800);
        messageContainer.setAlignment(Pos.TOP_CENTER);

        // Main container
        VBox container = new VBox(24,
                titleLabel,
                messageContainer,
                membersSection,
                managersSection,
                HelpPanel.createEmailHelpPanel(MembersI18nKeys.NeedHelpTitle, "kbs@kadampa.net")
        );
        container.setMaxWidth(800);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(0, 20, 50, 20));

        mainContent.setContent(container);
        return mainContent;
    }

    /**
     * Build the "Members I Can Book For" section.
     */
    private void buildMembersSection() {
        // Section title
        Label sectionTitle = Bootstrap.strong(Bootstrap.h3(
                I18nControls.newLabel(MembersI18nKeys.MembersICanBookFor)));
        sectionTitle.setTextAlignment(TextAlignment.CENTER);

        // Description
        Label description = Bootstrap.textSecondary(
                I18nControls.newLabel(MembersI18nKeys.MembersICanBookForDescription));
        description.setWrapText(true);
        description.setTextAlignment(TextAlignment.CENTER);
        description.setPadding(new Insets(10, 0, 20, 0));

        // Loading indicator
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(60, 60);
        VBox loadingBox = new VBox(loadingIndicator);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(60, 0, 60, 0));

        // Unified members list container - combines the three separate lists
        VBox membersListBox = new VBox(0);
        membersListBox.setAlignment(Pos.CENTER);

        // Create a combined observable list that updates when any of the three source lists change
        javafx.collections.ObservableList<one.modality.crm.frontoffice.activities.members.model.MemberItem> combinedMembersList =
                javafx.collections.FXCollections.observableArrayList();

        Runnable updateCombinedList = () -> {
            combinedMembersList.clear();
            combinedMembersList.addAll(model.getDirectMembersList());
            combinedMembersList.addAll(model.getAuthorizedMembersList());
            combinedMembersList.addAll(model.getPendingMemberInvitationsList());
        };

        // Listen to all three source lists and update combined list
        ObservableLists.runNowAndOnListChange(change -> updateCombinedList.run(), model.getDirectMembersList());
        ObservableLists.runOnListChange(change -> updateCombinedList.run(), model.getAuthorizedMembersList());
        ObservableLists.runOnListChange(change -> updateCombinedList.run(), model.getPendingMemberInvitationsList());

        ObservableLists.bindConverted(membersListBox.getChildren(),
                combinedMembersList,
                rendererFactory::createMemberItem);

        // Empty state
        VBox emptyState = rendererFactory.createEmptyState(
                SvgIcons.createUsersEmptyIcon(),
                MembersI18nKeys.NoMembersEmptyState,
                MembersI18nKeys.NoMembersEmptyStateDescription
        );

        // Visibility logic: show loading, or empty state, or members list
        Runnable updateVisibility = () -> {
            boolean isLoading = model.loadingMembersProperty().get();
            // Check if all three member lists are empty
            boolean isMembersEmpty = model.getDirectMembersList().isEmpty()
                    && model.getAuthorizedMembersList().isEmpty()
                    && model.getPendingMemberInvitationsList().isEmpty();

            loadingBox.setVisible(isLoading);
            loadingBox.setManaged(isLoading);

            if (!isLoading) {
                emptyState.setVisible(isMembersEmpty);
                emptyState.setManaged(isMembersEmpty);

                membersListBox.setVisible(!isMembersEmpty);
                membersListBox.setManaged(!isMembersEmpty);
            } else {
                emptyState.setVisible(false);
                emptyState.setManaged(false);
                membersListBox.setVisible(false);
                membersListBox.setManaged(false);
            }
        };

        model.loadingMembersProperty().addListener((obs, old, val) -> updateVisibility.run());
        // Listen to all three member lists
        ObservableLists.runNowAndOnListChange(change -> updateVisibility.run(), model.getDirectMembersList());
        ObservableLists.runNowAndOnListChange(change -> updateVisibility.run(), model.getAuthorizedMembersList());
        ObservableLists.runNowAndOnListChange(change -> updateVisibility.run(), model.getPendingMemberInvitationsList());

        // Add Member button
        Button addButton = Bootstrap.largePrimaryButton(
                I18nControls.newButton(MembersI18nKeys.AddMember), false);
        addButton.setOnAction(e -> {
            if (onAddMemberRequested != null) {
                onAddMemberRequested.run();
            }
        });

        membersSection.getChildren().setAll(
                sectionTitle,
                description,
                loadingBox,
                emptyState,
                membersListBox,
                addButton
        );
        membersSection.setAlignment(Pos.CENTER);
    }

    /**
     * Build the "Who Can Book For Me" section.
     */
    private void buildManagersSection() {
        // Section title
        Label sectionTitle = Bootstrap.strong(Bootstrap.h4(
                I18nControls.newLabel(MembersI18nKeys.WhoCanBookForMe)));
        sectionTitle.getStyleClass().add("section-title-uppercase");
        sectionTitle.setTextAlignment(TextAlignment.CENTER);

        // Description
        Label description = Bootstrap.textSecondary(
                I18nControls.newLabel(MembersI18nKeys.PeopleManagingMyBookingsDescription));
        description.setWrapText(true);
        description.setTextAlignment(TextAlignment.CENTER);
        description.setPadding(new Insets(10, 0, 20, 0));

        // Loading indicator
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(60, 60);
        VBox loadingBox = new VBox(loadingIndicator);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(60, 0, 60, 0));

        // Pending incoming requests (people asking to manage my bookings) - now using ManagerItem
        VBox pendingIncomingList = new VBox(0);
        pendingIncomingList.setAlignment(Pos.CENTER);
        ObservableLists.bindConverted(pendingIncomingList.getChildren(),
                model.getPendingIncomingManagerInvitationsList(),
                rendererFactory::createManagerItem);

        // Pending outgoing invitations (I invited someone to manage my bookings) - now using ManagerItem
        VBox pendingOutgoingList = new VBox(0);
        pendingOutgoingList.setAlignment(Pos.CENTER);
        ObservableLists.bindConverted(pendingOutgoingList.getChildren(),
                model.getPendingOutgoingManagerInvitationsList(),
                rendererFactory::createManagerItem);

        // Active managers list - using ManagerItem
        VBox managersListBox = new VBox(0);
        managersListBox.setAlignment(Pos.CENTER);
        ObservableLists.bindConverted(managersListBox.getChildren(),
                model.getAuthorizedManagersList(),
                rendererFactory::createManagerItem);

        // Empty state
        VBox emptyState = rendererFactory.createEmptyState(
                SvgIcons.createUsersEmptyIcon(),
                MembersI18nKeys.NoManagersEmptyState,
                MembersI18nKeys.NoManagersEmptyStateDescription
        );

        // Visibility logic
        Runnable updateVisibility = () -> {
            boolean isLoading = model.loadingManagersProperty().get();
            // Check if all three manager lists are empty
            boolean isEmpty = model.getAuthorizedManagersList().isEmpty()
                    && model.getPendingIncomingManagerInvitationsList().isEmpty()
                    && model.getPendingOutgoingManagerInvitationsList().isEmpty();

            loadingBox.setVisible(isLoading);
            loadingBox.setManaged(isLoading);

            if (!isLoading) {
                emptyState.setVisible(isEmpty);
                emptyState.setManaged(isEmpty);

                pendingIncomingList.setVisible(!model.getPendingIncomingManagerInvitationsList().isEmpty());
                pendingIncomingList.setManaged(!model.getPendingIncomingManagerInvitationsList().isEmpty());

                pendingOutgoingList.setVisible(!model.getPendingOutgoingManagerInvitationsList().isEmpty());
                pendingOutgoingList.setManaged(!model.getPendingOutgoingManagerInvitationsList().isEmpty());

                managersListBox.setVisible(!model.getAuthorizedManagersList().isEmpty());
                managersListBox.setManaged(!model.getAuthorizedManagersList().isEmpty());
            } else {
                emptyState.setVisible(false);
                emptyState.setManaged(false);
                pendingIncomingList.setVisible(false);
                pendingIncomingList.setManaged(false);
                pendingOutgoingList.setVisible(false);
                pendingOutgoingList.setManaged(false);
                managersListBox.setVisible(false);
                managersListBox.setManaged(false);
            }
        };

        model.loadingManagersProperty().addListener((obs, old, val) -> updateVisibility.run());
        // Listen to all three manager lists
        ObservableLists.runNowAndOnListChange(change -> updateVisibility.run(), model.getAuthorizedManagersList());
        ObservableLists.runNowAndOnListChange(change -> updateVisibility.run(), model.getPendingIncomingManagerInvitationsList());
        ObservableLists.runNowAndOnListChange(change -> updateVisibility.run(), model.getPendingOutgoingManagerInvitationsList());

        // Section divider
        Node divider = rendererFactory.createSectionDivider();

        // Invite Manager button
        Button inviteButton = Bootstrap.largePrimaryButton(
                I18nControls.newButton(MembersI18nKeys.InviteBookingManager), false);
        inviteButton.setOnAction(e -> {
            if (onInviteManagerRequested != null) {
                onInviteManagerRequested.run();
            }
        });

        managersSection.getChildren().setAll(
                divider,
                sectionTitle,
                description,
                loadingBox,
                emptyState,
                pendingIncomingList,
                pendingOutgoingList,
                managersListBox,
                inviteButton
        );
        managersSection.setAlignment(Pos.CENTER);
    }

    // ========== Dialog Creation Methods ==========

    /**
         * Data class to hold member information from Add Member dialog.
         */
        public record AddMemberData(String firstName, String lastName, String email) {
    }

    public void showAddMemberDialog(Consumer<AddMemberData> onDataEntered) {
        // Create form fields
        Label firstNameLabel = I18nControls.newLabel(MembersI18nKeys.FirstName);
        TextField firstNameField = new TextField();
        firstNameField.setPromptText(I18n.getI18nText(MembersI18nKeys.FirstNamePlaceholder));

        Label lastNameLabel = I18nControls.newLabel(MembersI18nKeys.LastName);
        TextField lastNameField = new TextField();
        lastNameField.setPromptText(I18n.getI18nText(MembersI18nKeys.LastNamePlaceholder));

        Label emailLabel = I18nControls.newLabel(MembersI18nKeys.AddMemberEmailLabel);
        TextField emailField = new TextField();
        emailField.setPromptText(I18n.getI18nText(MembersI18nKeys.AddMemberEmailPlaceholder));

        // Create form content
        VBox formContent = new VBox(16,
                firstNameLabel, firstNameField,
                lastNameLabel, lastNameField,
                emailLabel, emailField);
        formContent.setPadding(new Insets(20, 0, 20, 0));

        // Create dialog using factory method
        DialogContent dialog = DialogContent.createInformationDialogWithTwoActions(
                I18n.getI18nText(MembersI18nKeys.AddMemberModalTitle),
                I18n.getI18nText(MembersI18nKeys.AddMemberModalTitle),
                "",
                I18n.getI18nText(MembersI18nKeys.AddMemberInfoMessage)
        );

        // Add form fields to the dialog
        dialog.setContent(formContent)
                .setCancelConfirm();  // Use Cancel/Confirm instead of Yes/No

        DialogBuilderUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());
        DialogBuilderUtil.armDialogContentButtons(dialog, dialogCallback -> {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();

            // Validate first name and last name are not empty
            if (firstName.isEmpty() || lastName.isEmpty()) {
                showErrorDialog(
                    I18n.getI18nText(MembersI18nKeys.ValidationError),
                    I18n.getI18nText(MembersI18nKeys.FirstLastNameRequired)
                );
                return;
            }

            onDataEntered.accept(new AddMemberData(firstName, lastName, email));
            dialogCallback.closeDialog();
        });
    }

    /**
     * Show a dialog informing the user that the email was not found.
     */
    public void showEmailNotFoundDialog(String email) {
        DialogContent dialog = DialogContent.createInformationDialog(
                I18n.getI18nText(MembersI18nKeys.EmailNotFoundTitle),
                I18n.getI18nText(MembersI18nKeys.EmailNotFoundTitle),
                I18n.getI18nText(MembersI18nKeys.EmailNotFoundMessage, email)
        );
        DialogBuilderUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());
        DialogBuilderUtil.armDialogContentButtons(dialog, DialogCallback::closeDialog);
    }

    /**
     * Show a message informing the user that authorization request was sent.
     */
    public void showAuthorizationSentDialog(String memberName) {
        showSuccessMessage(I18n.getI18nText(MembersI18nKeys.AuthorizationSentMessage, memberName));
    }

    /**
     * Show the "Invite Manager" dialog and return the email entered by the user.
     */
    public void showInviteManagerDialog(Consumer<String> onEmailEntered) {
        // Create form fields
        Label descLabel = I18nControls.newLabel(MembersI18nKeys.InviteManagerDescription);
        descLabel.setWrapText(true);

        Label emailLabel = I18nControls.newLabel(MembersI18nKeys.InviteManagerEmailLabel);
        TextField emailField = new TextField();
        emailField.setPromptText(I18n.getI18nText(MembersI18nKeys.InviteManagerEmailPlaceholder));

        // Create form content
        VBox formContent = new VBox(16,
                descLabel,
                emailLabel, emailField);
        formContent.setPadding(new Insets(20, 0, 20, 0));

        // Create dialog using factory method
        DialogContent dialog = DialogContent.createInformationDialogWithTwoActions(
                I18n.getI18nText(MembersI18nKeys.InviteManagerModalTitle),
                I18n.getI18nText(MembersI18nKeys.InviteManagerModalTitle),
                "",
                I18n.getI18nText(MembersI18nKeys.InviteManagerPermissions)
        );

        // Add form fields to the dialog
        dialog.setContent(formContent)
                .setCancelConfirm();  // Use Cancel/Confirm instead of Yes/No

        DialogBuilderUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());
        DialogBuilderUtil.armDialogContentButtons(dialog, dialogCallback -> {
            String email = emailField.getText().trim();
            if (!email.isEmpty()) {
                onEmailEntered.accept(email);
                dialogCallback.closeDialog();
            }
        });
    }

    /**
     * Show a generic error dialog.
     */
    public void showErrorDialog(String title, String message) {
        DialogContent dialog = DialogContent.createErrorDialog(
                title,
                title,
                message
        );
        DialogBuilderUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());
        DialogBuilderUtil.armDialogContentButtons(dialog, DialogCallback::closeDialog);
    }

    /**
     * Show a dialog for editing a member's details.
     */
    public void showEditMemberDialog(Person person, Consumer<MemberUpdateData> onUpdate) {
        // Check if this is an authorized member (has accountPerson)
        boolean isAuthorizedMember = person.getAccountPerson() != null;

        // Create form fields
        Label firstNameLabel = I18nControls.newLabel(MembersI18nKeys.FirstName);
        TextField firstNameField = new TextField(person.getFirstName());
        firstNameField.setPromptText(I18n.getI18nText(MembersI18nKeys.FirstNamePlaceholder));

        Label lastNameLabel = I18nControls.newLabel(MembersI18nKeys.LastName);
        TextField lastNameField = new TextField(person.getLastName());
        lastNameField.setPromptText(I18n.getI18nText(MembersI18nKeys.LastNamePlaceholder));

        // Email field - only show for direct members (no accountPerson)
        Label emailLabel = null;
        TextField emailField = null;
        if (!isAuthorizedMember) {
            emailLabel = I18nControls.newLabel(MembersI18nKeys.Email);
            emailField = new TextField(person.getEmail());
            emailField.setPromptText(I18n.getI18nText(MembersI18nKeys.EmailPlaceholder));
        }

        // Create form content
        VBox formContent = new VBox(16);
        formContent.setPadding(new Insets(20, 0, 20, 0));
        formContent.getChildren().addAll(
                firstNameLabel, firstNameField,
                lastNameLabel, lastNameField
        );
        if (!isAuthorizedMember) {
            formContent.getChildren().addAll(emailLabel, emailField);
        }

        // Create dialog using factory method - different info message for authorized members
        DialogContent dialog = DialogContent.createInformationDialogWithTwoActions(
                I18n.getI18nText(MembersI18nKeys.EditMemberTitle),
                I18n.getI18nText(MembersI18nKeys.EditMemberTitle),
                "",
                I18n.getI18nText(isAuthorizedMember ? MembersI18nKeys.EditAuthorizedMemberInfoMessage : MembersI18nKeys.EditMemberInfoMessage)
        );

        // Add form fields to the dialog
        dialog.setContent(formContent)
                .setCancelSave();

        DialogBuilderUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());

        // Capture email field reference for button callback
        TextField finalEmailField = emailField;
        DialogBuilderUtil.armDialogContentButtons(dialog, dialogCallback -> {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = finalEmailField != null ? finalEmailField.getText().trim() : null;

            // Validate
            if (firstName.isEmpty() || lastName.isEmpty()) {
                showErrorDialog(I18n.getI18nText(MembersI18nKeys.ValidationError),
                        I18n.getI18nText(MembersI18nKeys.FirstLastNameRequired));
                return;
            }

            MemberUpdateData updateData = new MemberUpdateData(firstName, lastName, email != null && !email.isEmpty() ? email : null);
            onUpdate.accept(updateData);
            dialogCallback.closeDialog();
        });
    }

    // ========== Message Display Methods ==========

    /**
     * Show a success message with auto-hide after 8 seconds with fade out animation.
     */
    public void showSuccessMessage(String message) {
        messageContainer.getChildren().clear();
        Label successLabel = new Label(message);
        successLabel.setWrapText(true);
        successLabel.setMaxWidth(Double.MAX_VALUE);
        Bootstrap.alertSuccess(successLabel);
        messageContainer.getChildren().add(successLabel);

        // Start fully visible
        successLabel.setOpacity(1.0);

        // Wait for 8 seconds, then fade out over 0.5 seconds, then clear
        PauseTransition pause = new PauseTransition(Duration.seconds(8));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), successLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> messageContainer.getChildren().clear());

        SequentialTransition sequence = new SequentialTransition(pause, fadeOut);
        sequence.play();
    }

    // ========== Data Classes ==========

    /**
     * Show comprehensive edit dialog for direct members using UserProfileView.
     */
    public void showEditDirectMemberDialog(Person person, DataSourceModel dataSourceModel, Runnable onSuccess) {
        // Create UpdateStore for editing
        EntityStore entityStore = EntityStore.create(dataSourceModel);
        UpdateStore updateStore = UpdateStore.createAbove(entityStore);
        Person personToEdit = updateStore.updateEntity(person);

        // Create UserProfileView with appropriate sections visible
        // Note: ChangePictureUI is package-private, so we pass null (no picture editing in this dialog)
        one.modality.crm.frontoffice.activities.userprofile.UserProfileView userProfileView =
                new one.modality.crm.frontoffice.activities.userprofile.UserProfileView(
                        null,  // changePictureUI - no picture editing for members
                        false,  // showTitle - no title in dialog
                        false,  // showProfileHeader - no profile picture in dialog
                        true,   // showName - show first/last name
                        false,  // showEmail - don't show email change (they can edit in simple field)
                        false,  // showPassword - no password for members
                        true,   // showPersonalDetails - birth date, gender, ordained/lay, phone
                        true,   // showAddress - street, city, postcode, country
                        true,   // showKadampaCenter - organization selector
                        true,   // showSaveChangesButton
                        true,   // showCancelButton
                        personToEdit
                );

        VBox dialogContent = userProfileView.buildView();
        userProfileView.syncUIFromModel();

        // Create dialog
        DialogContent dialog = new DialogContent();
        dialog.setTitle(I18n.getI18nText(MembersI18nKeys.EditMemberDetails));
        dialog.setContent(dialogContent);

        // Show dialog
        DialogCallback dialogCallback = DialogBuilderUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());

        // Wire up Save button
        userProfileView.saveButton.setOnAction(e -> updateStore.submitChanges()
                .onFailure(error -> {
                    Console.log("Error updating member: " + error);
                    showErrorDialog(I18n.getI18nText(MembersI18nKeys.Error),
                            I18n.getI18nText(MembersI18nKeys.FailedToUpdateMember));
                })
                .onSuccess(result -> {
                    Console.log("Member updated successfully");
                    dialogCallback.closeDialog();
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                }));

        // Wire up Cancel button
        userProfileView.cancelButton.setOnAction(e -> dialogCallback.closeDialog());
    }

    /**
         * Data class for member updates.
         */
        public record MemberUpdateData(String firstName, String lastName, String email) {
    }
}
