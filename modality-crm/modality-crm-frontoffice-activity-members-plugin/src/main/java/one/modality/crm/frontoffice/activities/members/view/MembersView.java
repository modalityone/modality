package one.modality.crm.frontoffice.activities.members.view;

import dev.webfx.extras.controlfactory.MaterialFactoryMixin;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.styles.materialdesign.util.MaterialUtil;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.time.pickers.DateField;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.builder.DialogBuilderUtil;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelector;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.client.time.FrontOfficeTimeFormats;
import one.modality.base.shared.entities.Country;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.Person;
import one.modality.crm.client.i18n.CrmI18nKeys;
import one.modality.crm.frontoffice.activities.createaccount.UserAccountUI;
import one.modality.crm.frontoffice.activities.members.MembersI18nKeys;
import one.modality.crm.frontoffice.activities.members.model.MemberItem;
import one.modality.crm.frontoffice.activities.members.model.MembersModel;
import one.modality.crm.frontoffice.help.HelpPanel;

import java.util.function.Consumer;

import static one.modality.crm.frontoffice.activities.members.MembersCssSelectors.section_title_uppercase;

/**
 * View class for the Members activity.
 * Handles all UI construction and rendering.
 *
 * @author David Hello
 * @author Bruno Salmon
 */
public class MembersView implements MaterialFactoryMixin, ModalityButtonFactoryMixin {

    // UI dimension constants
    private static final int DIALOG_SCROLL_PANE_MAX_HEIGHT = 500;
    private static final int DIALOG_PREFERRED_WIDTH = 700;

    private final MembersModel model;
    private MembersItemRendererFactory rendererFactory;

    // UI components
    private final MonoPane mainContent = new MonoPane();
    private final VBox membersSection = new VBox(10);
    private final VBox managersSection = new VBox(10);
    private final VBox messageContainer = new VBox(8);

    // Action handlers (set by the activity/controller)
    private Runnable onAddMemberRequested;
    private Runnable onInviteManagerRequested;

    public MembersView(MembersModel model) {
        this.model = model;
    }

    /**
     * Set the renderer factory (called after construction).
     */
    public void setRendererFactory(MembersItemRendererFactory rendererFactory) {
        this.rendererFactory = rendererFactory;
    }

    /**
     * Get the responsive width property for the main content area.
     */
    public javafx.beans.value.ObservableDoubleValue getResponsiveWidthProperty() {
        return mainContent.widthProperty();
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
        // Build all sections
        buildMembersSection();
        buildManagersSection();

        // Message container styling
        messageContainer.setMaxWidth(800);
        messageContainer.setAlignment(Pos.TOP_CENTER);

        // Main container
        VBox container = new VBox(24,
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
     * Create a warning alert for members with matching accounts that need validation.
     */
    private VBox createMatchingAccountsWarning() {
        VBox warningBox = new VBox(8);
        warningBox.setMaxWidth(800);
        warningBox.setPadding(new Insets(16));
        warningBox.getStyleClass().addAll("alert", "alert-warning");

        Label warningMessage = new Label();
        warningMessage.setWrapText(true);

        // Update warning visibility based on directMembersList
        Runnable updateWarning = () -> {
            long count = model.getDirectMembersList().stream()
                    .filter(MemberItem::hasMatchingAccount)
                    .count();

            if (count == 0) {
                warningBox.setVisible(false);
                warningBox.setManaged(false);
            } else {
                warningBox.setVisible(true);
                warningBox.setManaged(true);

                String message = count == 1
                    ? "⚠ One of your members has created a KBS account. You can link this account to enable them to manage their own bookings."
                    : "⚠ " + count + " of your members have created KBS accounts. You can link these accounts to enable them to manage their own bookings.";
                warningMessage.setText(message);
            }
        };

        // Listen to direct members list changes
        ObservableLists.runNowAndOnListChange(change -> updateWarning.run(), model.getDirectMembersList());

        warningBox.getChildren().add(warningMessage);
        return warningBox;
    }

    /**
     * Build the "Members I Can Book For" section.
     */
    private void buildMembersSection() {
        // Section title (same style as "WHO CAN BOOK FOR ME")
        Label sectionTitle = Bootstrap.textPrimary(Bootstrap.strong(
                I18nControls.newLabel(MembersI18nKeys.MembersICanBookFor)));
        sectionTitle.getStyleClass().add(section_title_uppercase);
        sectionTitle.setTextAlignment(TextAlignment.CENTER);
        sectionTitle.setStyle("-fx-font-size: 18px;");
        sectionTitle.setWrapText(true);

        // Description
        Label description = Bootstrap.textSecondary(
                I18nControls.newLabel(MembersI18nKeys.MembersICanBookForDescription));
        description.setWrapText(true);
        description.setTextAlignment(TextAlignment.CENTER);
        description.setPadding(new Insets(10, 0, 20, 0));

        // Warning alert for members with matching accounts
        VBox warningAlert = createMatchingAccountsWarning();

        // Loading indicator
        Region loadingSpinner = Controls.createSectionSizeSpinner();

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

            Layouts.setManagedAndVisibleProperties(loadingSpinner, isLoading);

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
                warningAlert,
                loadingSpinner,
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
        // Section divider with uppercase label
        Node divider = rendererFactory.createSectionDivider();

        Label dividerLabel = Bootstrap.textPrimary(Bootstrap.strong(
                I18nControls.newLabel(MembersI18nKeys.WhoCanBookForMe)));
        dividerLabel.getStyleClass().add(section_title_uppercase);
        dividerLabel.setTextAlignment(TextAlignment.CENTER);
        dividerLabel.setStyle("-fx-font-size: 18px;");

        // Section title (same style as first section)
        Label sectionTitle = Bootstrap.strong(Bootstrap.h3(
                I18nControls.newLabel(MembersI18nKeys.PeopleManagingMyBookings)));
        sectionTitle.setTextAlignment(TextAlignment.CENTER);
        sectionTitle.setWrapText(true);
        sectionTitle.setPadding(new Insets(20, 0, 0, 0));

        // Description
        Label description = Bootstrap.textSecondary(
                I18nControls.newLabel(MembersI18nKeys.PeopleManagingMyBookingsDescription));
        description.setWrapText(true);
        description.setTextAlignment(TextAlignment.CENTER);
        description.setPadding(new Insets(10, 0, 20, 0));

        // Loading indicator
        Region loadingSpinner = Controls.createSectionSizeSpinner();

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

            Layouts.setManagedAndVisibleProperties(loadingSpinner, isLoading);

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
                dividerLabel,
                sectionTitle,
                description,
                loadingSpinner,
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
        public record AddMemberData(String firstName, String lastName, String email, java.time.LocalDate birthDate) {
    }

    public void showAddMemberDialog(Consumer<AddMemberData> onDataEntered) {
        // Create form content container
        VBox formContent = new VBox(16);
        formContent.setPadding(new Insets(20, 0, 20, 0));

        // Create MaterialDesign form fields
        TextField firstNameField = newMaterialTextField(MembersI18nKeys.FirstName);
        MaterialUtil.getMaterialTextField(firstNameField).setAnimateLabel(false);

        TextField lastNameField = newMaterialTextField(MembersI18nKeys.LastName);
        MaterialUtil.getMaterialTextField(lastNameField).setAnimateLabel(false);

        TextField emailField = newMaterialTextField(MembersI18nKeys.AddMemberEmailLabel);
        MaterialUtil.getMaterialTextField(emailField).setAnimateLabel(false);

        // Birthdate field (optional text field with format validation)
        TextField birthDateField = new TextField();
        birthDateField.setPromptText(FrontOfficeTimeFormats.BIRTH_DATE_FORMAT);
        MaterialUtil.makeMaterial(birthDateField);
        dev.webfx.extras.styles.materialdesign.textfield.MaterialTextField materialBirthDateField = MaterialUtil.getMaterialTextField(birthDateField);
        materialBirthDateField.setAnimateLabel(false);
        I18n.bindI18nTextProperty(materialBirthDateField.labelTextProperty(), CrmI18nKeys.BirthDate);

        // Warning box for birthdate
        VBox birthdateWarning = new VBox(8);
        birthdateWarning.setPadding(new Insets(12));
        birthdateWarning.getStyleClass().addAll("alert", "alert-warning");
        Label birthdateWarningLabel = I18nControls.newLabel(MembersI18nKeys.BirthdateWarning);
        birthdateWarningLabel.setWrapText(true);
        birthdateWarning.getChildren().add(birthdateWarningLabel);

        // Create validation support
        ValidationSupport validationSupport = new ValidationSupport();
        validationSupport.addRequiredInput(firstNameField);
        validationSupport.addRequiredInput(lastNameField);
        // Email is required and must be valid format
        validationSupport.addRequiredInput(emailField);
        validationSupport.addEmailValidation(emailField, emailField, I18n.i18nTextProperty(MembersI18nKeys.ValidationError));
        // Birthdate is optional but must be valid format if entered
        validationSupport.addDateOrEmptyValidation(
                birthDateField,
                LocalizedTime.dateTimeFormatter(FrontOfficeTimeFormats.BIRTH_DATE_FORMAT),
                birthDateField,
                I18n.i18nTextProperty(MembersI18nKeys.InvalidBirthdateFormat));

        // Add fields to form content
        formContent.getChildren().addAll(
                firstNameField,
                lastNameField,
                emailField,
                birthDateField,
                birthdateWarning);

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
            // Validate using ValidationSupport
            if (!validationSupport.isValid()) {
                return;
            }

            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            // Parse birthdate if entered (already validated by ValidationSupport)
            java.time.LocalDate birthDate = null;
            String birthDateText = birthDateField.getText().trim();
            if (!birthDateText.isEmpty()) {
                birthDate = LocalizedTime.parseLocalDate(birthDateText, FrontOfficeTimeFormats.BIRTH_DATE_FORMAT);
            }

            onDataEntered.accept(new AddMemberData(firstName, lastName, email, birthDate));
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

        TextField emailField = newMaterialTextField(MembersI18nKeys.InviteManagerEmailLabel);
        MaterialUtil.getMaterialTextField(emailField).setAnimateLabel(false);

        // Create validation support
        ValidationSupport validationSupport = new ValidationSupport();
        validationSupport.addRequiredInput(emailField);
        validationSupport.addEmailValidation(emailField, emailField, I18n.i18nTextProperty(MembersI18nKeys.ValidationError));

        // Create form content
        VBox formContent = new VBox(16,
                descLabel,
                emailField);
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
            // Validate using ValidationSupport
            if (!validationSupport.isValid()) {
                return;
            }

            String email = emailField.getText().trim();
            onEmailEntered.accept(email);
            dialogCallback.closeDialog();
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

        // Create MaterialDesign form fields
        TextField firstNameField = newMaterialTextField(MembersI18nKeys.FirstName);
        MaterialUtil.getMaterialTextField(firstNameField).setAnimateLabel(false);
        firstNameField.setText(person.getFirstName());

        TextField lastNameField = newMaterialTextField(MembersI18nKeys.LastName);
        MaterialUtil.getMaterialTextField(lastNameField).setAnimateLabel(false);
        lastNameField.setText(person.getLastName());

        // Email field - only show for direct members (no accountPerson)
        TextField emailField = null;
        if (!isAuthorizedMember) {
            emailField = newMaterialTextField(MembersI18nKeys.Email);
            MaterialUtil.getMaterialTextField(emailField).setAnimateLabel(false);
            emailField.setText(person.getEmail());
        }

        // Create validation support
        ValidationSupport validationSupport = new ValidationSupport();
        validationSupport.addRequiredInput(firstNameField);
        validationSupport.addRequiredInput(lastNameField);
        // Email is optional for direct members

        // Create form content
        VBox formContent = new VBox(16);
        formContent.setPadding(new Insets(20, 0, 20, 0));
        formContent.getChildren().addAll(
                firstNameField,
                lastNameField
        );
        if (!isAuthorizedMember) {
            formContent.getChildren().add(emailField);
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
            // Validate using ValidationSupport
            if (!validationSupport.isValid()) {
                return;
            }

            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = finalEmailField != null ? finalEmailField.getText().trim() : null;

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
     * Show comprehensive edit dialog for direct members with all fields.
     */
    public void showEditDirectMemberDialog(Person person, DataSourceModel dataSourceModel, Runnable onSuccess) {
        // Create UpdateStore early so we can bind to hasChanges
        EntityStore entityStore = EntityStore.create(dataSourceModel);
        UpdateStore updateStore = UpdateStore.createAbove(entityStore);
        Person personToUpdate = updateStore.updateEntity(person);

        // Check if person has ever made a booking (name fields only editable if neverBooked)
        boolean canEditName = Boolean.TRUE.equals(person.isNeverBooked());

        // Create MaterialDesign form fields - Basic Info
        TextField firstNameField = newMaterialTextField(CrmI18nKeys.FirstName);
        MaterialUtil.getMaterialTextField(firstNameField).setAnimateLabel(false);
        firstNameField.setText(person.getFirstName());
        firstNameField.setEditable(canEditName);
        firstNameField.setDisable(!canEditName);

        TextField lastNameField = newMaterialTextField(CrmI18nKeys.LastName);
        MaterialUtil.getMaterialTextField(lastNameField).setAnimateLabel(false);
        lastNameField.setText(person.getLastName());
        lastNameField.setEditable(canEditName);
        lastNameField.setDisable(!canEditName);

        TextField emailField = newMaterialTextField(CrmI18nKeys.Email);
        MaterialUtil.getMaterialTextField(emailField).setAnimateLabel(false);
        emailField.setText(person.getEmail());

        // Create form content container first (needed for DateField)
        VBox formContent = new VBox(16);
        formContent.setPadding(new Insets(20));
        formContent.setMaxWidth(700);

        // Personal Details - DateField needs a container
        DateField birthDateField = new DateField(formContent);
        birthDateField.dateTimeFormatterProperty().bind(LocalizedTime.dateFormatterProperty(FrontOfficeTimeFormats.BIRTH_DATE_FORMAT));
        birthDateField.setDate(person.getBirthDate());
        TextField birthDateTextField = birthDateField.getTextField();
        birthDateTextField.setPromptText(FrontOfficeTimeFormats.BIRTH_DATE_FORMAT);
        MaterialUtil.makeMaterial(birthDateTextField);
        dev.webfx.extras.styles.materialdesign.textfield.MaterialTextField materialBirthDateField = MaterialUtil.getMaterialTextField(birthDateTextField);
        materialBirthDateField.setAnimateLabel(false);
        I18n.bindI18nTextProperty(materialBirthDateField.labelTextProperty(), CrmI18nKeys.BirthDate);

        // Gender radio buttons
        ToggleGroup genderGroup = new ToggleGroup();
        RadioButton optionMale = new RadioButton();
        I18nControls.bindI18nProperties(optionMale, CrmI18nKeys.Male);
        optionMale.setToggleGroup(genderGroup);
        optionMale.setSelected(Boolean.TRUE.equals(person.isMale()));

        RadioButton optionFemale = new RadioButton();
        I18nControls.bindI18nProperties(optionFemale, CrmI18nKeys.Female);
        optionFemale.setToggleGroup(genderGroup);
        optionFemale.setSelected(Boolean.FALSE.equals(person.isMale()));

        HBox genderBox = new HBox(20, optionMale, optionFemale);

        // Ordained/Lay radio buttons
        ToggleGroup ordainedGroup = new ToggleGroup();
        RadioButton optionOrdained = new RadioButton();
        I18nControls.bindI18nProperties(optionOrdained, CrmI18nKeys.Ordained);
        optionOrdained.setToggleGroup(ordainedGroup);
        optionOrdained.setSelected(Boolean.TRUE.equals(person.isOrdained()));

        RadioButton optionLay = new RadioButton();
        I18nControls.bindI18nProperties(optionLay, CrmI18nKeys.Lay);
        optionLay.setToggleGroup(ordainedGroup);
        optionLay.setSelected(Boolean.FALSE.equals(person.isOrdained()));

        HBox ordainedBox = new HBox(20, optionOrdained, optionLay);

        TextField layNameField = newMaterialTextField(CrmI18nKeys.LayName);
        MaterialUtil.getMaterialTextField(layNameField).setAnimateLabel(false);
        layNameField.setText(person.getLayName());
        layNameField.setVisible(Boolean.TRUE.equals(person.isOrdained()));
        layNameField.setManaged(Boolean.TRUE.equals(person.isOrdained()));
        optionOrdained.selectedProperty().addListener((obs, old, isOrdained) -> {
            layNameField.setVisible(isOrdained);
            layNameField.setManaged(isOrdained);
        });

        TextField phoneField = newMaterialTextField(CrmI18nKeys.Phone);
        MaterialUtil.getMaterialTextField(phoneField).setAnimateLabel(false);
        phoneField.setText(person.getPhone());

        // Address
        TextField streetField = newMaterialTextField(CrmI18nKeys.Street);
        MaterialUtil.getMaterialTextField(streetField).setAnimateLabel(false);
        streetField.setText(person.getStreet());

        TextField postCodeField = newMaterialTextField(CrmI18nKeys.Postcode);
        MaterialUtil.getMaterialTextField(postCodeField).setAnimateLabel(false);
        postCodeField.setText(person.getPostCode());

        TextField cityNameField = newMaterialTextField(CrmI18nKeys.City);
        MaterialUtil.getMaterialTextField(cityNameField).setAnimateLabel(false);
        cityNameField.setText(person.getCityName());

        ButtonSelectorParameters buttonSelectorParameters = new ButtonSelectorParameters()
                .setButtonFactory(this)
                .setDialogParentGetter(FXMainFrameDialogArea::getDialogArea);
        EntityButtonSelector<Country> countrySelector = UserAccountUI.createCountryButtonSelector(dataSourceModel, buttonSelectorParameters);
        countrySelector.setSelectedItem(person.getCountry());
        dev.webfx.extras.styles.materialdesign.textfield.MaterialTextFieldPane countryButton = countrySelector.toMaterialButton(CrmI18nKeys.Country);
        countryButton.getMaterialTextField().setAnimateLabel(false);

        // Organization
        ButtonSelector<Organization> organizationSelector = UserAccountUI.createOrganizationButtonSelector(dataSourceModel, buttonSelectorParameters);
        organizationSelector.setSelectedItem(person.getOrganization());
        dev.webfx.extras.styles.materialdesign.textfield.MaterialTextFieldPane organizationButton = organizationSelector.toMaterialButton(CrmI18nKeys.Centre);
        organizationButton.getMaterialTextField().setAnimateLabel(false);

        // Create validation support
        ValidationSupport validationSupport = new ValidationSupport();
        validationSupport.addRequiredInput(firstNameField);
        validationSupport.addRequiredInput(lastNameField);
        validationSupport.addOptionalEmailValidation(emailField, emailField, I18n.i18nTextProperty(MembersI18nKeys.ValidationError));

        // Sync UI changes to entity model
        FXProperties.runOnPropertiesChange(() -> {
            if (canEditName) {
                personToUpdate.setFirstName(firstNameField.getText());
                personToUpdate.setLastName(lastNameField.getText());
            }
            String email = emailField.getText();
            personToUpdate.setEmail(email != null && !email.trim().isEmpty() ? email.trim() : null);

            personToUpdate.setBirthDate(birthDateField.getDate());
            personToUpdate.setMale(optionMale.isSelected());
            personToUpdate.setOrdained(optionOrdained.isSelected());

            String layName = layNameField.getText();
            personToUpdate.setLayName(layName != null && !layName.trim().isEmpty() ? layName.trim() : null);

            String phone = phoneField.getText();
            personToUpdate.setPhone(phone != null && !phone.trim().isEmpty() ? phone.trim() : null);

            String street = streetField.getText();
            personToUpdate.setStreet(street != null && !street.trim().isEmpty() ? street.trim() : null);

            String postCode = postCodeField.getText();
            personToUpdate.setPostCode(postCode != null && !postCode.trim().isEmpty() ? postCode.trim() : null);

            String cityName = cityNameField.getText();
            personToUpdate.setCityName(cityName != null && !cityName.trim().isEmpty() ? cityName.trim() : null);

            personToUpdate.setCountry(countrySelector.getSelectedItem());
            personToUpdate.setOrganization(organizationSelector.getSelectedItem());
        },
        firstNameField.textProperty(),
        lastNameField.textProperty(),
        emailField.textProperty(),
        birthDateField.dateProperty(),
        optionMale.selectedProperty(),
        optionFemale.selectedProperty(),
        optionOrdained.selectedProperty(),
        optionLay.selectedProperty(),
        layNameField.textProperty(),
        phoneField.textProperty(),
        streetField.textProperty(),
        postCodeField.textProperty(),
        cityNameField.textProperty(),
        countrySelector.selectedItemProperty(),
        organizationSelector.selectedItemProperty()
        );

        // Add all fields to form content
        formContent.getChildren().addAll(
                firstNameField,
                lastNameField,
                emailField,
                birthDateField.getView(),
                genderBox,
                ordainedBox,
                layNameField,
                phoneField,
                streetField,
                postCodeField,
                cityNameField,
                countryButton,
                organizationButton
        );

        // Wrap form in ScrollPane for long content
        ScrollPane scrollPane = new ScrollPane(formContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(DIALOG_SCROLL_PANE_MAX_HEIGHT);
        scrollPane.setPadding(new Insets(0));
        scrollPane.setPrefWidth(DIALOG_PREFERRED_WIDTH);
        scrollPane.setMaxWidth(DIALOG_PREFERRED_WIDTH);

        // Create dialog without Bootstrap helper (no info message parameter)
        DialogContent dialog = new DialogContent();
        dialog.setHeaderText(I18n.getI18nText(MembersI18nKeys.UpdateMember));
        dialog.setContent(scrollPane);
        dialog.setCancelSave();

        // Bind save button disable property to inverse of updateStore.hasChanges
        Button saveButton = dialog.getPrimaryButton();
        if (saveButton != null) {
            saveButton.disableProperty().bind(dev.webfx.stack.orm.entity.binding.EntityBindings.hasChangesProperty(updateStore).not());
        }

        DialogBuilderUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());
        DialogBuilderUtil.armDialogContentButtons(dialog, dialogCallback -> {
            // Validate using ValidationSupport
            if (!validationSupport.isValid()) {
                return;
            }

            // Apply all field values (UpdateStore already created at top of method)
            String firstName = firstNameField.getText();
            personToUpdate.setFirstName(firstName != null ? firstName.trim() : "");

            String lastName = lastNameField.getText();
            personToUpdate.setLastName(lastName != null ? lastName.trim() : "");

            String email = emailField.getText();
            email = email != null ? email.trim() : "";
            personToUpdate.setEmail(email.isEmpty() ? null : email);

            personToUpdate.setBirthDate(birthDateField.getDate());
            personToUpdate.setMale(optionMale.isSelected());
            personToUpdate.setOrdained(optionOrdained.isSelected());

            String layName = layNameField.getText();
            layName = layName != null ? layName.trim() : "";
            personToUpdate.setLayName(layName.isEmpty() ? null : layName);

            String phone = phoneField.getText();
            phone = phone != null ? phone.trim() : "";
            personToUpdate.setPhone(phone.isEmpty() ? null : phone);

            String street = streetField.getText();
            street = street != null ? street.trim() : "";
            personToUpdate.setStreet(street.isEmpty() ? null : street);

            String postCode = postCodeField.getText();
            postCode = postCode != null ? postCode.trim() : "";
            personToUpdate.setPostCode(postCode.isEmpty() ? null : postCode);

            String cityName = cityNameField.getText();
            cityName = cityName != null ? cityName.trim() : "";
            personToUpdate.setCityName(cityName.isEmpty() ? null : cityName);

            personToUpdate.setCountry(countrySelector.getSelectedItem());
            personToUpdate.setOrganization(organizationSelector.getSelectedItem());

            // Submit changes
            updateStore.submitChanges()
                    .onFailure(error -> {
                        showErrorDialog(I18n.getI18nText(MembersI18nKeys.Error),
                                I18n.getI18nText(MembersI18nKeys.FailedToUpdateMember));
                    })
                    .onSuccess(result -> {
                        // Update the original person object with new values so UI reflects changes
                        person.setFirstName(personToUpdate.getFirstName());
                        person.setLastName(personToUpdate.getLastName());
                        person.setEmail(personToUpdate.getEmail());

                        UiScheduler.scheduleDeferred(() -> {
                            dialogCallback.closeDialog();
                            showSuccessMessage(I18n.getI18nText(MembersI18nKeys.MemberUpdatedSuccessfully));

                            if (onSuccess != null) {
                                onSuccess.run();
                            }
                        });
                    });
        });
    }

    /**
     * Data class for simple member updates (used by simple edit dialog).
     */
    public record MemberUpdateData(String firstName, String lastName, String email) {
    }
}
