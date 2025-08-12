package one.modality.crm.frontoffice.activities.userprofile;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.authn.AuthenticationService;
import dev.webfx.stack.authn.FinaliseEmailUpdateCredentials;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import javafx.animation.Interpolator;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.client.cloudinary.ModalityCloudinary;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.shared.entities.Person;
import one.modality.crm.frontoffice.activities.createaccount.UserAccountUI;
import one.modality.crm.frontoffice.help.HelpPanel;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;

import java.util.Objects;

import static one.modality.crm.frontoffice.activities.userprofile.ChangePictureUI.CLOUDINARY_RELOAD_DELAY;

/**
 * @author David Hello
 */
final class UserProfileActivity extends ViewDomainActivityBase implements ModalityButtonFactoryMixin {

    private final ChangeEmailUI changeEmailUI = new ChangeEmailUI();
    private final ChangePasswordUI changePasswordUI = new ChangePasswordUI();
    private final ChangePictureUI changePictureUI = new ChangePictureUI(this);
    private final StringProperty tokenProperty = new SimpleStringProperty();
    private final UserProfileMessageUI messagePane = new UserProfileMessageUI();
    private final UserAccountUI accountUI = new UserAccountUI();
    private UpdateStore updateStore;
    private Person currentPerson;
    private final ValidationSupport validationSupport = new ValidationSupport();
    private UserProfileView view;

    protected void startLogic() {
        EntityStore entityStore = EntityStore.create(getDataSourceModel());
        updateStore = UpdateStore.createAbove(entityStore);
        accountUI.startLogic(updateStore, UserAccountUI.EDITION_MODE, getHistory());
    }

    @Override
    public Node buildUi() {
        view = new UserProfileView(changePictureUI, true, true, false, true, true, true, true, true, true);
        VBox viewNode = view.buildView();
        viewNode.getChildren().add(HelpPanel.createEmailHelpPanel(UserProfileI18nKeys.UserProfileHelp, "kbs@kadampa.net"));

        // Bind event handlers
        view.layNameTextField.textProperty().addListener((observable, oldValue, newValue) -> currentPerson.setLayName(newValue));
        view.phoneTextField.textProperty().addListener((observable, oldValue, newValue) -> currentPerson.setPhone(newValue));
        view.postCodeTextField.textProperty().addListener((observable, oldValue, newValue) -> currentPerson.setPostCode(newValue));
        view.cityNameTextField.textProperty().addListener((observable, oldValue, newValue) -> currentPerson.setCityName(newValue));
        view.countrySelector.selectedItemProperty().addListener((observable, oldValue, newValue) -> currentPerson.setCountry(newValue));
        view.organizationSelector.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            currentPerson.setOrganization(newValue);
            if (newValue != null) view.noOrganizationRadioButton.setSelected(false);
        });
        view.noOrganizationRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                view.organizationSelector.setSelectedItem(null);
            }
        });

        ToggleGroup maleFemaleToggleGroup = new ToggleGroup();
        view.optionMale.setToggleGroup(maleFemaleToggleGroup);
        view.optionFemale.setToggleGroup(maleFemaleToggleGroup);
        maleFemaleToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (view.optionMale.isSelected()) {
                    currentPerson.setMale(true);
                }
                if (view.optionFemale.isSelected()) {
                    currentPerson.setMale(false);
                }
            }
        });

        ToggleGroup layOrdainedToggleGroup = new ToggleGroup();
        view.optionLay.setToggleGroup(layOrdainedToggleGroup);
        view.optionOrdained.setToggleGroup(layOrdainedToggleGroup);
        layOrdainedToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (view.optionOrdained.isSelected()) {
                currentPerson.setOrdained(true);
            }
            if (view.optionLay.isSelected()) {
                currentPerson.setOrdained(false);
            }
        });

        view.saveButton.disableProperty().bind(EntityBindings.hasChangesProperty(updateStore).not());
        view.saveButton.setOnAction(e -> {
            if (validateForm())
                updateStore.submitChanges()
                    .inUiThread()
                    .onFailure(failure -> {
                        Console.log("Error while updating account:" + failure);
                        view.infoMessage.setVisible(true);
                        I18nControls.bindI18nProperties(view.infoMessage, UserProfileI18nKeys.ErrorWhileUpdatingPersonalInformation);
                    })
                    .onSuccess(success -> {
                        Console.log("Account updated with success");
                        view.infoMessage.setVisible(true);
                        I18nControls.bindI18nProperties(view.infoMessage, UserProfileI18nKeys.PersonalInformationUpdated);
                        FXUserPerson.reloadUserPerson();
                    });
        });

        view.changeUserEmail.setOnAction(e -> {
            Region userEmailUIView = changeEmailUI.getView();
            DialogCallback callback = DialogUtil.showModalNodeInGoldLayout(userEmailUIView, FXMainFrameDialogArea.getDialogArea());
            FXMainFrameDialogArea.getDialogArea().setOnMouseClicked(ev -> {
                callback.closeDialog();
                changeEmailUI.resetToInitialState();
            });
            Animations.fadeIn(userEmailUIView);
        });

        view.changeUserPassword.setOnAction(e -> {
            Region passwordUIView = changePasswordUI.getView();
            DialogCallback callback = DialogUtil.showModalNodeInGoldLayout(passwordUIView, FXMainFrameDialogArea.getDialogArea());
            FXMainFrameDialogArea.getDialogArea().setOnMouseClicked(ev -> {
                callback.closeDialog();
                changePasswordUI.resetToInitialState();
            });
            changePasswordUI.setDialogCallback(callback);
            Animations.fadeIn(passwordUIView);
        });

        //We load the data of the person
        FXProperties.runNowAndOnPropertyChange(userPerson -> {
            if (userPerson != null) {
                currentPerson = updateStore.updateEntity(userPerson);
                syncUIFromModel();
            }
        }, FXUserPerson.userPersonProperty());
        FXUserPerson.reloadUserPerson();

        FXProperties.runNowAndOnPropertyChange(token -> {
            if (token != null) {
                AuthenticationService.authenticate(new FinaliseEmailUpdateCredentials(token))
                    .inUiThread()
                    .onFailure(e -> {
                        String technicalMessage = e.getMessage();
                        Console.log("Technical error: " + technicalMessage);
                        messagePane.setInfoMessage(technicalMessage, "danger");
                        messagePane.setTitle(UserProfileI18nKeys.Error);
                        DialogCallback callback = DialogUtil.showModalNodeInGoldLayout(messagePane.getView(), FXMainFrameDialogArea.getDialogArea());
                        FXMainFrameDialogArea.getDialogArea().setOnMouseClicked(ev -> {
                            callback.closeDialog();
                            messagePane.resetToInitialState();
                        });
                        messagePane.setDialogCallback(callback);
                        Animations.fadeIn(messagePane.getView());
                    })
                    .onSuccess(email -> {
                        Console.log("Email change successfully: " + email);
                        messagePane.setInfoMessage(UserProfileI18nKeys.EmailUpdatedWithSuccess, "success");
                        messagePane.setTitle(UserProfileI18nKeys.UserProfileTitle);
                        DialogCallback callback = DialogUtil.showModalNodeInGoldLayout(messagePane.getView(), FXMainFrameDialogArea.getDialogArea());
                        FXMainFrameDialogArea.getDialogArea().setOnMouseClicked(ev -> {
                            callback.closeDialog();
                            messagePane.resetToInitialState();
                        });
                        messagePane.setDialogCallback(callback);
                        Animations.fadeIn(messagePane.getView());
                        FXUserPerson.reloadUserPerson();
                    });
            }
        }, tokenProperty);

        return FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftTopRightBottomPadding(viewNode);
    }

    private void syncUIFromModel() {
        view.infoMessage.setVisible(false);
        view.nameLabel.setText(currentPerson.getFirstName() + " " + currentPerson.getLastName());
        view.emailLabel.setText(currentPerson.getEmail());
        view.emailTextField.setText(currentPerson.getEmail());
        view.optionMale.setSelected(null != currentPerson.isMale() && currentPerson.isMale());
        view.optionFemale.setSelected(null != currentPerson.isMale() && !currentPerson.isMale());
        view.optionOrdained.setSelected(null != currentPerson.isOrdained() && currentPerson.isOrdained());
        view.optionLay.setSelected(null == currentPerson.isOrdained() || !currentPerson.isOrdained());
        view.layNameTextField.setText(currentPerson.getLayName());
        view.phoneTextField.setText(currentPerson.getPhone());
        view.postCodeTextField.setText(currentPerson.getPostCode());
        view.cityNameTextField.setText(currentPerson.getCityName());
        view.countrySelector.setSelectedItem(currentPerson.getCountry());
        view.organizationSelector.setSelectedItem(currentPerson.getOrganization());
        view.noOrganizationRadioButton.setSelected(currentPerson.getOrganization() == null);
        view.birthDateField.dateProperty().bindBidirectional(EntityBindings.getLocalDateFieldProperty(currentPerson, Person.birthDate));
        if (currentPerson.getBirthDate() != null) {
            view.birthDateField.getTextField().setDisable(true);
        }
        view.birthDateField.getTextField().setDisable(true);
        loadProfilePictureIfExist();
    }

    protected void updateModelFromContextParameters() {
        tokenProperty.set(getParameter("token"));
    }

    public void loadProfilePictureIfExist() {
        String cloudImagePath = ModalityCloudinary.personImagePath(currentPerson);
        if (Objects.equals(cloudImagePath, changePictureUI.getRecentlyUploadedCloudPictureId()))
            return;
        ModalityCloudinary.loadImage(cloudImagePath, view.pictureImageContainer, 150, 150, () -> new ImageView(UserProfileView.NO_PICTURE_IMAGE))
            .onSuccess(imageView -> view.setImage(imageView));
    }

    public Person getCurrentPerson() {
        return currentPerson;
    }

    private void initFormValidation() {
        if (validationSupport.isEmpty()) {
            validationSupport.addDateOrEmptyValidation(view.birthDateField.getTextField(), view.birthDateField.getDateTimeFormatter(), view.birthDateField.getTextField(), I18n.i18nTextProperty(UserProfileI18nKeys.DateOfBirthIncorrectFormat));
        }
    }

    public boolean validateForm() {
        initFormValidation();
        return validationSupport.isValid();
    }

    public void removeUserProfilePicture() {
        view.setImage(new ImageView(new Image(UserProfileView.NO_PICTURE_IMAGE, true)));
    }

    public void showProgressIndicator() {
        ProgressIndicator progressIndicator = new ProgressIndicator(0);
        view.picturePane.getChildren().add(progressIndicator);
        StackPane.setAlignment(progressIndicator, StackPane.getAlignment(view.pictureImageContainer));
        Animations.animateProperty(progressIndicator.progressProperty(), 1, Duration.millis(CLOUDINARY_RELOAD_DELAY), Interpolator.LINEAR)
            .setOnFinished(e -> removeProgressIndicator());
    }

    public void removeProgressIndicator() {
        view.picturePane.getChildren().removeIf(node -> node instanceof ProgressIndicator);
    }
}

