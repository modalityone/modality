package one.modality.crm.frontoffice.activities.userprofile;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.panes.TransitionPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.styles.materialdesign.textfield.MaterialTextFieldPane;
import dev.webfx.extras.styles.materialdesign.util.MaterialUtil;
import dev.webfx.extras.time.pickers.DatePicker;
import dev.webfx.extras.time.pickers.DatePickerOptions;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.authn.AuthenticationService;
import dev.webfx.stack.authn.FinaliseEmailUpdateCredentials;
import dev.webfx.stack.cloud.image.CloudImageService;
import dev.webfx.stack.cloud.image.impl.client.ClientImageService;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelector;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.ui.dialog.DialogCallback;
import dev.webfx.stack.ui.dialog.DialogUtil;
import dev.webfx.stack.ui.validation.ValidationSupport;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.util.Duration;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.client.cloudinary.ModalityCloudinary;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.client.util.converters.Converters;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.Person;
import one.modality.crm.client.i18n.CrmI18nKeys;
import one.modality.crm.frontoffice.activities.createaccount.CreateAccountI18nKeys;
import one.modality.crm.frontoffice.activities.createaccount.UserAccountUI;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

import static one.modality.crm.frontoffice.activities.createaccount.UserAccountUI.createEntityButtonSelector;
import static one.modality.crm.frontoffice.activities.userprofile.ChangePictureUI.CLOUDINARY_RELOAD_DELAY;

final class UserProfileActivity extends ViewDomainActivityBase implements ModalityButtonFactoryMixin {

    private final VBox container = new VBox();
    private final Hyperlink changeUserEmail = I18nControls.newHyperlink(UserProfileI18nKeys.ChangeEmail);
    private final Hyperlink changeUserPassword = I18nControls.newHyperlink(UserProfileI18nKeys.ChangePassword);
    private final TransitionPane transitionPane = new TransitionPane();
    private final ChangeEmailUI changeEmailUI = new ChangeEmailUI(this);
    private final ChangePasswordUI changePasswordUI = new ChangePasswordUI();
    private final ChangePictureUI changePictureUI = new ChangePictureUI(this);
    private final StringProperty tokenProperty = new SimpleStringProperty();
    private final UserProfileMessageUI messagePane = new UserProfileMessageUI();
    private final UserAccountUI accountUI = new UserAccountUI();
    private EntityStore entityStore;
    private UpdateStore updateStore;
    private Person currentPerson;
    private Label nameLabel;
    private Label emailLabel;
    private TextField emailTextField;
    private TextField birthDateTextField;
    private TextField layNameTextField;
    private TextField phoneTextField;
    private TextField postCodeTextField;
    private TextField cityNameTextField;
    protected EntityButtonSelector countrySelector;
    private RadioButton optionMale;
    private RadioButton optionFemale;
    private RadioButton optionLay;
    private RadioButton optionOrdained;
    private ButtonSelector<Organization> organizationSelector;
    private RadioButton optionNoKadampaCenter;
    private Button saveButton;
    private Label infoMessage;
    private DatePicker birthDatePicker;
    private MonoPane pickupDateMonoPane;
    private final CloudImageService cloudImageService = new ClientImageService();
    private ImageView imageView;
    private final String noPictureImage = "images/large/no-picture.png";
    private final ValidationSupport validationSupport = new ValidationSupport();
    public static final int MODAL_WINDOWS_MAX_WIDTH = 500;
    private StackPane picturePane;


    protected void startLogic() {
        entityStore = EntityStore.create(getDataSourceModel());
        updateStore = UpdateStore.createAbove(entityStore);

        accountUI.startLogic(updateStore, UserAccountUI.EDITION_MODE, getHistory());
       
    }

    @Override
    public Node buildUi() {
        //We create a stack pane to manage the datePicker behaviour for the birthDate
        StackPane mainStackPane = new StackPane(container);

        container.setSpacing(20);
        container.getStyleClass().add("user-profile");
        container.setPadding(new Insets(50, 20, 0, 20));
        container.setAlignment(Pos.TOP_CENTER);
        container.setMaxWidth(870);
        Label titleLabel = Bootstrap.h2Primary(I18nControls.newLabel(UserProfileI18nKeys.UserProfileTitle));
        titleLabel.setPadding(new Insets(100, 0, 50, 0));
        container.getChildren().add(titleLabel);

        HBox pictureAndNameHBox = new HBox();
        picturePane = new StackPane();
        picturePane.setAlignment(Pos.CENTER);
        picturePane.setMinSize(150, 150);
        imageView = new ImageView(noPictureImage);
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        MonoPane imageContainerMonoPane = new MonoPane(imageView);
        imageContainerMonoPane.setMinSize(150, 150);
        // Set a circular clip on the StackPane
        Circle clip = new Circle(75); // Radius of the circle
        clip.setCenterX(75); // Center X (adjust if needed)
        clip.setCenterY(75); // Center Y (adjust if needed)
        imageContainerMonoPane.setClip(clip);

        picturePane.getChildren().add(imageContainerMonoPane);
        SVGPath pickupImageSvgPath = SvgIcons.createPickupPicture();
        MonoPane pickupImageMonoPane = new MonoPane(pickupImageSvgPath);
        pickupImageMonoPane.setMinSize(30, 30);
        pickupImageMonoPane.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(50), Insets.EMPTY)));
        picturePane.getChildren().add(pickupImageMonoPane);
        pickupImageMonoPane.setOnMouseClicked(ev -> {
            ScalePane changePictureUIView = changePictureUI.getView();
            DialogCallback callback = DialogUtil.showModalNodeInGoldLayout(changePictureUIView, FXMainFrameDialogArea.getDialogArea());
            FXMainFrameDialogArea.getDialogArea().setOnMouseClicked(e -> {
                callback.closeDialog();
            });
            changePictureUI.setDialogCallback(callback);
            Animations.fadeIn(changePictureUI.getView());
        });
        StackPane.setMargin(pickupImageMonoPane, new Insets(0, 10, 10, 0));
        StackPane.setAlignment(pickupImageMonoPane, Pos.BOTTOM_RIGHT);

        Region spacer = new Region();
        VBox nameVbox = new VBox(10);
        nameVbox.setPadding(new Insets(0, 0, 0, 50));
        nameLabel = Bootstrap.h3(Bootstrap.textSecondary(new Label()));
        emailLabel = Bootstrap.textSecondary(new Label());
        nameVbox.getChildren().addAll(nameLabel, emailLabel);
        nameVbox.setAlignment(Pos.CENTER_LEFT);
        pictureAndNameHBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        pictureAndNameHBox.getChildren().addAll(picturePane, spacer, nameVbox);
        pictureAndNameHBox.setPadding(new Insets(0, 0, 50, 0));
        container.getChildren().add(pictureAndNameHBox);

        Separator separator = new Separator();
        container.getChildren().add(separator);


        /* THE USER INFORMATION  */
        /* ********************* */
        int FIELDS_MAX_WIDTH = 345;

        ColumnsPane columnsPane = new ColumnsPane();
        columnsPane.setMaxColumnCount(2);
        columnsPane.setHgap(100);
        columnsPane.setMinColumnWidth(FIELDS_MAX_WIDTH);
        columnsPane.setPadding(new Insets(50, 0, 0, 0));


        VBox firstColumn = new VBox(15);
        //******* LOGIN DETAILS ********//
        Label loginInfoLabel = Bootstrap.small(Bootstrap.textSecondary(I18nControls.newLabel(CreateAccountI18nKeys.LoginDetails)));
        loginInfoLabel.setPadding(new Insets(0, 0, 10, 0));
        firstColumn.getChildren().add(loginInfoLabel);

        StackPane emailPane = new StackPane();
        emailTextField = newMaterialTextField(CrmI18nKeys.Email);
        Controls.setHtmlInputTypeAndAutocompleteToEmail(emailTextField);
        formatTextFieldLabel(emailTextField);
        emailTextField.setDisable(true);
        emailPane.getChildren().add(emailTextField);
        emailPane.getChildren().add(changeUserEmail);
        StackPane.setAlignment(changeUserEmail, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(changeUserEmail, new Insets(0, 0, 3, 0));
        firstColumn.getChildren().add(emailPane);

        StackPane passwordPane = new StackPane();
        PasswordField passwordField = newMaterialPasswordField(CrmI18nKeys.Password);
        passwordField.setText("*******");
        passwordField.setDisable(true);
        formatTextFieldLabel(passwordField);
        passwordPane.getChildren().addAll(passwordField, changeUserPassword);
        StackPane.setAlignment(changeUserPassword, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(changeUserPassword, new Insets(0, 0, 3, 0));
        firstColumn.getChildren().add(passwordPane);


        //******* PERSONAL DETAILS ********//
        Label personnalDetailsLabel = Bootstrap.small(Bootstrap.textSecondary(I18nControls.newLabel(CreateAccountI18nKeys.PersonalDetails)));
        personnalDetailsLabel.setPadding(new Insets(40, 0, 10, 0));
        firstColumn.getChildren().add(personnalDetailsLabel);

        Label birthDateLabel = Bootstrap.small(new Label(CrmI18nKeys.BirthDate));
        firstColumn.getChildren().add(birthDateLabel);
        birthDateLabel.getStyleClass().add("material-label");

        HBox birthDateHBox = new HBox(10);
        birthDateHBox.setAlignment(Pos.TOP_LEFT);
        birthDateTextField = new TextField();
        I18n.bindI18nTextProperty(birthDateTextField.promptTextProperty(), UserProfileI18nKeys.DateOfBirthFormat);
        birthDateTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            LocalDate dateOfBirth = Converters.convertTextFieldValueToLocalDate(newValue);
            if (dateOfBirth != null) {
                birthDatePicker.setSelectedDate(dateOfBirth);
                birthDatePicker.setDisplayedYearMonth(YearMonth.of(dateOfBirth.getYear(), dateOfBirth.getMonth()));
                currentPerson.setBirthDate(dateOfBirth);
            }
        });
        birthDateHBox.getChildren().add(birthDateTextField);
        pickupDateMonoPane = new MonoPane(SvgIcons.createCalendarPath());
        birthDatePicker = new DatePicker(new DatePickerOptions().setApplyBorderStyle(true).setApplyMaxSize(false));
        birthDateHBox.getChildren().add(pickupDateMonoPane);
        birthDateHBox.setAlignment(Pos.CENTER_LEFT);
        mainStackPane.getChildren().add(birthDatePicker.getView());
        birthDatePicker.getView().setVisible(false);
        birthDatePicker.getView().setManaged(false);
        birthDatePicker.getView().resize(250, 0);

        StackPane.setMargin(birthDatePicker.getView(), new Insets(0, 0, 0, 0));

        pickupDateMonoPane.setOnMouseClicked(e -> {
            if (birthDatePicker.getView().isVisible()) {
                birthDatePicker.getView().setVisible(false);
                return;
            }

            FXProperties.runNowAndOnPropertiesChange(() -> {
                // Get the scene position of the HBox
                Point2D birthDateTextFieldPoint = birthDateTextField.localToScene(0, 0);
                double sceneX = birthDateTextFieldPoint.getX();
                double sceneY = birthDateTextFieldPoint.getY();

                // Convert the scene position to the root container's local coordinates
                Point2D mainStackPanePoint = mainStackPane.sceneToLocal(sceneX, sceneY);
                double rootX = mainStackPanePoint.getX();
                double rootY = mainStackPanePoint.getY();

                birthDatePicker.getView().relocate(rootX, rootY + birthDateHBox.getHeight());
            }, birthDateTextField.layoutXProperty(), birthDateTextField.layoutYProperty(), mainStackPane.widthProperty(), mainStackPane.heightProperty());

            birthDatePicker.getView().setVisible(true);
            birthDatePicker.getSelectedDates().addListener((InvalidationListener) observable -> {
                if (!birthDatePicker.getSelectedDates().isEmpty()) {
                    LocalDate dateOfBirth = LocalDate.of(birthDatePicker.getSelectedDates().get(0).getYear(), birthDatePicker.getSelectedDates().get(0).getMonth(), birthDatePicker.getSelectedDates().get(0).getDayOfMonth());
                    birthDateTextField.setText(Converters.convertLocalDateToTextFieldValue(dateOfBirth));
                    birthDatePicker.getView().setVisible(false);
                }
            });
        });

        firstColumn.getChildren().add(birthDateHBox);

        //Male/Female option
        ToggleGroup maleFemaleToggleGroup = new ToggleGroup();
        // Create RadioButtons
        optionMale = I18nControls.newRadioButton(CrmI18nKeys.Male);
        int firstRadioWidth = 100;
        optionMale.setPrefWidth(firstRadioWidth);
        optionMale.setToggleGroup(maleFemaleToggleGroup);

        optionFemale = I18nControls.newRadioButton(CrmI18nKeys.Female);
        optionFemale.setToggleGroup(maleFemaleToggleGroup);
        // Add a listener to the ToggleGroup to detect selection changes
        maleFemaleToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (optionMale.isSelected()) {
                    currentPerson.setMale(true);
                }
                if (optionFemale.isSelected()) {
                    currentPerson.setMale(false);
                }
            }
        });
        optionMale.setDisable(true);
        optionFemale.setDisable(true);

        //optionFemale.setSelected(!person.isMale());
        HBox maleFemaleHBox = new HBox(20, optionMale, optionFemale);
        maleFemaleHBox.setPadding(new Insets(10, 0, 0, 0));
        maleFemaleHBox.setPrefWidth(FIELDS_MAX_WIDTH);
        firstColumn.getChildren().add(maleFemaleHBox);


        //Lay/Ordained option
        ToggleGroup layOrdainedToggleGroup = new ToggleGroup();
        // Create RadioButtons
        optionLay = I18nControls.newRadioButton(CrmI18nKeys.Lay);
        optionLay.setPrefWidth(firstRadioWidth);
        optionLay.setToggleGroup(layOrdainedToggleGroup);

        optionOrdained = I18nControls.newRadioButton(CrmI18nKeys.Ordained);
        optionOrdained.setToggleGroup(layOrdainedToggleGroup);

        optionLay.setDisable(true);
        optionOrdained.setDisable(true);

        // Add a listener to the ToggleGroup to detect selection changes
        layOrdainedToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (optionOrdained.isSelected()) {
                currentPerson.setOrdained(true);
            }
            if (optionLay.isSelected()) {
                currentPerson.setOrdained(false);
            }
        });

        //optionLay.setSelected(!person.isOrdained());
        HBox layOrdainedHBox = new HBox(20, optionLay, optionOrdained);
        layOrdainedHBox.setPadding(new Insets(10, 0, 0, 0));
        layOrdainedHBox.setPrefWidth(FIELDS_MAX_WIDTH);
        firstColumn.getChildren().add(layOrdainedHBox);

        layNameTextField = newMaterialTextField(CrmI18nKeys.LayName);
        layNameTextField.getStyleClass().setAll("transparent-input");
        formatTextFieldLabel(layNameTextField);
        firstColumn.getChildren().add(layNameTextField);
        layNameTextField.textProperty().addListener((observable, oldValue, newValue) -> currentPerson.setLayName(newValue));
        layNameTextField.setDisable(true);
        Layouts.bindManagedAndVisiblePropertiesTo(optionOrdained.selectedProperty(), layNameTextField);

        phoneTextField = newMaterialTextField(CrmI18nKeys.Phone);
        Controls.setHtmlInputTypeAndAutocompleteToTel(phoneTextField);
        phoneTextField.getStyleClass().setAll("transparent-input");
        formatTextFieldLabel(phoneTextField);
        firstColumn.getChildren().add(phoneTextField);
        phoneTextField.textProperty().addListener((observable, oldValue, newValue) -> currentPerson.setPhone(newValue));


        //******* ADDRESS INFORMATION ********//
        VBox secondColumn = new VBox(15);
        Label addressInformationLabel = Bootstrap.small(Bootstrap.textSecondary(I18nControls.newLabel(CreateAccountI18nKeys.AddressInformation)));
        addressInformationLabel.setPadding(new Insets(0, 0, 10, 0));
        secondColumn.getChildren().add(addressInformationLabel);

        postCodeTextField = newMaterialTextField(CrmI18nKeys.Postcode);
        formatTextFieldLabel(postCodeTextField);
//        Button findAddressButton = Bootstrap.primaryButton(I18nControls.newButton(CreateAccountI18nKeys.FindAddress));
//        Region spacer = new Region();
//        HBox.setHgrow(spacer, Priority.ALWAYS);
//        HBox postCodeHBox = new HBox(30,postCodeTextField,spacer);//TODO: implement the functionality for findAddressButton);
//        postCodeHBox.setAlignment(Pos.BOTTOM_LEFT);
        secondColumn.getChildren().add(postCodeTextField);
        postCodeTextField.textProperty().addListener((observable, oldValue, newValue) -> currentPerson.setPostCode(newValue));

        cityNameTextField = newMaterialTextField(CrmI18nKeys.City);
        formatTextFieldLabel(cityNameTextField);
        secondColumn.getChildren().add(cityNameTextField);
        cityNameTextField.textProperty().addListener((observable, oldValue, newValue) -> currentPerson.setCityName(newValue));


        ButtonSelectorParameters buttonSelectorParameters = new ButtonSelectorParameters().setButtonFactory(this).setDialogParentGetter(FXMainFrameDialogArea::getDialogArea);
        String countryJson = "{class: 'Country', orderBy: 'name'}";
        if (WebFxKitLauncher.supportsSvgImageFormat())
            countryJson = "{class: 'Country', orderBy: 'name', columns: [{expression: '[image(`images/s16/countries/svg/` + iso_alpha2 + `.svg`),name]'}] }";
        countrySelector = createEntityButtonSelector(countryJson, DataSourceModelService.getDefaultDataSourceModel(), buttonSelectorParameters);
        MaterialTextFieldPane countryButton = countrySelector.toMaterialButton(CrmI18nKeys.Country);
        countryButton.getMaterialTextField().setAnimateLabel(false);

        //TODO: see how to put  the select bg color transparent
        secondColumn.getChildren().add(countryButton);
        countrySelector.selectedItemProperty().addListener((observable, oldValue, newValue) -> currentPerson.setCountry(newValue));


        //******* KADAMPA CENTER ********//
        Label kadampaCenterLabel = Bootstrap.small(Bootstrap.textSecondary(I18nControls.newLabel(CreateAccountI18nKeys.SelectKadampaCenter)));
        kadampaCenterLabel.setPadding(new Insets(40, 0, 0, 0));
        secondColumn.getChildren().add(kadampaCenterLabel);

        String organizationJson = "{class: 'Organization', alias: 'o', where: '!closed and name!=`ISC`', orderBy: 'country.name,name'}";
        if (WebFxKitLauncher.supportsSvgImageFormat())
            organizationJson = "{class: 'Organization', alias: 'o', where: '!closed and name!=`ISC`', orderBy: 'country.name,name', columns: [{expression: '[image(`images/s16/organizations/svg/` + (type=2 ? `kmc` : type=3 ? `kbc` : type=4 ? `branch` : `generic`) + `.svg`),name]'}] }";
        organizationSelector = createEntityButtonSelector(organizationJson, DataSourceModelService.getDefaultDataSourceModel(), buttonSelectorParameters);
        MaterialTextFieldPane organizationButton = organizationSelector.toMaterialButton(CrmI18nKeys.Centre);
        //organizationButton.getMaterialTextField().focusLabelFillProperty().setValue(Color.BLACK);
        //TODO: see how to put  the select bg color transparent
        secondColumn.getChildren().add(organizationButton);
        Label orLabel = Bootstrap.small(Bootstrap.textSecondary(I18nControls.newLabel(CreateAccountI18nKeys.Or)));
        secondColumn.getChildren().add(orLabel);


        // Create RadioButtons
        optionNoKadampaCenter = I18nControls.newRadioButton(CreateAccountI18nKeys.NoAttendanceToAKadampaCenter);
        //TODO: Add a listener to the organizationButton to detect selection changes
        secondColumn.getChildren().add(optionNoKadampaCenter);
        organizationButton.getMaterialTextField().setAnimateLabel(false);

        optionNoKadampaCenter.setSelected(null == organizationSelector.getSelectedItem());
        //If the select box is checked, we empty the kadampa center info
        optionNoKadampaCenter.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                organizationSelector.setSelectedItem(null);
            }
        });
        organizationSelector.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            currentPerson.setOrganization(newValue);
            if (newValue != null) optionNoKadampaCenter.setSelected(false);
        });

        columnsPane.getChildren().addAll(firstColumn, secondColumn);

        container.getChildren().add(columnsPane);

        infoMessage = new Label();
        infoMessage.setVisible(false);
        infoMessage.setPadding(new Insets(20, 0, 20, 0));
        container.getChildren().add(infoMessage);

        saveButton = Bootstrap.largePrimaryButton(I18nControls.newButton(CreateAccountI18nKeys.SaveChanges));
        saveButton.disableProperty().bind(EntityBindings.hasChangesProperty(updateStore).not());
        //Here we're editing an existing user
        saveButton.setOnAction(e -> {
            if (validateForm())
                updateStore.submitChanges().
                    onFailure(failure -> {
                        Console.log("Error while updating account:" + failure);
                        Platform.runLater(() -> {
                            infoMessage.setVisible(true);
                            I18nControls.bindI18nProperties(infoMessage, UserProfileI18nKeys.ErrorWhileUpdatingPersonalInformation);
                        });
                    })
                    .onSuccess(success -> {
                        Console.log("Account updated with success");
                        Platform.runLater(() -> {
                            infoMessage.setVisible(true);
                            I18nControls.bindI18nProperties(infoMessage, UserProfileI18nKeys.PersonalInformationUpdated);
                            //Temporary, we force the reload. This is a temp fix while waiting the DynamicEntity.equals function behaviour to be fixed
                            FXUserPerson.reloadUserPerson();
                        });
                    });
        });

        container.getChildren().add(saveButton);

        SVGPath headPhoneSvgPath = SvgIcons.createHeadphonesPath();
        MonoPane headPhoneMonoPane = new MonoPane(headPhoneSvgPath);
        headPhoneMonoPane.setPadding(new Insets(100, 0, 10, 0));
        container.getChildren().add(headPhoneMonoPane);
        transitionPane.transitToContent(mainStackPane);

        Label needHelp = Bootstrap.h3(Bootstrap.textSecondary(I18nControls.newLabel(UserProfileI18nKeys.NeedHelp)));
        needHelp.setPadding(new Insets(0, 0, 10, 0));
        needHelp.setTextAlignment(TextAlignment.CENTER);
        needHelp.setWrapText(true);
        Label emailUs = Bootstrap.strong(I18nControls.newLabel(UserProfileI18nKeys.EmailUs));
        emailUs.setWrapText(true);
        emailUs.setTextAlignment(TextAlignment.CENTER);
        emailUs.setPadding(new Insets(0, 0, 50, 0));
        container.getChildren().addAll(needHelp, emailUs);

        changeUserEmail.setOnAction(e -> {
            ScalePane userEmailUIView = changeEmailUI.getView();
            DialogCallback callback = DialogUtil.showModalNodeInGoldLayout(userEmailUIView, FXMainFrameDialogArea.getDialogArea());
            FXMainFrameDialogArea.getDialogArea().setOnMouseClicked(ev -> {
                callback.closeDialog();
                changeEmailUI.resetToInitialState();
            });
            changeEmailUI.setDialogCallback(callback);
            Animations.fadeIn(changeEmailUI.getView());
        });

        changeUserPassword.setOnAction(e -> {
            ScalePane passwordUIView = changePasswordUI.getView();
            DialogCallback callback = DialogUtil.showModalNodeInGoldLayout(passwordUIView, FXMainFrameDialogArea.getDialogArea());
            FXMainFrameDialogArea.getDialogArea().setOnMouseClicked(ev -> {
                callback.closeDialog();
                changePasswordUI.resetToInitialState();
            });
            changePasswordUI.setDialogCallback(callback);
            Animations.fadeIn(changePasswordUI.getView());
        });


        //We load the data of the person
        FXProperties.runNowAndOnPropertyChange(userPerson -> {
            if (userPerson != null) {
                currentPerson = updateStore.updateEntity(userPerson);
                syncUIFromModel();
            }
        }, FXUserPerson.userPersonProperty());
        FXUserPerson.reloadUserPerson();

        /* **************************************************************************************************************** */
        /*                  HERE WE HANDLE THE EMAIL UPDATE FINALISATION PROCESS                                            */
        /*                  ----------------------------------------------------                                            */
        /* **************************************************************************************************************** */
        // If we go in the code bellow, it means we're on a route similar to /user-profile/email-update/$token_value
        // Here, if the token change and is not null, it means we're trying to update the email of the current user
        FXProperties.runNowAndOnPropertyChange(token -> {
            if (token != null) {
                AuthenticationService.authenticate(new FinaliseEmailUpdateCredentials(token))
                    .onFailure(e -> {
                        String technicalMessage = e.getMessage();
                        Console.log("Technical error: " + technicalMessage);
                        Platform.runLater(() -> {
                            messagePane.setInfoMessage(technicalMessage, Bootstrap.TEXT_DANGER);
                            messagePane.setTitle(UserProfileI18nKeys.Error);
                            DialogCallback callback = DialogUtil.showModalNodeInGoldLayout(messagePane.getView(), FXMainFrameDialogArea.getDialogArea());
                            FXMainFrameDialogArea.getDialogArea().setOnMouseClicked(ev -> {
                                callback.closeDialog();
                                messagePane.resetToInitialState();
                            });
                            messagePane.setDialogCallback(callback);
                            Animations.fadeIn(messagePane.getView());
                        });
                    })
                    .onSuccess(email -> {
                        Console.log("Email change successfully: " + email);
                        Platform.runLater(() -> {
                            messagePane.setInfoMessage(UserProfileI18nKeys.EmailUpdatedWithSuccess, Bootstrap.TEXT_SUCCESS);
                            messagePane.setTitle(UserProfileI18nKeys.UserProfileTitle);
                            DialogCallback callback = DialogUtil.showModalNodeInGoldLayout(messagePane.getView(), FXMainFrameDialogArea.getDialogArea());
                            FXMainFrameDialogArea.getDialogArea().setOnMouseClicked(ev -> {
                                callback.closeDialog();
                                messagePane.resetToInitialState();
                            });
                            messagePane.setDialogCallback(callback);
                            Animations.fadeIn(messagePane.getView());
                            //TODO: ckeck with Bruno why it's not working: We reload the user person to update the email address
                            FXUserPerson.reloadUserPerson();
                            //currentPerson = updateStore.updateEntity(FXUserPerson.getUserPerson());
                            //reloadData();
                        });
                    });
            }
        }, tokenProperty);

        return transitionPane;
    }

    private void syncUIFromModel() {
        infoMessage.setVisible(false);
        nameLabel.setText(currentPerson.getFirstName() + " " + currentPerson.getLastName());
        emailLabel.setText(currentPerson.getEmail());
        emailTextField.setText(currentPerson.getEmail());
        optionMale.setSelected(null != currentPerson.isMale() && currentPerson.isMale());
        optionFemale.setSelected(null != currentPerson.isMale() && !currentPerson.isMale());
        optionOrdained.setSelected(null != currentPerson.isOrdained() && currentPerson.isOrdained());
        optionLay.setSelected(null == currentPerson.isOrdained() || !currentPerson.isOrdained());
        layNameTextField.setText(currentPerson.getLayName());
        phoneTextField.setText(currentPerson.getPhone());
        postCodeTextField.setText(currentPerson.getPostCode());
        cityNameTextField.setText(currentPerson.getCityName());
        countrySelector.setSelectedItem(currentPerson.getCountry());
        organizationSelector.setSelectedItem(currentPerson.getOrganization());
        optionNoKadampaCenter.setSelected(currentPerson.getOrganization() == null);
        if (currentPerson.getBirthDate() != null) {
            birthDateTextField.setText(Converters.convertLocalDateToTextFieldValue(currentPerson.getBirthDate()));
            birthDatePicker.setSelectedDate(currentPerson.getBirthDate());
            birthDateTextField.setDisable(true);
            pickupDateMonoPane.setVisible(false);
        }
        loadProfilePictureIfExist();
    }

    public VBox getContainer() {
        return container;
    }

    public TransitionPane getTransitionPane() {
        return transitionPane;
    }

    protected void updateModelFromContextParameters() {
        tokenProperty.set(getParameter("token"));
    }

    private void formatTextFieldLabel(TextField textField) {
        MaterialUtil.getMaterialTextField(textField).setAnimateLabel(false);
    }

    public void loadProfilePictureIfExist() {
        Object imageTag = ModalityCloudinary.getPersonImageTag(currentPerson.getId().getPrimaryKey());
        doesCloudPictureExist(imageTag)
            .onFailure(ex -> {
                Console.log(ex);
                imageView.setImage(new Image(noPictureImage));

                changePictureUI.setImage(new Image(noPictureImage));
                //   isPictureDisplayed.setValue(false);
            })
            .onSuccess(exists -> Platform.runLater(() -> {
                //Console.log("exists: " + exists);
                if (!exists) {
                    setImage(new Image(noPictureImage));
                    //     isPictureDisplayed.setValue(false);
                } else {
                    //First, we need to get the zoom factor of the screen
                    double zoomFactor = Screen.getPrimary().getOutputScaleX();
                    String url = cloudImageService.url(String.valueOf(imageTag), (int) (imageView.getFitWidth() * zoomFactor), -1);
                    Image imageToDisplay = new Image(url, true);
                    setImage(imageToDisplay);
                }
            }));
    }

    private void setImage(Image newImage) {
        imageView.setImage(newImage);
        changePictureUI.setImage(newImage);
    }

    private Future<Boolean> doesCloudPictureExist(Object eventId) {
        String pictureId = String.valueOf(eventId);
        if (Objects.equals(pictureId, changePictureUI.getRecentlyUploadedCloudPictureId()))
            return Future.succeededFuture(true);
        return cloudImageService.exists(pictureId);
    }

    public Person getCurrentPerson() {
        return currentPerson;
    }

    public CloudImageService getCloudImageService() {
        return cloudImageService;
    }


    private void initFormValidation() {
        if (validationSupport.isEmpty()) {
            validationSupport.addDateOrEmptyValidation(birthDateTextField, "dd/MM/yyyy", birthDateTextField, I18n.i18nTextProperty(UserProfileI18nKeys.DateOfBirthIncorrectFormat));
        }
    }

    /**
     * We validate the form
     *
     * @return true if all the validation is success, false otherwise
     */
    public boolean validateForm() {
        initFormValidation(); // do nothing if not yet initialised
        return validationSupport.isValid();
    }

    public void removeUserProfilePicture() {
        setImage(new Image(noPictureImage));
    }


    // Method to show a progress indicator for 10 seconds
    public void showProgressIndicator() {
        ProgressIndicator progressIndicator = Controls.createProgressIndicator(50); // Set size for the progress indicator
        progressIndicator.setProgress(0);
        picturePane.getChildren().add(progressIndicator); // Add to the stack pane
        StackPane.setAlignment(progressIndicator, Pos.CENTER); // Center it on the image

        // Simulate progress updates
        Animations.animateProperty(progressIndicator.progressProperty(), 1, Duration.millis(CLOUDINARY_RELOAD_DELAY), Interpolator.LINEAR)
            .setOnFinished(e -> removeProgressIndicator());
    }

    // Method to remove the progress indicator
    public void removeProgressIndicator() {
        picturePane.getChildren().removeIf(node -> node instanceof ProgressIndicator);
    }

}
