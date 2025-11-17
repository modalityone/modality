package one.modality.crm.frontoffice.activities.userprofile;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.I18nKeys;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.MonoClipPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.responsive.ResponsiveDesign;
import dev.webfx.extras.responsive.ResponsiveLayout;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.styles.materialdesign.textfield.MaterialTextField;
import dev.webfx.extras.styles.materialdesign.textfield.MaterialTextFieldPane;
import dev.webfx.extras.styles.materialdesign.util.MaterialUtil;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.time.pickers.DateField;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Booleans;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelector;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.client.time.FrontOfficeTimeFormats;
import one.modality.base.shared.entities.Country;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.Person;
import one.modality.crm.client.i18n.CrmI18nKeys;
import one.modality.crm.frontoffice.activities.createaccount.CreateAccountI18nKeys;
import one.modality.crm.frontoffice.activities.createaccount.UserAccountUI;

/**
 * @author David Hello
 */
public final class UserProfileView implements ModalityButtonFactoryMixin {

    private static final double PROFILE_IMAGE_SIZE = 150;
    public static final String NO_PICTURE_IMAGE = "images/large/no-picture.png";
    private static final double FIELDS_MIN_WIDTH = 200;

    private final boolean showTitle;
    private final boolean showProfileHeader;
    private final boolean showName;
    private final boolean showEmail;
    private final boolean showPassword;
    private final boolean showPersonalDetails;
    private final boolean showAddress;
    private final boolean showKadampaCenter;
    private final boolean showSaveChangesButton;
    private final boolean showCancelButton;

    private final ChangePictureUI changePictureUI;

    public VBox container;
    public Hyperlink changeUserEmail;
    public Hyperlink changeUserPassword;
    public MonoPane pictureImageContainer;
    public StackPane picturePane;
    public Label nameLabel;
    public Label emailLabel;
    public TextField firstNameTextField;
    public TextField lastNameTextField;
    public TextField emailTextField;
    public DateField birthDateField;
    public TextField layNameTextField;
    public TextField phoneTextField;
    public TextField streetTextField;
    public TextField postCodeTextField;
    public TextField cityNameTextField;
    public EntityButtonSelector<Country> countrySelector;
    public RadioButton optionMale;
    public RadioButton optionFemale;
    public RadioButton optionLay;
    public RadioButton optionOrdained;
    public ButtonSelector<Organization> organizationSelector;
    public RadioButton noOrganizationRadioButton;
    public Label infoMessage;
    public Button saveButton;
    public Button cancelButton;
    private Person currentEditedPerson;
    private VBox loginDetailsVBox, personalDetailsVBox, addressInfoVBox, kadampaCenterVBox;
    private boolean syncing;


    public UserProfileView(ChangePictureUI changePictureUI, boolean showTitle, boolean showProfileHeader, boolean showName, boolean showEmail, boolean showPassword, boolean showPersonalDetails, boolean showAddress, boolean showKadampaCenter, boolean showSaveChangesButton, boolean showCancelButton, Person person) {
        this.changePictureUI = changePictureUI;
        this.showTitle = showTitle;
        this.showName = showName;
        this.showProfileHeader = showProfileHeader;
        this.showEmail = showEmail;
        this.showPassword = showPassword;
        this.showPersonalDetails = showPersonalDetails;
        this.showAddress = showAddress;
        this.showKadampaCenter = showKadampaCenter;
        this.showSaveChangesButton = showSaveChangesButton;
        this.showCancelButton = showCancelButton;
        this.currentEditedPerson = person;
    }

    public void setCurrentEditedPerson(Person currentEditedPerson) {
        this.currentEditedPerson = currentEditedPerson;
    }

    public VBox buildView() {
        container = new VBox();
        container.setSpacing(20);
        container.getStyleClass().add("user-profile");
        container.setAlignment(Pos.TOP_CENTER);

        Label titleLabel = buildTitle();
        container.getChildren().add(titleLabel);
        setManagedAndVisible(titleLabel, showTitle);

        Node profileHeader = buildProfileHeader();
        container.getChildren().add(profileHeader);
        setManagedAndVisible(profileHeader, showProfileHeader);

        ColumnsPane columnsPane = new ColumnsPane();
        columnsPane.setMaxColumnCount(2);
        columnsPane.setHgap(100);
        columnsPane.setMinColumnWidth(FIELDS_MIN_WIDTH);
        columnsPane.setVgap(50);
        columnsPane.getStyleClass().add("personal-details-container");

        VBox firstColumn = new VBox(15);
        loginDetailsVBox = buildLoginDetails();
        firstColumn.getChildren().add(loginDetailsVBox);
        setManagedAndVisible(loginDetailsVBox, showEmail || showPassword);

        personalDetailsVBox = buildPersonalDetails();
        firstColumn.getChildren().add(personalDetailsVBox);
        setManagedAndVisible(personalDetailsVBox, showPersonalDetails);
        firstColumn.managedProperty().bind(loginDetailsVBox.managedProperty());
        firstColumn.visibleProperty().bind(loginDetailsVBox.visibleProperty());

        VBox secondColumn = new VBox(15);
        addressInfoVBox = buildAddressInfo();
        secondColumn.getChildren().add(addressInfoVBox);
        setManagedAndVisible(addressInfoVBox, showAddress);

        kadampaCenterVBox = buildKadampaCenter();
        secondColumn.getChildren().add(kadampaCenterVBox);
        setManagedAndVisible(kadampaCenterVBox, showKadampaCenter);

        columnsPane.getChildren().addAll(firstColumn, secondColumn);
        container.getChildren().add(columnsPane);

        infoMessage = new Label();
        infoMessage.setVisible(false);
        infoMessage.setPadding(new Insets(20, 0, 20, 0));
        container.getChildren().add(infoMessage);

        ColumnsPane buttonsColumnsPane = new ColumnsPane();
        buttonsColumnsPane.setMaxColumnCount(2);
        buttonsColumnsPane.setHgap(50);
        buttonsColumnsPane.setMinColumnWidth(200);
        buttonsColumnsPane.setVgap(30);

        cancelButton = buildCancelButton();
        setManagedAndVisible(cancelButton, showCancelButton);

        saveButton = buildSaveChangesButton();
        setManagedAndVisible(saveButton, showSaveChangesButton);

        buttonsColumnsPane.getChildren().addAll(cancelButton,saveButton);
        container.getChildren().add(buttonsColumnsPane);

        FXProperties.runOnPropertiesChange(this::syncModelFromUI,
            firstNameTextField.textProperty(),
            lastNameTextField.textProperty(),
            emailTextField.textProperty(),
            layNameTextField.textProperty(),
            phoneTextField.textProperty(),
            streetTextField.textProperty(),
            postCodeTextField.textProperty(),
            cityNameTextField.textProperty(),
            countrySelector.selectedItemProperty(),
            organizationSelector.selectedItemProperty(),
            noOrganizationRadioButton.selectedProperty(),
            birthDateField.dateProperty(),
            optionMale.selectedProperty(),
            optionLay.selectedProperty()
        );

        return container;
    }

    private Label buildTitle() {
        Label titleLabel = Bootstrap.h2Primary(I18nControls.newLabel(UserProfileI18nKeys.UserProfileTitle));
        titleLabel.setWrapText(true);
        titleLabel.setPadding(new Insets(0, 0, 50, 0));
        return titleLabel;
    }

    private Node buildProfileHeader() {
        pictureImageContainer = new MonoClipPane(true);
        picturePane = new StackPane();
        Layouts.setFixedSize(picturePane, PROFILE_IMAGE_SIZE, PROFILE_IMAGE_SIZE);
        Layouts.setFixedSize(pictureImageContainer, PROFILE_IMAGE_SIZE, PROFILE_IMAGE_SIZE);

        picturePane.getChildren().add(pictureImageContainer);
        SVGPath pickupImageSvgPath = SvgIcons.createPickupPicture();
        MonoPane pickupImageMonoPane = new MonoPane(pickupImageSvgPath);
        pickupImageMonoPane.setMinSize(30, 30);
        pickupImageMonoPane.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(50), Insets.EMPTY)));
        picturePane.getChildren().add(pickupImageMonoPane);
        pickupImageMonoPane.setOnMouseClicked(ev -> {
            changePictureUI.initializeWithCurrentPicture();
            Region changePictureUIView = changePictureUI.getView();
            DialogCallback callback = DialogUtil.showModalNodeInGoldLayout(changePictureUIView, FXMainFrameDialogArea.getDialogArea());
            FXMainFrameDialogArea.getDialogArea().setOnMouseClicked(e -> callback.closeDialog());
            changePictureUI.setDialogCallback(callback);
            Animations.fadeIn(changePictureUIView);
        });
        StackPane.setMargin(pickupImageMonoPane, new Insets(0, 10, 10, 0));
        StackPane.setAlignment(pictureImageContainer, Pos.CENTER);
        StackPane.setAlignment(pickupImageMonoPane, Pos.BOTTOM_RIGHT);

        nameLabel = Bootstrap.h3(Bootstrap.textSecondary(new Label()));
        emailLabel = Bootstrap.textSecondary(new Label());
        VBox nameVbox = new VBox(10, nameLabel, emailLabel);
        HBox.setHgrow(nameVbox, Priority.ALWAYS);

        MonoPane pictureAndNameResponsivePane = new MonoPane(nameVbox);
        pictureAndNameResponsivePane.setPadding(new Insets(0, 0, 50, 0));
        HBox hBox = new HBox(picturePane, nameVbox);
        new ResponsiveDesign(pictureAndNameResponsivePane)
            .addResponsiveLayout(new ResponsiveLayout() {
                @Override
                public boolean testResponsiveLayoutApplicability(double width) {
                    double nameVBoxPrefWidth = nameLabel.prefWidth(-1);
                    return nameVBoxPrefWidth > 0 && width > PROFILE_IMAGE_SIZE + nameVBoxPrefWidth + 10;
                }

                @Override
                public void applyResponsiveLayout() {
                    nameVbox.setAlignment(Pos.CENTER_RIGHT);
                    hBox.getChildren().setAll(picturePane, nameVbox);
                    pictureAndNameResponsivePane.setContent(hBox);
                }

                @Override
                public ObservableValue<?>[] getResponsiveTestDependencies() {
                    return new ObservableValue[]{nameLabel.textProperty()};
                }
            }).addResponsiveLayout(() -> {
                nameVbox.setAlignment(Pos.CENTER);
                hBox.getChildren().clear();
                VBox vBox = new VBox(20, picturePane, nameVbox);
                vBox.setAlignment(Pos.CENTER);
                pictureAndNameResponsivePane.setContent(vBox);
            })
            .start();

        Separator profileHeaderSeparator = new Separator();
        return new VBox(pictureAndNameResponsivePane, profileHeaderSeparator);
    }

    public TextField getFirstNameTextField() {
        return firstNameTextField;
    }

    public TextField getLastNameTextField() {
        return lastNameTextField;
    }

    public TextField getEmailTextField() {
        return emailTextField;
    }

    private VBox buildLoginDetails() {
        VBox vBox = new VBox(15);
        Label loginInfoLabel = Bootstrap.small(Bootstrap.textSecondary(I18nControls.newLabel(CreateAccountI18nKeys.LoginDetails)));
        loginInfoLabel.setPadding(new Insets(0, 0, 10, 0));
        vBox.getChildren().add(loginInfoLabel);

        firstNameTextField = newMaterialTextField(CrmI18nKeys.FirstName);
        formatTextFieldLabel(firstNameTextField);
        lastNameTextField = newMaterialTextField(CrmI18nKeys.LastName);
        formatTextFieldLabel(lastNameTextField);
        vBox.getChildren().addAll(firstNameTextField, lastNameTextField);
        setManagedAndVisible(firstNameTextField, showName);
        setManagedAndVisible(lastNameTextField, showName);


        changeUserEmail = I18nControls.newHyperlink(UserProfileI18nKeys.ChangeEmail);
        StackPane emailPane = new StackPane();
        emailTextField = newMaterialTextField(CrmI18nKeys.Email);
        formatTextFieldLabel(emailTextField);
        emailTextField.setDisable(true);
        emailPane.getChildren().addAll(emailTextField, changeUserEmail);
        StackPane.setAlignment(changeUserEmail, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(changeUserEmail, new Insets(0, 0, 3, 0));
        vBox.getChildren().add(emailPane);
        setManagedAndVisible(emailPane, showEmail);

        changeUserPassword = I18nControls.newHyperlink(UserProfileI18nKeys.ChangePassword);
        StackPane passwordPane = new StackPane();
        PasswordField passwordField = newMaterialPasswordField(CrmI18nKeys.Password);
        passwordField.setText("*******");
        passwordField.setDisable(true);
        formatTextFieldLabel(passwordField);
        passwordPane.getChildren().addAll(passwordField, changeUserPassword);
        StackPane.setAlignment(changeUserPassword, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(changeUserPassword, new Insets(0, 0, 3, 0));
        vBox.getChildren().add(passwordPane);
        setManagedAndVisible(passwordPane, showPassword);

        return vBox;
    }

    private VBox buildPersonalDetails() {
        VBox vBox = new VBox(15);
        Label personalDetailsLabel = Bootstrap.small(Bootstrap.textSecondary(I18nControls.newLabel(CrmI18nKeys.PersonalDetails)));
        personalDetailsLabel.setPadding(new Insets(40, 0, 10, 0));
        vBox.getChildren().add(personalDetailsLabel);

        birthDateField = new DateField(container);
        TextField birthDateTextField = birthDateField.getTextField();
        MaterialUtil.makeMaterial(birthDateTextField);
        MaterialTextField materialTextField = MaterialUtil.getMaterialTextField(birthDateTextField);
        materialTextField.setAnimateLabel(false);
        I18n.bindI18nTextProperty(materialTextField.labelTextProperty(), CrmI18nKeys.BirthDate);
        birthDateField.dateTimeFormatterProperty().bind(LocalizedTime.dateFormatterProperty(FrontOfficeTimeFormats.BIRTH_DATE_FORMAT));
        I18n.bindI18nTextProperty(birthDateTextField.promptTextProperty(), I18nKeys.embedInString(I18nKeys.appendColons(UserProfileI18nKeys.DateOfBirthFormat)) + " {0}",
            LocalizedTime.inferLocalDatePatternProperty(birthDateField.dateTimeFormatterProperty(), true));
        birthDateField.getDatePicker().getView().setTranslateY(10);
        vBox.getChildren().add(birthDateField.getView());

        ToggleGroup maleFemaleToggleGroup = new ToggleGroup();
        optionMale = I18nControls.newRadioButton(CrmI18nKeys.Male);
        optionMale.setPrefWidth(100);
        optionMale.setToggleGroup(maleFemaleToggleGroup);
        optionFemale = I18nControls.newRadioButton(CrmI18nKeys.Female);
        optionFemale.setToggleGroup(maleFemaleToggleGroup);
        optionMale.setDisable(true);
        optionFemale.setDisable(true);
        HBox maleFemaleHBox = new HBox(20, optionMale, optionFemale);
        maleFemaleHBox.setPadding(new Insets(10, 0, 0, 0));
        vBox.getChildren().add(maleFemaleHBox);

        ToggleGroup layOrdainedToggleGroup = new ToggleGroup();
        optionLay = I18nControls.newRadioButton(CrmI18nKeys.Lay);
        optionLay.setPrefWidth(100);
        optionLay.setToggleGroup(layOrdainedToggleGroup);
        optionOrdained = I18nControls.newRadioButton(CrmI18nKeys.Ordained);
        optionOrdained.setToggleGroup(layOrdainedToggleGroup);
        optionLay.setDisable(true);
        optionOrdained.setDisable(true);
        HBox layOrdainedHBox = new HBox(20, optionLay, optionOrdained);
        layOrdainedHBox.setPadding(new Insets(10, 0, 0, 0));
        vBox.getChildren().add(layOrdainedHBox);

        layNameTextField = newMaterialTextField(CrmI18nKeys.LayName);
        layNameTextField.getStyleClass().setAll("transparent-input");
        formatTextFieldLabel(layNameTextField);
        vBox.getChildren().add(layNameTextField);
        layNameTextField.setDisable(true);
        Layouts.bindManagedAndVisiblePropertiesTo(optionOrdained.selectedProperty(), layNameTextField);

        phoneTextField = newMaterialTextField(CrmI18nKeys.Phone);
        Controls.setHtmlInputTypeAndAutocompleteToTel(phoneTextField);
        phoneTextField.getStyleClass().setAll("transparent-input");
        formatTextFieldLabel(phoneTextField);
        vBox.getChildren().add(phoneTextField);

        // Add Kelsang auto-detection functionality
        setupKelsangAutoDetection();

        return vBox;
    }

    /**
     * Sets up automatic detection of "Kelsang" in first name or last name fields
     * and automatically selects the ordained option when found.
     */
    private void setupKelsangAutoDetection() {
        // Create a method to check for Kelsang and update ordained status
        Runnable checkForKelsang = () -> {
            String firstName = firstNameTextField.getText();
            String lastName = lastNameTextField.getText();

            boolean containsKelsang = (firstName != null && firstName.toLowerCase().contains("kelsang")) ||
                (lastName != null && lastName.toLowerCase().contains("kelsang"));

            if (containsKelsang && !optionOrdained.isDisable()) {
                optionOrdained.setSelected(true);
            }
        };

        // Add listeners to both name text fields
        firstNameTextField.textProperty().addListener((observable, oldValue, newValue) -> checkForKelsang.run());
        lastNameTextField.textProperty().addListener((observable, oldValue, newValue) -> checkForKelsang.run());
    }

    private VBox buildAddressInfo() {
        VBox vBox = new VBox(15);
        Label addressInformationLabel = Bootstrap.small(Bootstrap.textSecondary(I18nControls.newLabel(CreateAccountI18nKeys.AddressInformation)));
        addressInformationLabel.setPadding(new Insets(0, 0, 10, 0));
        vBox.getChildren().add(addressInformationLabel);

        streetTextField = newMaterialTextField(CrmI18nKeys.Street);
        formatTextFieldLabel(streetTextField);
        vBox.getChildren().add(streetTextField);

        postCodeTextField = newMaterialTextField(CrmI18nKeys.Postcode);
        formatTextFieldLabel(postCodeTextField);
        vBox.getChildren().add(postCodeTextField);

        cityNameTextField = newMaterialTextField(CrmI18nKeys.City);
        formatTextFieldLabel(cityNameTextField);
        vBox.getChildren().add(cityNameTextField);

        ButtonSelectorParameters buttonSelectorParameters = new ButtonSelectorParameters().setButtonFactory(this).setDialogParentGetter(FXMainFrameDialogArea::getDialogArea);
        countrySelector = UserAccountUI.createCountryButtonSelector(DataSourceModelService.getDefaultDataSourceModel(), buttonSelectorParameters);
        MaterialTextFieldPane countryButton = countrySelector.toMaterialButton(CrmI18nKeys.Country);
        countryButton.getMaterialTextField().setAnimateLabel(false);
        vBox.getChildren().add(countryButton);
        return vBox;
    }

    private VBox buildKadampaCenter() {
        VBox vBox = new VBox(15);
        Label kadampaCenterLabel = Bootstrap.small(Bootstrap.textSecondary(I18nControls.newLabel(CreateAccountI18nKeys.SelectKadampaCenter)));
        kadampaCenterLabel.setPadding(new Insets(40, 0, 0, 0));
        vBox.getChildren().add(kadampaCenterLabel);

        ButtonSelectorParameters buttonSelectorParameters = new ButtonSelectorParameters().setButtonFactory(this).setDialogParentGetter(FXMainFrameDialogArea::getDialogArea);
        organizationSelector = UserAccountUI.createOrganizationButtonSelector(DataSourceModelService.getDefaultDataSourceModel(), buttonSelectorParameters);
        MaterialTextFieldPane organizationButton = organizationSelector.toMaterialButton(CrmI18nKeys.Centre);
        organizationButton.getMaterialTextField().setAnimateLabel(false);
        vBox.getChildren().add(organizationButton);
        Label orLabel = Bootstrap.small(Bootstrap.textSecondary(I18nControls.newLabel(CreateAccountI18nKeys.Or)));
        vBox.getChildren().add(orLabel);

        noOrganizationRadioButton = I18nControls.newRadioButton(CreateAccountI18nKeys.NoAttendanceToAKadampaCenter);
        noOrganizationRadioButton.setWrapText(true);
        vBox.getChildren().add(noOrganizationRadioButton);
        return vBox;
    }

    private Button buildSaveChangesButton() {
        Button button = Bootstrap.largePrimaryButton(I18nControls.newButton(CreateAccountI18nKeys.SaveChanges));
        button.setWrapText(true);
        button.setTextAlignment(TextAlignment.CENTER);
        return button;
    }
    private Button buildCancelButton() {
        Button button = Bootstrap.largeSecondaryButton(I18nControls.newButton(CreateAccountI18nKeys.Cancel));
        button.setWrapText(true);
        button.setTextAlignment(TextAlignment.CENTER);
        return button;
    }

    private void formatTextFieldLabel(TextField textField) {
        MaterialUtil.getMaterialTextField(textField).setAnimateLabel(false);
    }

    private void setManagedAndVisible(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    public void setLoginDetailsVisible(boolean visible) {
        setManagedAndVisible(loginDetailsVBox, visible);
    }


    public void setPersonalDetailsVisible(boolean visible) {
        setManagedAndVisible(personalDetailsVBox, visible);
    }

    public void setAddressInfoVisible(boolean visible) {
        setManagedAndVisible(addressInfoVBox, visible);
    }

    public void setKadampaCenterVisible(boolean visible) {
        setManagedAndVisible(kadampaCenterVBox, visible);
    }

    public void setChangeEmailLinkVisible(boolean visible) {
        setManagedAndVisible(changeUserEmail, visible);
    }

    public void setEmailFieldDisabled(boolean disabled) {
        emailTextField.setDisable(disabled);
    }
    public void setOptionMaleFemaleDisabled(boolean disabled) {
        optionMale.setDisable(disabled);
        optionFemale.setDisable(disabled);
    }
    public void setOptionLayOrdainedDisabled(boolean disabled) {
        optionLay.setDisable(disabled);
        optionOrdained.setDisable(disabled);
    }
    public void setBirthDateFieldVisible(boolean visible) {
        setManagedAndVisible(birthDateField.getView(), visible);
    }

    public void setImage(ImageView imageView) {
        pictureImageContainer.setContent(imageView);
    }

    public void syncUIFromModel() {
        if (syncing)
            return;
        syncing = true;
        firstNameTextField.setText(currentEditedPerson.getFirstName());
        lastNameTextField.setText(currentEditedPerson.getLastName());
        emailTextField.setText(currentEditedPerson.getEmail());
        layNameTextField.setText(currentEditedPerson.getLayName());
        phoneTextField.setText(currentEditedPerson.getPhone());
        streetTextField.setText(currentEditedPerson.getStreet());
        postCodeTextField.setText(currentEditedPerson.getPostCode());
        cityNameTextField.setText(currentEditedPerson.getCityName());
        countrySelector.setSelectedItem(currentEditedPerson.getCountry());
        organizationSelector.setSelectedItem(currentEditedPerson.getOrganization());
        birthDateField.setDate(currentEditedPerson.getBirthDate());
        optionOrdained.setSelected(Booleans.isTrue(currentEditedPerson.isOrdained()));
        optionLay.setSelected(Booleans.isFalse(currentEditedPerson.isOrdained()));
        optionMale.setSelected(Booleans.isTrue(currentEditedPerson.isMale()));
        optionFemale.setSelected(Booleans.isFalse(currentEditedPerson.isMale()));
        noOrganizationRadioButton.setSelected(currentEditedPerson.getOrganization() == null);

        syncing = false;
    }

    private void syncModelFromUI() {
        if (syncing)
            return;
        syncing = true;
        if(currentEditedPerson!=null) {
            currentEditedPerson.setFirstName(firstNameTextField.getText());
            currentEditedPerson.setLastName(lastNameTextField.getText());
            currentEditedPerson.setEmail(emailTextField.getText());
            currentEditedPerson.setLayName(layNameTextField.getText());
            currentEditedPerson.setPhone(phoneTextField.getText());
            currentEditedPerson.setStreet(streetTextField.getText());
            currentEditedPerson.setPostCode(postCodeTextField.getText());
            currentEditedPerson.setCityName(cityNameTextField.getText());
            currentEditedPerson.setCountry(countrySelector.getSelectedItem());
            Organization organization = organizationSelector.getSelectedItem();
            boolean noOrganization = noOrganizationRadioButton.isSelected()
                && (organization == null || Entities.sameId(organization, currentEditedPerson.getOrganization()));
            if (noOrganization) {
                organization = null;
            }
            currentEditedPerson.setOrganization(organization);
            currentEditedPerson.setBirthDate(birthDateField.getDate());
            currentEditedPerson.setOrdained(!optionOrdained.isSelected() && !optionLay.isSelected() ? null : optionOrdained.isSelected());
            currentEditedPerson.setMale(!optionMale.isSelected() && !optionFemale.isSelected() ? null : optionMale.isSelected());
            organizationSelector.setSelectedItem(organization);
            noOrganizationRadioButton.setSelected(organization == null);
        }
        syncing = false;
    }
}
