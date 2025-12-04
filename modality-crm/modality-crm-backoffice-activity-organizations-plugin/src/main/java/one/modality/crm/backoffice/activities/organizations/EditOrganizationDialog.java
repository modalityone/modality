package one.modality.crm.backoffice.activities.organizations;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import one.modality.base.backoffice.claude.FormField;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Country;
import one.modality.base.shared.entities.Language;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.OrganizationType;

import static one.modality.base.backoffice.claude.FormFieldHelper.createTextField;
import static one.modality.crm.backoffice.activities.organizations.OrganizationsI18nKeys.*;
import static one.modality.base.client.i18n.BaseI18nKeys.*;

/**
 * Dialog for creating and editing organizations.
 *
 * @author Claude Code
 */
final class EditOrganizationDialog {

    /**
     * Shows the create/edit organization dialog.
     *
     * @param organization Existing organization to edit (null for new organization)
     * @param onSuccess Callback to execute after successful save
     */
    public static void show(Organization organization, Runnable onSuccess) {
        boolean isEdit = organization != null;

        // Main dialog container with scroll
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(24));
        dialogContent.setMinWidth(500);
        dialogContent.setPrefWidth(560);
        dialogContent.setMaxWidth(700);

        // Header with title
        Object titleKey = isEdit ? EditOrganizationTitle : CreateOrganizationTitle;
        Label titleLabel = Bootstrap.strong(I18nControls.newLabel(titleKey));
        titleLabel.getStyleClass().add("modal-title");

        Object subtitleKey = isEdit ? EditOrganizationSubtitle : CreateOrganizationSubtitle;
        Label subtitleLabel = I18nControls.newLabel(subtitleKey);
        subtitleLabel.getStyleClass().add("modal-subtitle");

        VBox headerBox = new VBox(4);
        headerBox.getChildren().addAll(titleLabel, subtitleLabel);

        // Form fields container
        VBox formFields = new VBox(20);
        formFields.setMaxWidth(Double.MAX_VALUE);

        // Get the data source model - use directly for selectors to ensure proper data loading
        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();

        // Create entity store
        EntityStore store = organization != null ? organization.getStore() : EntityStore.create(dataSourceModel);
        UpdateStore updateStore = UpdateStore.createAbove(store);
        Organization orgToSave = organization != null ? updateStore.updateEntity(organization) : updateStore.insertEntity(Organization.class);

        // ===== IDENTITY SECTION =====
        Label identitySection = createSectionLabel(OrganizationIdentitySection);

        // Organization Name field (required)
        FormField<TextField> nameFormField = createTextField(
            OrganizationNameLabel,
            OrganizationNamePlaceholder,
            null
        );
        TextField nameInput = nameFormField.inputField();
        if (isEdit) {
            nameInput.setText(organization.getName());
        }

        // Type selector (required)
        VBox typeField = new VBox(8);
        typeField.setMaxWidth(Double.MAX_VALUE);

        Label typeLabel = I18nControls.newLabel(OrganizationTypeLabel);
        typeLabel.getStyleClass().add("form-field-label");

        ButtonSelectorParameters typeSelectorParams = new ButtonSelectorParameters()
            .setButtonFactory(new ButtonFactoryMixin() {})
            .setDialogParentGetter(FXMainFrameDialogArea::getDialogArea);
        EntityButtonSelector<OrganizationType> typeSelector = new EntityButtonSelector<>(
            "{class: 'OrganizationType', alias: 'ot', columns: [{expression: 'name'}], orderBy: 'name'}",
            dataSourceModel,
            typeSelectorParams
        );
        Button typeButton = typeSelector.getButton();
        typeButton.setMaxWidth(Double.MAX_VALUE);

        if (isEdit && organization.getType() != null) {
            typeSelector.setSelectedItem(organization.getType());
        }

        typeField.getChildren().addAll(typeLabel, typeButton);

        // ===== LOCATION SECTION =====
        Label locationSection = createSectionLabel(OrganizationLocationSection);

        // Country and City in a row
        HBox locationRow1 = new HBox(12);
        locationRow1.setMaxWidth(Double.MAX_VALUE);

        // Country selector
        VBox countryField = new VBox(8);
        countryField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(countryField, Priority.ALWAYS);

        Label countryLabel = I18nControls.newLabel(OrganizationCountryLabel);
        countryLabel.getStyleClass().add("form-field-label");

        ButtonSelectorParameters countrySelectorParams = new ButtonSelectorParameters()
            .setButtonFactory(new ButtonFactoryMixin() {})
            .setDialogParentGetter(FXMainFrameDialogArea::getDialogArea);
        EntityButtonSelector<Country> countrySelector = new EntityButtonSelector<>(
            "{class: 'Country', alias: 'c', columns: [{expression: 'name'}], orderBy: 'name'}",
            dataSourceModel,
            countrySelectorParams
        );
        Button countryButton = countrySelector.getButton();
        countryButton.setMaxWidth(Double.MAX_VALUE);

        if (isEdit && organization.getCountry() != null) {
            countrySelector.setSelectedItem(organization.getCountry());
        }

        countryField.getChildren().addAll(countryLabel, countryButton);

        // City field
        FormField<TextField> cityFormField = createTextField(
            OrganizationCityLabel,
            OrganizationCityPlaceholder,
            null
        );
        TextField cityInput = cityFormField.inputField();
        VBox cityFieldContainer = cityFormField.container();
        HBox.setHgrow(cityFieldContainer, Priority.ALWAYS);
        if (isEdit) {
            String cityName = organization.getStringFieldValue("cityName");
            if (cityName != null) cityInput.setText(cityName);
        }

        locationRow1.getChildren().addAll(countryField, cityFieldContainer);

        // Street field
        FormField<TextField> streetFormField = createTextField(
            OrganizationStreetLabel,
            OrganizationStreetPlaceholder,
            null
        );
        TextField streetInput = streetFormField.inputField();
        if (isEdit) {
            String street = organization.getStringFieldValue("street");
            if (street != null) streetInput.setText(street);
        }

        // Post Code and Timezone in a row
        HBox locationRow2 = new HBox(12);
        locationRow2.setMaxWidth(Double.MAX_VALUE);

        // Post Code field
        FormField<TextField> postCodeFormField = createTextField(
            OrganizationPostCodeLabel,
            OrganizationPostCodePlaceholder,
            null
        );
        TextField postCodeInput = postCodeFormField.inputField();
        VBox postCodeFieldContainer = postCodeFormField.container();
        HBox.setHgrow(postCodeFieldContainer, Priority.ALWAYS);
        if (isEdit) {
            String postCode = organization.getStringFieldValue("postCode");
            if (postCode != null) postCodeInput.setText(postCode);
        }

        // Timezone field
        FormField<TextField> timezoneFormField = createTextField(
            OrganizationTimezoneLabel,
            OrganizationTimezonePlaceholder,
            null
        );
        TextField timezoneInput = timezoneFormField.inputField();
        VBox timezoneFieldContainer = timezoneFormField.container();
        HBox.setHgrow(timezoneFieldContainer, Priority.ALWAYS);
        if (isEdit) {
            String timezone = organization.getStringFieldValue("timezone");
            if (timezone != null) timezoneInput.setText(timezone);
        }

        locationRow2.getChildren().addAll(postCodeFieldContainer, timezoneFieldContainer);

        // ===== CONTACT SECTION =====
        Label contactSection = createSectionLabel(OrganizationContactSection);

        // Email and Phone in a row
        HBox contactRow = new HBox(12);
        contactRow.setMaxWidth(Double.MAX_VALUE);

        // Email field
        FormField<TextField> emailFormField = createTextField(
            OrganizationEmailLabel,
            OrganizationEmailPlaceholder,
            null
        );
        TextField emailInput = emailFormField.inputField();
        VBox emailFieldContainer = emailFormField.container();
        HBox.setHgrow(emailFieldContainer, Priority.ALWAYS);
        if (isEdit) {
            String email = organization.getStringFieldValue("email");
            if (email != null) emailInput.setText(email);
        }

        // Phone field
        FormField<TextField> phoneFormField = createTextField(
            OrganizationPhoneLabel,
            OrganizationPhonePlaceholder,
            null
        );
        TextField phoneInput = phoneFormField.inputField();
        VBox phoneFieldContainer = phoneFormField.container();
        HBox.setHgrow(phoneFieldContainer, Priority.ALWAYS);
        if (isEdit) {
            String phone = organization.getStringFieldValue("phone");
            if (phone != null) phoneInput.setText(phone);
        }

        contactRow.getChildren().addAll(emailFieldContainer, phoneFieldContainer);

        // ===== SETTINGS SECTION =====
        Label settingsSection = createSectionLabel(OrganizationSettingsSection);

        // Language and Domain in a row
        HBox settingsRow = new HBox(12);
        settingsRow.setMaxWidth(Double.MAX_VALUE);

        // Language selector
        VBox languageField = new VBox(8);
        languageField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(languageField, Priority.ALWAYS);

        Label languageLabel = I18nControls.newLabel(OrganizationLanguageLabel);
        languageLabel.getStyleClass().add("form-field-label");

        ButtonSelectorParameters languageSelectorParams = new ButtonSelectorParameters()
            .setButtonFactory(new ButtonFactoryMixin() {})
            .setDialogParentGetter(FXMainFrameDialogArea::getDialogArea);
        EntityButtonSelector<Language> languageSelector = new EntityButtonSelector<>(
            "{class: 'Language', alias: 'l', columns: [{expression: 'name'}], where: 'supported=true', orderBy: 'name'}",
            dataSourceModel,
            languageSelectorParams
        );
        Button languageButton = languageSelector.getButton();
        languageButton.setMaxWidth(Double.MAX_VALUE);

        if (isEdit && organization.getLanguage() != null) {
            languageSelector.setSelectedItem(organization.getLanguage());
        }

        languageField.getChildren().addAll(languageLabel, languageButton);

        // Domain field
        FormField<TextField> domainFormField = createTextField(
            OrganizationDomainLabel,
            OrganizationDomainPlaceholder,
            null
        );
        TextField domainInput = domainFormField.inputField();
        VBox domainFieldContainer = domainFormField.container();
        HBox.setHgrow(domainFieldContainer, Priority.ALWAYS);
        if (isEdit) {
            String domainName = organization.getStringFieldValue("domainName");
            if (domainName != null) domainInput.setText(domainName);
        }

        settingsRow.getChildren().addAll(languageField, domainFieldContainer);

        // Closed toggle
        VBox closedField = new VBox(8);
        closedField.setPadding(new Insets(12));
        closedField.getStyleClass().add("organization-closed-field");
        closedField.setMaxWidth(Double.MAX_VALUE);

        HBox closedRow = new HBox(12);
        closedRow.setAlignment(Pos.CENTER_LEFT);
        closedRow.setMaxWidth(Double.MAX_VALUE);

        VBox closedTextBox = new VBox(2);
        HBox.setHgrow(closedTextBox, Priority.ALWAYS);

        Label closedLabel = I18nControls.newLabel(OrganizationClosedLabel);
        closedLabel.getStyleClass().add("organization-closed-title");

        Label closedHelp = I18nControls.newLabel(OrganizationClosedHelp);
        closedHelp.getStyleClass().add("organization-closed-help");
        closedHelp.setWrapText(true);

        closedTextBox.getChildren().addAll(closedLabel, closedHelp);

        CheckBox closedCheck = new CheckBox();
        if (isEdit) {
            Boolean closed = organization.getBooleanFieldValue("closed");
            closedCheck.setSelected(closed != null && closed);
        }

        closedRow.getChildren().addAll(closedTextBox, closedCheck);
        closedField.getChildren().add(closedRow);

        // Add all sections to form
        formFields.getChildren().addAll(
            identitySection,
            nameFormField.container(),
            typeField,
            locationSection,
            locationRow1,
            streetFormField.container(),
            locationRow2,
            contactSection,
            contactRow,
            settingsSection,
            settingsRow,
            closedField
        );

        // Validation
        ValidationSupport validationSupport = new ValidationSupport();
        validationSupport.addRequiredInput(nameInput);

        // Add listeners to update entity - EntityBindings.hasChangesProperty() automatically tracks changes
        nameInput.textProperty().addListener((obs, oldVal, newVal) ->
            orgToSave.setName(newVal != null ? newVal.trim() : null));

        typeSelector.selectedItemProperty().addListener((obs, oldVal, newVal) ->
            orgToSave.setType(newVal));

        countrySelector.selectedItemProperty().addListener((obs, oldVal, newVal) ->
            orgToSave.setCountry(newVal));

        cityInput.textProperty().addListener((obs, oldVal, newVal) ->
            orgToSave.setFieldValue("cityName", newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null));

        streetInput.textProperty().addListener((obs, oldVal, newVal) ->
            orgToSave.setFieldValue("street", newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null));

        postCodeInput.textProperty().addListener((obs, oldVal, newVal) ->
            orgToSave.setFieldValue("postCode", newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null));

        timezoneInput.textProperty().addListener((obs, oldVal, newVal) ->
            orgToSave.setFieldValue("timezone", newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null));

        emailInput.textProperty().addListener((obs, oldVal, newVal) ->
            orgToSave.setFieldValue("email", newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null));

        phoneInput.textProperty().addListener((obs, oldVal, newVal) ->
            orgToSave.setFieldValue("phone", newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null));

        languageSelector.selectedItemProperty().addListener((obs, oldVal, newVal) ->
            orgToSave.setLanguage(newVal));

        domainInput.textProperty().addListener((obs, oldVal, newVal) ->
            orgToSave.setFieldValue("domainName", newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null));

        closedCheck.selectedProperty().addListener((obs, oldVal, newVal) ->
            orgToSave.setClosed(newVal));

        // Footer buttons
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(16, 0, 0, 0));

        Button cancelButton = Bootstrap.button(I18nControls.newButton(Cancel));
        Object saveButtonKey = isEdit ? SaveChanges : CreateOrganizationButton;
        Button saveButton = Bootstrap.primaryButton(I18nControls.newButton(saveButtonKey));

        // Bind save button disable property to UpdateStore's hasChanges - automatically updates when entity changes
        saveButton.disableProperty().bind(EntityBindings.hasChangesProperty(updateStore).not());

        footer.getChildren().addAll(cancelButton, saveButton);

        // Add all to dialog content
        dialogContent.getChildren().addAll(headerBox, formFields, footer);

        // Set scroll content
        scrollPane.setContent(dialogContent);
        scrollPane.setMaxHeight(700);

        // Show dialog
        BorderPane dialogPane = new BorderPane(scrollPane);
        dialogPane.getStyleClass().add("modal-dialog-pane");
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());

        // Button actions
        cancelButton.setOnAction(e -> dialogCallback.closeDialog());

        saveButton.setOnAction(e -> {
            // Validate form
            if (!validationSupport.isValid()) {
                return;
            }

            // Submit changes
            updateStore.submitChanges().onSuccess(result -> {
                dialogCallback.closeDialog();
                if (onSuccess != null) {
                    onSuccess.run();
                }
            }).onFailure(error -> showErrorDialog(error.getMessage()));
        });
    }

    private static Label createSectionLabel(Object i18nKey) {
        HBox sectionBox = new HBox();
        sectionBox.setAlignment(Pos.CENTER);
        sectionBox.setMaxWidth(Double.MAX_VALUE);

        Region leftLine = new Region();
        leftLine.getStyleClass().add("section-divider-line");
        HBox.setHgrow(leftLine, Priority.ALWAYS);

        Label sectionLabel = I18nControls.newLabel(i18nKey);
        sectionLabel.getStyleClass().add("section-divider-label");
        sectionLabel.setPadding(new Insets(0, 12, 0, 12));

        Region rightLine = new Region();
        rightLine.getStyleClass().add("section-divider-line");
        HBox.setHgrow(rightLine, Priority.ALWAYS);

        sectionBox.getChildren().addAll(leftLine, sectionLabel, rightLine);

        // Wrap in a Label for compatibility with VBox children type
        Label wrapper = new Label();
        wrapper.setGraphic(sectionBox);
        wrapper.setMaxWidth(Double.MAX_VALUE);
        return wrapper;
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

        Label headerLabel = I18nControls.newLabel(FailedToSaveOrganization);
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

        Button okButton = Bootstrap.dangerButton(I18nControls.newButton(Ok));

        footer.getChildren().add(okButton);
        dialogContent.getChildren().add(footer);

        BorderPane dialogPane = new BorderPane(dialogContent);
        dialogPane.getStyleClass().add("modal-dialog-pane");
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());

        okButton.setOnAction(e -> dialogCallback.closeDialog());
    }
}
