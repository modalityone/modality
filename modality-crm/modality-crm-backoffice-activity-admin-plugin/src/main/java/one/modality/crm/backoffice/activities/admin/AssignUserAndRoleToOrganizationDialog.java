package one.modality.crm.backoffice.activities.admin;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.domainmodel.functions.AbcNames;
import one.modality.base.shared.entities.AuthorizationOrganizationUserAccess;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.Person;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

import static one.modality.crm.backoffice.activities.admin.Admin18nKeys.*;

/**
 * Dialog for assigning roles to users within an organization.
 * Can be used both for new assignments and editing existing role assignments.
 *
 * @author Claude Code
 */
public class AssignUserAndRoleToOrganizationDialog {

    /**
     * Shows the assign role dialog.
     *
     * @param existingUserAccess Existing user access record to edit (null for new assignment)
     * @param buttonFactory      Button factory for creating dialog buttons
     * @param entityStore        The entity store for database operations
     */
    public static void show(Entity existingUserAccess, ButtonFactoryMixin buttonFactory, EntityStore entityStore) {
        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
        UpdateStore updateStore = UpdateStore.createAbove(entityStore);

        // Main dialog container
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30, 40, 30, 40));
        dialogContent.setMinWidth(500);
        dialogContent.setMaxWidth(600);

        // Header with title
        Label titleLabel = Bootstrap.strong(I18nControls.newLabel(AssignRoleDialogTitle));
        titleLabel.getStyleClass().addAll("admin-title", "dialog-title");

        // Organization info box
        VBox organizationBox = new VBox(4);
        organizationBox.getStyleClass().add("organization-info-box");

        Label organizationPrefix = I18nControls.newLabel(OrganizationLabel);
        organizationPrefix.getStyleClass().add("organization-info-prefix");

        Organization currentOrg = FXOrganization.getOrganization();
        String orgName = currentOrg != null ? currentOrg.getStringFieldValue("name") : "No organization selected";
        Label organizationName = Bootstrap.strong(new Label(orgName));
        organizationName.getStyleClass().add("organization-info-name");

        HBox orgNameRow = new HBox(8);
        orgNameRow.setAlignment(Pos.CENTER_LEFT);
        orgNameRow.getChildren().addAll(organizationPrefix, organizationName);

        organizationBox.getChildren().add(orgNameRow);
        organizationBox.setPadding(new Insets(12, 16, 12, 16));

        // Form fields
        VBox formFields = new VBox(16);

        // Button selector parameters
        ButtonSelectorParameters buttonSelectorParameters = new ButtonSelectorParameters()
            .setButtonFactory(buttonFactory)
            .setDialogParentGetter(FXMainFrameDialogArea::getDialogArea);

        // Select User field
        VBox userField = new VBox(8);
        Label userLabel = I18nControls.newLabel(SelectUser);
        userLabel.getStyleClass().add("form-label");

        EntityButtonSelector<Person> userSelector = createPersonButtonSelector(dataSourceModel);
        Button userButton = userSelector.getButton();
        userButton.setMaxWidth(Double.MAX_VALUE);

        // Pre-fill for editing
        if (existingUserAccess != null) {
            Entity user = existingUserAccess.getForeignEntity("user");
            if (user instanceof Person) {
                userSelector.setSelectedItem((Person) user);
            }
            userButton.setDisable(true); // Can't change user when editing
        }
        userField.getChildren().addAll(userLabel, userButton);

        // Select Role field
        VBox roleField = new VBox(8);
        Label roleLabel = I18nControls.newLabel(SelectRole);
        roleLabel.getStyleClass().add("form-label");

        EntityButtonSelector<Entity> roleSelector = createRoleButtonSelector(dataSourceModel, buttonSelectorParameters);
        Button roleButton = roleSelector.getButton();
        roleButton.setMaxWidth(Double.MAX_VALUE);

        // Pre-fill for editing
        if (existingUserAccess != null) {
            Entity role = existingUserAccess.getForeignEntity("role");
            if (role != null) {
                roleSelector.setSelectedItem(role);
            }
        }
        roleField.getChildren().addAll(roleLabel, roleButton);

        // Scope selection
        VBox scopeField = new VBox(8);
        Label scopeLabel = I18nControls.newLabel(Scope);
        scopeLabel.getStyleClass().add("form-label");
        Label scopeDesc = I18nControls.newLabel(ScopeDescription);
        scopeDesc.getStyleClass().add("form-description");

        // Scope cards
        HBox scopeCards = new HBox(12);
        ToggleGroup scopeToggleGroup = new ToggleGroup();

        // Entire Organization card
        RadioButton entireOrgRadio = new RadioButton();
        entireOrgRadio.setToggleGroup(scopeToggleGroup);
        entireOrgRadio.setSelected(true);
        VBox entireOrgCard = createScopeCard(
            entireOrgRadio,
            I18n.getI18nText(EntireOrganization),
            I18n.getI18nText(EntireOrganizationDescription),
            true
        );

        // Specific Event card
        RadioButton specificEventRadio = new RadioButton();
        specificEventRadio.setToggleGroup(scopeToggleGroup);
        VBox specificEventCard = createScopeCard(
            specificEventRadio,
            I18n.getI18nText(SpecificEvent),
            I18n.getI18nText(SpecificEventDescription),
            false
        );

        // Event selector (shown only when specific event is selected)
        EntityButtonSelector<Entity> eventSelector = createEventButtonSelector(dataSourceModel, buttonSelectorParameters, currentOrg);
        Button eventButton = eventSelector.getButton();
        eventButton.setMaxWidth(Double.MAX_VALUE);
        eventButton.setText(I18n.getI18nText(SelectEventPlaceholder));
        eventButton.setVisible(false);
        eventButton.setManaged(false);

        // Update button text when selection changes
        eventSelector.selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                eventButton.setText(newVal.getStringFieldValue("name"));
            } else {
                eventButton.setText(I18n.getI18nText(SelectEventPlaceholder));
            }
        });

        // Pre-fill for editing
        if (existingUserAccess != null) {
            Entity event = existingUserAccess.getForeignEntity("event");
            if (event != null) {
                eventSelector.setSelectedItem(event);
                specificEventRadio.setSelected(true);
                eventButton.setVisible(true);
                eventButton.setManaged(true);
                updateScopeCardStyles(entireOrgCard, false);
                updateScopeCardStyles(specificEventCard, true);
            }
        }

        // Toggle event selector visibility
        specificEventRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            eventButton.setVisible(newVal);
            eventButton.setManaged(newVal);
            updateScopeCardStyles(entireOrgCard, !newVal);
            updateScopeCardStyles(specificEventCard, newVal);
        });

        scopeCards.getChildren().addAll(entireOrgCard, specificEventCard);
        HBox.setHgrow(entireOrgCard, Priority.ALWAYS);
        HBox.setHgrow(specificEventCard, Priority.ALWAYS);

        scopeField.getChildren().addAll(scopeLabel, scopeDesc, scopeCards, eventButton);

        // Access Type selection
        VBox accessTypeField = new VBox(8);
        Label accessTypeLabel = I18nControls.newLabel(AccessTypeLabel);
        accessTypeLabel.getStyleClass().add("form-label");

        HBox accessTypeOptions = new HBox(16);
        ToggleGroup accessTypeGroup = new ToggleGroup();

        RadioButton readOnlyRadio = I18nControls.newRadioButton(ReadOnly);
        readOnlyRadio.setToggleGroup(accessTypeGroup);

        RadioButton readWriteRadio = I18nControls.newRadioButton(ReadAndWrite);
        readWriteRadio.setToggleGroup(accessTypeGroup);
        readWriteRadio.setSelected(true);

        // Pre-fill for editing
        if (existingUserAccess != null) {
            Boolean readOnly = existingUserAccess.getBooleanFieldValue("readOnly");
            if (readOnly != null && readOnly) {
                readOnlyRadio.setSelected(true);
            } else {
                readWriteRadio.setSelected(true);
            }
        }

        accessTypeOptions.getChildren().addAll(readOnlyRadio, readWriteRadio);
        accessTypeField.getChildren().addAll(accessTypeLabel, accessTypeOptions);

        // Summary info box
        Label summaryBox = Bootstrap.alertInfo(new Label());
        summaryBox.setWrapText(true);
        summaryBox.setMaxWidth(Double.MAX_VALUE);
        summaryBox.getStyleClass().add("dialog-summary");

        // Update summary dynamically
        Runnable updateSummary = () -> {
            String accessType = readWriteRadio.isSelected() ?
                I18n.getI18nText(ReadAndWrite) : I18n.getI18nText(ReadOnly);
            String role = roleSelector.getSelectedItem() != null ?
                roleSelector.getSelectedItem().getStringFieldValue("name") : "[Role]";
            String scope = entireOrgRadio.isSelected() ?
                I18n.getI18nText(EntireOrganization).toLowerCase() :
                (eventSelector.getSelectedItem() != null ? I18n.getI18nText(EventPrefix) + eventSelector.getSelectedItem().getStringFieldValue("name") : "[Event]");

            summaryBox.setText(I18n.getI18nText(AssignRoleSummaryText)
                .replace("{0}", accessType)
                .replace("{1}", role)
                .replace("{2}", scope));
        };

        roleSelector.selectedItemProperty().addListener((obs, oldVal, newVal) -> updateSummary.run());
        accessTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> updateSummary.run());
        scopeToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> updateSummary.run());
        eventSelector.selectedItemProperty().addListener((obs, oldVal, newVal) -> updateSummary.run());

        updateSummary.run();

        formFields.getChildren().addAll(userField, roleField, scopeField, accessTypeField, summaryBox);

        // Footer buttons
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = Bootstrap.button(I18nControls.newButton(Cancel));
        Button assignButton = Bootstrap.successButton(I18nControls.newButton(AssignRoleButton));

        // Disable save button until all required fields are filled
        BooleanBinding isInvalid = Bindings.createBooleanBinding(() -> {
                // User must be selected
                if (userSelector.getSelectedItem() == null) return true;
                // Role must be selected
                if (roleSelector.getSelectedItem() == null) return true;
                // If specific event is selected, event must be chosen
                return specificEventRadio.isSelected() && eventSelector.getSelectedItem() == null;
            }, userSelector.selectedItemProperty(), roleSelector.selectedItemProperty(),
            specificEventRadio.selectedProperty(), eventSelector.selectedItemProperty());

        assignButton.disableProperty().bind(isInvalid);

        footer.getChildren().addAll(cancelButton, assignButton);

        // Add all to dialog content
        dialogContent.getChildren().addAll(titleLabel, organizationBox, formFields, footer);

        // Show dialog
        BorderPane dialogPane = new BorderPane(dialogContent);
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());

        // Button actions
        cancelButton.setOnAction(e -> dialogCallback.closeDialog());
        assignButton.setOnAction(e -> {
            // Save role assignment to database (validation is handled by button binding)
            Person selectedUser = userSelector.getSelectedItem();
            Entity selectedRole = roleSelector.getSelectedItem();
            Entity selectedEvent = specificEventRadio.isSelected() ? eventSelector.getSelectedItem() : null;
            boolean readOnly = readOnlyRadio.isSelected();

            AuthorizationOrganizationUserAccess userAccess;
            if (existingUserAccess != null) {
                // Update existing record - must copy it to the updateStore to track changes
                if (existingUserAccess instanceof AuthorizationOrganizationUserAccess) {
                    userAccess = updateStore.updateEntity((AuthorizationOrganizationUserAccess) existingUserAccess);
                } else {
                    // Fallback - shouldn't happen
                    return;
                }
            } else {
                // Create new record
                userAccess = updateStore.insertEntity(AuthorizationOrganizationUserAccess.class);
                userAccess.setOrganization(currentOrg);
                userAccess.setUser(selectedUser);
            }

            // Set/update fields
            userAccess.setRole(selectedRole);
            userAccess.setEvent(selectedEvent);
            userAccess.setReadOnly(readOnly);

            // Submit changes
            updateStore.submitChanges()
                .onSuccess(result -> dialogCallback.closeDialog())
                .onFailure(error -> Platform.runLater(() -> showErrorDialog(error.getMessage())));
        });
    }

    /**
     * Creates an EntityButtonSelector for Person entities.
     */
    private static EntityButtonSelector<Person> createPersonButtonSelector(
        DataSourceModel dataSourceModel) {
        return new EntityButtonSelector<Person>( // language=JSON5
            "{class: 'Person', alias: 'p', columns: [{expression: '[firstName,lastName,`(` + email + `)`]'}], where: 'owner and !removed and frontendAccount.(backoffice and !disabled)', orderBy: 'firstName,lastName'}",
            new ButtonFactoryMixin() {
            }, FXMainFrameDialogArea::getDialogArea, dataSourceModel
        ) {
            @Override
            protected void setSearchParameters(String search, EntityStore store) {
                super.setSearchParameters(search, store);
                store.setParameterValue("abcSearchLike", AbcNames.evaluate(search, true));
            }
        }
            // Inline function doesn't work TODO: fix it
            //.setSearchCondition("searchMatchesPerson(p)")
            .setSearchCondition("abcNames(p..fullName) like ?abcSearchLike or lower(p..email) like ?searchEmailLike");
    }

    /**
     * Creates an EntityButtonSelector for authorization_role entities.
     */
    private static EntityButtonSelector<Entity> createRoleButtonSelector(
        DataSourceModel dataSourceModel,
        ButtonSelectorParameters buttonSelectorParameters
    ) {
        String roleJson = // language=JSON5
            "{class: 'AuthorizationRole', alias: 'r', columns: [{expression: 'name'}], orderBy: 'name'}";

        return new EntityButtonSelector<>(roleJson, dataSourceModel, buttonSelectorParameters);
    }

    /**
     * Creates an EntityButtonSelector for Event entities filtered by organization.
     */
    private static EntityButtonSelector<Entity> createEventButtonSelector(
        DataSourceModel dataSourceModel,
        ButtonSelectorParameters buttonSelectorParameters,
        Organization organization
    ) {
        Object orgId = organization != null ? organization.getPrimaryKey() : 1;
        String eventJson = // language=JSON5
            "{class: 'Event', alias: 'e', where: 'organization=" + orgId + "', orderBy: 'name', columns: 'name,startDate,endDate'}";

        return new EntityButtonSelector<>(eventJson, dataSourceModel, buttonSelectorParameters);
    }

    /**
     * Creates a scope card (Entire Organization or Specific Event).
     *
     * @param radio       RadioButton associated with this card
     * @param title       Card title
     * @param description Card description
     * @param selected    Whether this card is initially selected
     * @return The card VBox
     */
    private static VBox createScopeCard(RadioButton radio, String title, String description, boolean selected) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(16));
        card.getStyleClass().add("scope-card");
        card.setMaxWidth(Double.MAX_VALUE);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("scope-card-title");

        header.getChildren().addAll(radio, titleLabel);

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("scope-card-description");
        descLabel.setWrapText(true);

        card.getChildren().addAll(header, descLabel);

        // Click anywhere on card to select radio
        card.setOnMouseClicked(e -> radio.setSelected(true));

        updateScopeCardStyles(card, selected);

        return card;
    }

    /**
     * Updates the visual style of a scope card based on selection state.
     *
     * @param card     The card VBox to update
     * @param selected Whether the card is selected
     */
    private static void updateScopeCardStyles(VBox card, boolean selected) {
        if (selected) {
            if (!card.getStyleClass().contains("selected")) {
                card.getStyleClass().add("selected");
            }
        } else {
            card.getStyleClass().remove("selected");
        }
    }

    private static void showErrorDialog(String content) {
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setMinWidth(350);
        dialogContent.setPrefWidth(500);
        dialogContent.setMaxWidth(700);

        Label titleLabel = Bootstrap.strong(I18nControls.newLabel(Error));
        titleLabel.getStyleClass().add("error-dialog-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        Label headerLabel = I18nControls.newLabel(FailedToSaveRole);
        headerLabel.setWrapText(true);
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        headerLabel.getStyleClass().add("error-dialog-header");

        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(Double.MAX_VALUE);
        contentLabel.getStyleClass().add("error-dialog-content");

        dialogContent.getChildren().addAll(titleLabel, headerLabel, contentLabel);

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button okButton = Bootstrap.dangerButton(I18nControls.newButton(OK));

        footer.getChildren().add(okButton);
        dialogContent.getChildren().add(footer);

        BorderPane dialogPane = new BorderPane(dialogContent);
        dialogPane.getStyleClass().add("modal-dialog-pane");
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());

        okButton.setOnAction(e -> dialogCallback.closeDialog());
    }
}
