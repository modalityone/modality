package one.modality.crm.frontoffice.activities.userprofile;

import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.styles.materialdesign.util.MaterialUtil;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.authn.AuthenticateWithUsernamePasswordCredentials;
import dev.webfx.stack.authn.AuthenticationRequest;
import dev.webfx.stack.authn.AuthenticationService;
import dev.webfx.stack.authn.UpdatePasswordCredentials;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.ui.controls.MaterialFactoryMixin;
import dev.webfx.stack.ui.dialog.DialogCallback;
import dev.webfx.stack.ui.operation.OperationUtil;
import dev.webfx.stack.ui.validation.ValidationSupport;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Person;
import one.modality.crm.frontoffice.activities.createaccount.CreateAccountI18nKeys;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;

public class ChangePasswordUI implements MaterialFactoryMixin {

    private final PasswordField passwordField;
    private final PasswordField newPasswordField;
    private final PasswordField newPasswordRepeatedField;
    private final VBox changePasswordVBox = new VBox();
    private final ScalePane container = new ScalePane(changePasswordVBox);
    private final ValidationSupport validationSupport = new ValidationSupport();
    private final Button actionButton;
    private final Button closeButton;
    private Person currentUser;
    private final Label infoMessage = Bootstrap.textDanger(new Label());
    private final Label resultMessage = Bootstrap.textDanger(new Label());
    private String emailAddress = "";
    private DialogCallback callback;

    public ChangePasswordUI() {
        Label title = Bootstrap.textPrimary(Bootstrap.h2(I18nControls.newLabel(UserProfileI18nKeys.ChangePassword)));
        title.setPadding(new Insets(0, 0, 50, 0));

        Label description = Bootstrap.strong(I18nControls.newLabel(UserProfileI18nKeys.UpdatePasswordDesc));
        description.setPadding(new Insets(0, 0, 10, 0));

        passwordField = newMaterialPasswordField(UserProfileI18nKeys.CurrentPassword);
        MaterialUtil.getMaterialTextField(passwordField).setAnimateLabel(false);

        newPasswordField = newMaterialPasswordField(UserProfileI18nKeys.NewPassword);
        MaterialUtil.getMaterialTextField(newPasswordField).setAnimateLabel(false);

        newPasswordRepeatedField = newMaterialPasswordField(UserProfileI18nKeys.NewPasswordAgain);
        MaterialUtil.getMaterialTextField(newPasswordRepeatedField).setAnimateLabel(false);

        actionButton = Bootstrap.largePrimaryButton(I18nControls.newButton(UserProfileI18nKeys.Confirm));
        closeButton = Bootstrap.largePrimaryButton(I18nControls.newButton(UserProfileI18nKeys.Close));
        closeButton.setOnAction(evt -> {
            callback.closeDialog();
            resetToInitialState();
        });
        //We display the close button only when the confirm button is not visible
        closeButton.setVisible(false);
        closeButton.setManaged(false);

        passwordField.disableProperty().bind(closeButton.visibleProperty());
        newPasswordField.disableProperty().bind(closeButton.visibleProperty());
        newPasswordRepeatedField.disableProperty().bind(closeButton.visibleProperty());

        FXProperties.runNowAndOnPropertyChange(user -> {
            //We reload in case we changed the email address recently
            //FXUserPerson.reloadUserPerson();
            currentUser = FXUserPerson.getUserPerson();
            if (currentUser != null)
                emailAddress = currentUser.getEmail();

            actionButton.setOnAction(e -> {
                if (validateForm()) {
                    //First we check if the password entered is the correct
                    Object credentials = new AuthenticateWithUsernamePasswordCredentials(emailAddress, passwordField.getText().trim());
                    OperationUtil.turnOnButtonsWaitMode(actionButton);
                    new AuthenticationRequest()
                        .setUserCredentials(credentials)
                        .executeAsync()
                        .onComplete(ar -> UiScheduler.runInUiThread(() -> OperationUtil.turnOffButtonsWaitMode(actionButton)))
                        .onFailure(failure -> UiScheduler.runInUiThread(() -> {
                            Animations.shake(container);
                            showInfoMessage(UserProfileI18nKeys.IncorrectPassword, Bootstrap.TEXT_DANGER);
                        }))
                        .onSuccess(ignored -> {
                            //Here we send an email
                            //TODO : Change the function when it's ready
                            Object updatePasswordCredentials = new UpdatePasswordCredentials(passwordField.getText().trim(), newPasswordField.getText().trim());
                            UiScheduler.runInUiThread(() -> {
                                OperationUtil.turnOnButtonsWaitMode(actionButton);
                                AuthenticationService.updateCredentials(updatePasswordCredentials)
                                    .onComplete(ar -> UiScheduler.runInUiThread(() -> OperationUtil.turnOffButtonsWaitMode(actionButton)))
                                    .onFailure(failure -> UiScheduler.runInUiThread(() -> {
                                        //  showResultMessage(failure.getMessage(), Bootstrap.TEXT_DANGER);
                                        showInfoMessage(failure.getMessage(), Bootstrap.TEXT_DANGER);
                                        displayCloseButton();

                                        // transitionPane.transitToContent(messageVBox);
                                    }))
                                    .onSuccess(s -> UiScheduler.runInUiThread(() -> {
                                        showResultMessage(UserProfileI18nKeys.PasswordSuccessfullyChanged, Bootstrap.TEXT_SUCCESS);
                                        showInfoMessage(UserProfileI18nKeys.PasswordSuccessfullyChanged, Bootstrap.TEXT_SUCCESS);
                                        displayCloseButton();
                                        // transitionPane.transitToContent(messageVBox);
                                    }));
                            });
                        });
                }
            });
        }, FXUserPerson.userPersonProperty());

        infoMessage.setVisible(false);
        infoMessage.setWrapText(true);
        resultMessage.setWrapText(true);

        changePasswordVBox.getChildren().setAll(title, description, passwordField, newPasswordField, newPasswordRepeatedField, infoMessage, actionButton,closeButton);
        setupModalVBox(changePasswordVBox);

        closeButton.setOnAction(evt -> {
            callback.closeDialog();
            resetToInitialState();
        });

        //We prevent the mouse click event to pass through the Pane
        container.setOnMouseClicked(Event::consume);
    }

    private void displayCloseButton() {
        closeButton.setVisible(true);
        closeButton.setManaged(true);
        actionButton.setVisible(false);
        actionButton.setManaged(false);
    }

    public static void setupModalVBox(VBox vBox) {
        vBox.setPrefWidth(UserProfileActivity.MODAL_WINDOWS_MAX_WIDTH);
        vBox.setSpacing(20);
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setPadding(new Insets(60));
        vBox.getStyleClass().add("user-profile-modal-window");
    }

    public ScalePane getView() {
        return container;
    }

    public void resetToInitialState() {
        passwordField.setText("");
        newPasswordField.setText("");
        newPasswordRepeatedField.setText("");
        showResultMessage("", "");
        showInfoMessage("", "");
        closeButton.setVisible(false);
        closeButton.setManaged(false);
        actionButton.setVisible(true);
        actionButton.setManaged(true);
    }

    public void showResultMessage(String errorMessageKey, String cssClass) {
        I18nControls.bindI18nProperties(resultMessage, errorMessageKey);
        resultMessage.getStyleClass().setAll(cssClass);
    }

    public void showInfoMessage(String errorMessageKey, String cssClass) {
        I18nControls.bindI18nProperties(infoMessage, errorMessageKey);
        infoMessage.getStyleClass().setAll(cssClass);
        infoMessage.setVisible(true);
    }

    /**
     * This method is used to initialise the parameters for the form validation
     */
    private void initFormValidation() {
        if (validationSupport.isEmpty()) {
            validationSupport.addRequiredInput(passwordField);
            validationSupport.addPasswordStrengthValidation(newPasswordField, I18n.i18nTextProperty(CreateAccountI18nKeys.PasswordStrength));
            validationSupport.addPasswordMatchValidation(newPasswordField, newPasswordRepeatedField, I18n.i18nTextProperty(CreateAccountI18nKeys.PasswordNotMatchingError));
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

    public void setDialogCallback(DialogCallback callback) {
        this.callback = callback;
    }
}
