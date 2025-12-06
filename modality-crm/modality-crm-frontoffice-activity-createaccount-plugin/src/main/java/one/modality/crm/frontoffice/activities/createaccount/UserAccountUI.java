package one.modality.crm.frontoffice.activities.createaccount;


import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.styles.materialdesign.textfield.MaterialTextFieldPane;
import dev.webfx.extras.styles.materialdesign.util.MaterialUtil;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.control.HtmlInputAutocomplete;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Booleans;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.authn.AuthenticationService;
import dev.webfx.stack.authn.FinaliseAccountCreationCredentials;
import dev.webfx.stack.authn.login.ui.spi.impl.gateway.password.PasswordI18nKeys;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.domainmodel.functions.AbcNames;
import one.modality.base.shared.entities.Country;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.Person;
import one.modality.crm.client.activities.login.LoginRouting;
import one.modality.crm.client.i18n.CrmI18nKeys;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserAccountUI implements ModalityButtonFactoryMixin {

    protected TextField firstNameTextField, lastNameTextField, emailTextField, layNameTextField, phoneTextField, postCodeTextField, cityNameTextField;
    protected PasswordField passwordField, repeatPasswordField;
    protected EntityButtonSelector<Country> countrySelector;
    private final BorderPane container = new BorderPane();
    //The property that will be used to decide if we display and manage some elements
    private final BooleanProperty emailManagedProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty nameManagedProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty passwordManagedProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty genderManagedProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty ordainedManagedProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty phoneManagedProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty postCodeManagedProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty cityNameManagedProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty countryManagedProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty kadampaCenterManagedProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty termAndConditionManagedProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty editModeProperty = new SimpleBooleanProperty(false); //is true when we edit a user, false when we add a user
    private Person person;
    private UpdateStore updateStore;
    public static final boolean CREATION_MODE = false;
    public static final boolean EDITION_MODE = true;
    private Button actionButton;
    private BrowsingHistory browsingHistory;
    private final ValidationSupport validationSupport = new ValidationSupport();
    private CheckBox termAndConditionReadCheckBox;

    public void startLogic(UpdateStore uptStore, boolean mode, BrowsingHistory browsingHistory) {
        updateStore = uptStore;
        editModeProperty.set(mode);
        this.browsingHistory = browsingHistory;
        createTitle();
        MonoPane monopane = new MonoPane(Controls.createSpinner(50));
        BorderPane.setMargin(monopane, new Insets(200, 0, 200, 0));
        container.setCenter(monopane);
        if (editModeProperty.get() == EDITION_MODE) {
            passwordManagedProperty.setValue(false);
            termAndConditionManagedProperty.setValue(false);
        }
    }

    private void createTitle() {
        Label title;
        if (editModeProperty.get() == CREATION_MODE)
            title = Bootstrap.h2Primary(I18nControls.newLabel(PasswordI18nKeys.CreateAccountTitle));
        else title = Bootstrap.h2Primary(I18nControls.newLabel(PasswordI18nKeys.EditUserAccount));
        title.setPadding(new Insets(40, 0, 0, 0));
        BorderPane.setAlignment(title, Pos.CENTER);
        container.setTop(title);
    }

    public void initialiseUI(Person pers, String token) {
        person = pers;
        VBox fieldsListVBox = new VBox(15);
        fieldsListVBox.setPadding(new Insets(50, 0, 50, 0));
        int FIELDS_MAX_WIDTH = 370;
        fieldsListVBox.setMaxWidth(FIELDS_MAX_WIDTH);

        //******* LOGIN DETAILS ********//
        Label loginInfoLabel = Bootstrap.small(Bootstrap.textSecondary(I18nControls.newLabel(CreateAccountI18nKeys.LoginDetails)));
        loginInfoLabel.setPadding(new Insets(0, 0, 10, 0));
        Layouts.bindManagedAndVisiblePropertiesTo(emailManagedProperty.or(passwordManagedProperty), loginInfoLabel);
        fieldsListVBox.getChildren().add(loginInfoLabel);

        emailTextField = newMaterialTextField(CrmI18nKeys.Email);
        Controls.setHtmlInputTypeAndAutocompleteToEmail(emailTextField);
        formatTextFieldLabel(emailTextField);
        emailTextField.setDisable(true);
        emailTextField.setText(person.getEmail());
        fieldsListVBox.getChildren().add(emailTextField);
        Layouts.bindManagedAndVisiblePropertiesTo(emailManagedProperty, emailTextField);
        emailTextField.textProperty().addListener((observable, oldValue, newValue) -> person.setEmail(newValue));

        passwordField = newMaterialPasswordField(CrmI18nKeys.Password);
        Controls.setHtmlInputAutocomplete(passwordField, HtmlInputAutocomplete.NEW_PASSWORD);
        formatTextFieldLabel(passwordField);
        fieldsListVBox.getChildren().add(passwordField);
        Layouts.bindManagedAndVisiblePropertiesTo(passwordManagedProperty, passwordField);

        Label passwordStrength = Bootstrap.small(Bootstrap.textSecondary(I18nControls.newLabel(CreateAccountI18nKeys.PasswordStrength)));
        passwordStrength.setWrapText(true);
        fieldsListVBox.getChildren().add(passwordStrength);
        Layouts.bindManagedAndVisiblePropertiesTo(passwordManagedProperty, passwordStrength);

        repeatPasswordField = newMaterialPasswordField(CrmI18nKeys.RepeatPassword);
        Controls.setHtmlInputAutocomplete(repeatPasswordField, HtmlInputAutocomplete.NEW_PASSWORD);
        formatTextFieldLabel(repeatPasswordField);
        fieldsListVBox.getChildren().add(repeatPasswordField);
        Layouts.bindManagedAndVisiblePropertiesTo(passwordManagedProperty, repeatPasswordField);


        //******* PERSONAL DETAILS ********//
        Label personnalDetailsLabel = Bootstrap.small(Bootstrap.textSecondary(I18nControls.newLabel(CrmI18nKeys.PersonalDetails)));
        personnalDetailsLabel.setPadding(new Insets(40, 0, 10, 0));
        Layouts.bindManagedAndVisiblePropertiesTo(nameManagedProperty.or(genderManagedProperty).or(ordainedManagedProperty).or(phoneManagedProperty), personnalDetailsLabel);
        fieldsListVBox.getChildren().add(personnalDetailsLabel);

        firstNameTextField = newMaterialTextField(CrmI18nKeys.FirstName);
        firstNameTextField.setText(person.getFirstName());
        formatTextFieldLabel(firstNameTextField);
        fieldsListVBox.getChildren().add(firstNameTextField);
        Layouts.bindManagedAndVisiblePropertiesTo(nameManagedProperty, firstNameTextField);
        firstNameTextField.textProperty().addListener((observable, oldValue, newValue) -> person.setFirstName(newValue));


        lastNameTextField = newMaterialTextField(CrmI18nKeys.LastName);
        lastNameTextField.setText(person.getLastName());
        formatTextFieldLabel(lastNameTextField);
        fieldsListVBox.getChildren().add(lastNameTextField);
        Layouts.bindManagedAndVisiblePropertiesTo(nameManagedProperty, lastNameTextField);
        lastNameTextField.textProperty().addListener((observable, oldValue, newValue) -> person.setLastName(newValue));


        //Male/Female option
        ToggleGroup maleFemaleToggleGroup = new ToggleGroup();
        // Create RadioButtons
        RadioButton optionMale = I18nControls.newRadioButton(CrmI18nKeys.Male);
        int firstRadioWidth = 100;
        optionMale.setPrefWidth(firstRadioWidth);
        optionMale.setToggleGroup(maleFemaleToggleGroup);

        RadioButton optionFemale = I18nControls.newRadioButton(CrmI18nKeys.Female);
        optionFemale.setToggleGroup(maleFemaleToggleGroup);
        // Add a listener to the ToggleGroup to detect selection changes
        maleFemaleToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newToggle) ->
            person.setMale(newToggle == null ? null : optionMale.isSelected())
        );
        optionMale.setSelected(Booleans.isTrue(person.isMale()));
        optionFemale.setSelected(Booleans.isFalse(person.isMale()));
        HBox maleFemaleHBox = new HBox(20, optionMale, optionFemale);
        maleFemaleHBox.setPadding(new Insets(10, 0, 0, 0));
        maleFemaleHBox.setPrefWidth(FIELDS_MAX_WIDTH);
        fieldsListVBox.getChildren().add(maleFemaleHBox);
        Layouts.bindManagedAndVisiblePropertiesTo(genderManagedProperty, maleFemaleHBox);

        //Lay/Ordained option
        ToggleGroup layOrdainedToggleGroup = new ToggleGroup();
        // Create RadioButtons
        RadioButton optionLay = I18nControls.newRadioButton(CrmI18nKeys.Lay);
        optionLay.setPrefWidth(firstRadioWidth);
        optionLay.setToggleGroup(layOrdainedToggleGroup);

        RadioButton optionOrdained = I18nControls.newRadioButton(CrmI18nKeys.Ordained);
        optionOrdained.setToggleGroup(layOrdainedToggleGroup);

        // Add a listener to the ToggleGroup to detect selection changes
        layOrdainedToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newToggle) -> {
            person.setOrdained(newToggle == null ? null : optionOrdained.isSelected());
        });
        optionOrdained.setSelected(Booleans.isTrue(person.isOrdained()));
        optionLay.setSelected(Booleans.isFalse(person.isOrdained()));
        //optionLay.setSelected(!person.isOrdained());
        HBox layOrdainedHBox = new HBox(20, optionLay, optionOrdained);
        layOrdainedHBox.setPadding(new Insets(10, 0, 0, 0));
        layOrdainedHBox.setPrefWidth(FIELDS_MAX_WIDTH);
        fieldsListVBox.getChildren().add(layOrdainedHBox);
        Layouts.bindManagedAndVisiblePropertiesTo(ordainedManagedProperty, layOrdainedHBox);

        layNameTextField = newMaterialTextField(CrmI18nKeys.LayName);
        layNameTextField.setText(person.getLayName());
        layNameTextField.getStyleClass().clear();
        layNameTextField.getStyleClass().add("transparent-input");
        formatTextFieldLabel(layNameTextField);
        Layouts.bindManagedAndVisiblePropertiesTo(optionOrdained.selectedProperty().and(ordainedManagedProperty), layNameTextField);
        fieldsListVBox.getChildren().add(layNameTextField);
        layNameTextField.textProperty().addListener((observable, oldValue, newValue) -> person.setLayName(newValue));


        phoneTextField = newMaterialTextField(CrmI18nKeys.Phone);
        Controls.setHtmlInputTypeAndAutocompleteToTel(phoneTextField);
        phoneTextField.setText(person.getPhone());
        phoneTextField.getStyleClass().setAll("transparent-input");
        formatTextFieldLabel(phoneTextField);
        Layouts.bindManagedAndVisiblePropertiesTo(phoneManagedProperty, phoneTextField);
        fieldsListVBox.getChildren().add(phoneTextField);
        phoneTextField.textProperty().addListener((observable, oldValue, newValue) -> person.setPhone(newValue));


        //******* ADDRESS INFORMATION ********//
        Label addressInformationLabel = Bootstrap.small(Bootstrap.textSecondary(I18nControls.newLabel(CreateAccountI18nKeys.AddressInformation)));
        addressInformationLabel.setPadding(new Insets(40, 0, 10, 0));
        fieldsListVBox.getChildren().add(addressInformationLabel);
        Layouts.bindManagedAndVisiblePropertiesTo(postCodeManagedProperty.or(cityNameManagedProperty).or(countryManagedProperty), addressInformationLabel);

        postCodeTextField = newMaterialTextField(CrmI18nKeys.Postcode);
        postCodeTextField.setText(person.getPostCode());
        formatTextFieldLabel(postCodeTextField);
//        Button findAddressButton = Bootstrap.primaryButton(I18nControls.newButton(CreateAccountI18nKeys.FindAddress));
//        Region spacer = new Region();
//        HBox.setHgrow(spacer, Priority.ALWAYS);
//        HBox postCodeHBox = new HBox(30,postCodeTextField,spacer);//TODO: implement the functionality for findAddressButton);
//        postCodeHBox.setAlignment(Pos.BOTTOM_LEFT);
        Layouts.bindManagedAndVisiblePropertiesTo(postCodeManagedProperty, postCodeTextField);
        fieldsListVBox.getChildren().add(postCodeTextField);
        postCodeTextField.textProperty().addListener((observable, oldValue, newValue) -> person.setPostCode(newValue));

        cityNameTextField = newMaterialTextField(CrmI18nKeys.City);
        cityNameTextField.setText(person.getCityName());
        formatTextFieldLabel(cityNameTextField);
        Layouts.bindManagedAndVisiblePropertiesTo(cityNameManagedProperty, cityNameTextField);
        fieldsListVBox.getChildren().add(cityNameTextField);
        cityNameTextField.textProperty().addListener((observable, oldValue, newValue) -> person.setCityName(newValue));


        ButtonSelectorParameters buttonSelectorParameters = new ButtonSelectorParameters().setButtonFactory(this).setDialogParentGetter(FXMainFrameDialogArea::getDialogArea);
        countrySelector = createCountryButtonSelector(DataSourceModelService.getDefaultDataSourceModel(), buttonSelectorParameters)
            .setSelectedItem(person.getCountry());
        MaterialTextFieldPane countryButton = countrySelector.toMaterialButton(CrmI18nKeys.Country);
        //countryButton.getMaterialTextField().focusLabelFillProperty().setValue(Color.BLACK);
        Layouts.bindManagedAndVisiblePropertiesTo(countryManagedProperty, countryButton);
        countryButton.getMaterialTextField().setAnimateLabel(false);

        //TODO: see how to put  the select bg color transparent
        fieldsListVBox.getChildren().add(countryButton);
        countrySelector.selectedItemProperty().addListener((observable, oldValue, newValue) -> person.setCountry(newValue));


        //******* KADAMPA CENTER ********//
        Label kadampaCenterLabel = Bootstrap.small(Bootstrap.textSecondary(I18nControls.newLabel(CreateAccountI18nKeys.SelectKadampaCenter)));
        kadampaCenterLabel.setPadding(new Insets(40, 0, 0, 0));
        Layouts.bindManagedAndVisiblePropertiesTo(kadampaCenterManagedProperty, kadampaCenterLabel);
        fieldsListVBox.getChildren().add(kadampaCenterLabel);

        EntityButtonSelector<Organization> organizationSelector = createOrganizationButtonSelector(DataSourceModelService.getDefaultDataSourceModel(), buttonSelectorParameters)
            .setSelectedItem(person.getOrganization());
        MaterialTextFieldPane organizationButton = organizationSelector.toMaterialButton(CrmI18nKeys.Centre);
        //organizationButton.getMaterialTextField().focusLabelFillProperty().setValue(Color.BLACK);
        //TODO: see how to put  the select bg color transparent
        fieldsListVBox.getChildren().add(organizationButton);
        Label orLabel = Bootstrap.small(Bootstrap.textSecondary(I18nControls.newLabel(CreateAccountI18nKeys.Or)));
        fieldsListVBox.getChildren().add(orLabel);


        // Create RadioButtons
        RadioButton optionNoKadampaCenter = I18nControls.newRadioButton(CreateAccountI18nKeys.NoAttendanceToAKadampaCenter);
        optionNoKadampaCenter.setSelected(editModeProperty.get() && person.getOrganization() == null);
        //TODO: Add a listener to the organizationButton to detect selection changes
        fieldsListVBox.getChildren().add(optionNoKadampaCenter);
        Layouts.bindManagedAndVisiblePropertiesTo(kadampaCenterManagedProperty, organizationButton);
        organizationButton.getMaterialTextField().setAnimateLabel(false);

        Layouts.bindManagedAndVisiblePropertiesTo(kadampaCenterManagedProperty, orLabel);
        Layouts.bindManagedAndVisiblePropertiesTo(kadampaCenterManagedProperty, optionNoKadampaCenter);
        optionNoKadampaCenter.setSelected(null == organizationSelector.getSelectedItem() || null == person.getCountry());
        //If the select box is checked, we empty the kadampa center info
        optionNoKadampaCenter.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                organizationSelector.setSelectedItem(null);
            }
        });
        organizationSelector.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            person.setOrganization(newValue);
            if (newValue != null) optionNoKadampaCenter.setSelected(false);
        });

        //******* TERM & COND ********//
        Label termAndCondLabel = Bootstrap.small(Bootstrap.textPrimary(I18nControls.newLabel(CreateAccountI18nKeys.ReadTermAndCond)));
        termAndCondLabel.setPadding(new Insets(40, 0, 0, 0));
        Layouts.bindManagedAndVisiblePropertiesTo(termAndConditionManagedProperty, termAndCondLabel);
        fieldsListVBox.getChildren().add(termAndCondLabel);

        termAndConditionReadCheckBox = I18nControls.newCheckBox(CreateAccountI18nKeys.AgreeTermAndCond);
        Layouts.bindManagedAndVisiblePropertiesTo(termAndConditionManagedProperty, termAndConditionReadCheckBox);
        fieldsListVBox.getChildren().add(termAndConditionReadCheckBox);

        container.setCenter(fieldsListVBox);

        actionButton = Bootstrap.largePrimaryButton(new Button());

        editModeProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                I18nControls.bindI18nProperties(actionButton, CreateAccountI18nKeys.SaveChanges);
            } else {
                I18nControls.bindI18nProperties(actionButton, CreateAccountI18nKeys.CreateAccountButton);
            }
        });

        // Initialize the button's text based on the initial value of editModeProperty (true = edit mode; false = creation mode)
        if (editModeProperty.get()) {
            I18nControls.bindI18nProperties(actionButton, CreateAccountI18nKeys.SaveChanges);
        } else {
            I18nControls.bindI18nProperties(actionButton, CreateAccountI18nKeys.CreateAccountButton);
        }

        actionButton.disableProperty().bind(EntityBindings.hasChangesProperty(updateStore).not());
        if (editModeProperty.get() == CREATION_MODE) {
            actionButton.setOnAction(e -> {
                if (validateForm()) {
                    AuthenticationService.authenticate(new FinaliseAccountCreationCredentials(token, passwordField.getText().trim()))
                        .inUiThread()
                        .onFailure(failure -> {
                            Console.log("Error while creating account:", failure);
                            transformPaneToAccountCreationError(CreateAccountI18nKeys.CreatingAccountError);
                        })
                        .onSuccess(faPk -> {
                            person.setFrontendAccount(faPk);
                            updateStore.submitChanges()
                                .inUiThread()
                                .onFailure(failure -> {
                                    Console.log("Error while creating account:" + failure);
                                    transformPaneToAccountCreationError(CreateAccountI18nKeys.CreatingPersonAssociatedToAccountError);
                                })
                                .onSuccess(success -> {
                                    Console.log("Account created with success");
                                    transformPaneToAccountCreatedWithSuccess();
                                });
                        });
                }
            });
        } else {
            //Here we're editing an existing user
            actionButton.setOnAction(e -> {
                updateStore.submitChanges()
                    .inUiThread()
                    .onFailure(failure -> {
                        Console.log("Error while updating account:" + failure);
                        transformPaneToAccountCreationError(CreateAccountI18nKeys.CreatingPersonAssociatedToAccountError);
                    })
                    .onSuccess(success -> {
                        Console.log("Account updated with success");
                        transformPaneToAccountCreatedWithSuccess();
                    });
                //TODO
            });
        }
        BorderPane.setAlignment(actionButton, Pos.CENTER);
        container.setBottom(actionButton);
        BorderPane.setMargin(actionButton, new Insets(0, 0, 40, 0));

        if (editModeProperty.get() == EDITION_MODE) {
            //In Edit Mode, we prevent to change the firstName and lastName
            firstNameTextField.setDisable(true);
            lastNameTextField.setDisable(true);
        }

    }

    private void transformPaneToAccountCreatedWithSuccess() {
        Label successMessageLabel = I18nControls.newLabel(CreateAccountI18nKeys.AccountCreatedWithSuccess);
        successMessageLabel.setPadding(new Insets(60, 20, 60, 20));
        successMessageLabel.setWrapText(true);
        container.setCenter(successMessageLabel);
        I18nControls.bindI18nProperties(actionButton, CreateAccountI18nKeys.LoginTitle);
        actionButton.disableProperty().unbind();
        actionButton.setDisable(false);
        actionButton.setOnAction(e -> {
            LoginRouting.RouteToLoginRequest routeToLoginRequest = new LoginRouting.RouteToLoginRequest(browsingHistory);
            routeToLoginRequest.setReplace(true);
            routeToLoginRequest.execute();
        });
    }

    /**
     * This method is used to initialise the parameters for the form validation
     */
    private void initFormValidation() {
        if (validationSupport.isEmpty()) {
            validationSupport.addPasswordStrengthValidation(passwordField, I18n.i18nTextProperty(CreateAccountI18nKeys.PasswordStrength));
            validationSupport.addPasswordMatchValidation(passwordField, repeatPasswordField, I18n.i18nTextProperty(CreateAccountI18nKeys.PasswordNotMatchingError));
            validationSupport.addRequiredInput(firstNameTextField);
            validationSupport.addRequiredInput(lastNameTextField);
            validationSupport.addRequiredInput(postCodeTextField);
            validationSupport.addRequiredInput(countrySelector.selectedItemProperty(), countrySelector.getButton(), I18n.i18nTextProperty(CreateAccountI18nKeys.CountryRequired));
            validationSupport.addValidationRule(termAndConditionReadCheckBox.selectedProperty(), termAndConditionReadCheckBox, I18n.i18nTextProperty(CreateAccountI18nKeys.TermsAndCondsRequired));
        }
    }


    /**
     * We validate the form
     *
     * @return true if all the validation is success, false otherwise
     */
    public boolean validateForm() {
        initFormValidation();
        return validationSupport.isValid();
    }

    private void transformPaneToAccountCreationError(Object i18nKey) {
        Label errorMessageLabel = Bootstrap.textDanger(I18nControls.newLabel(i18nKey));
        errorMessageLabel.setPadding(new Insets(60, 20, 60, 20));
        errorMessageLabel.setWrapText(true);
        container.setCenter(errorMessageLabel);
        if (actionButton != null) {
            I18nControls.bindI18nProperties(actionButton, CreateAccountI18nKeys.LoginTitle);
            actionButton.setOnAction(e -> new LoginRouting.RouteToLoginRequest(browsingHistory).execute());
            actionButton.disableProperty().unbind();
            actionButton.setDisable(false);
        }
    }

    public BorderPane getView() {
        return container;
    }

    private void formatTextFieldLabel(TextField textField) {
        MaterialUtil.getMaterialTextField(textField).setAnimateLabel(false);
    }

    public static EntityButtonSelector<Organization> createOrganizationButtonSelector(DataSourceModel dataSourceModel, ButtonSelectorParameters buttonSelectorParameters) {
        String organizationJson = // language=JSON5
            "{class: 'Organization', alias: 'o', where: '!closed', orderBy: 'country.name,name'}";
        if (WebFxKitLauncher.supportsSvgImageFormat())
            organizationJson = // language=JSON5
                "{class: 'Organization', alias: 'o', where: '!closed', orderBy: 'country.name,name', columns: [{expression: '[image(`images/s16/organizations/svg/` + (type=2 ? `kmc` : type=3 ? `kbc` : type=4 ? `branch` : `generic`) + `.svg`),name]'}] }";
        return UserAccountUI.<Organization>createEntityButtonSelector(organizationJson, dataSourceModel, buttonSelectorParameters)
            .setDialogStyleClass("organization-selector-dialog");
    }

    public static EntityButtonSelector<Country> createCountryButtonSelector(DataSourceModel dataSourceModel, ButtonSelectorParameters buttonSelectorParameters) {
        String countryJson = // language=JSON5
            "{class: 'Country', orderBy: 'name'}";
        if (WebFxKitLauncher.supportsSvgImageFormat())
            countryJson = // language=JSON5
                "{class: 'Country', orderBy: 'name', columns: [{expression: '[image(`images/s16/countries/svg/` + iso_alpha2 + `.svg`),name]'}] }";
        return UserAccountUI.<Country>createEntityButtonSelector(countryJson, dataSourceModel, buttonSelectorParameters)
            .setDialogStyleClass("country-selector-dialog");
    }

    public static <T extends Entity> EntityButtonSelector<T> createEntityButtonSelector(Object jsonOrClass, DataSourceModel dataSourceModel, ButtonSelectorParameters buttonSelectorParameters) {
        return new EntityButtonSelector<T>(jsonOrClass, dataSourceModel, buttonSelectorParameters) {
            @Override
            protected void setSearchParameters(String search, EntityStore store) {
                super.setSearchParameters(search, store);
                store.setParameterValue("abcSearchLike", AbcNames.evaluate(search, true));
            }
        }
            .setDialogPrefRowHeight(37)
            .setDialogCellMargin(new Insets(17));
    }

    public void displayError(Throwable e) {
        String technicalMessage = e.getMessage();
        if (technicalMessage != null) {
            String key = findErrorKey(e.getMessage());
            transformPaneToAccountCreationError(key);
        }
    }

    public static String findErrorKey(String error) {
        // Define the regex pattern to match content inside []
        Pattern pattern = Pattern.compile("\\[(.*?)]");
        Matcher matcher = pattern.matcher(error);

        // Find the content inside brackets
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
