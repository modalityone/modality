package one.modality.crm.frontoffice.activities.userprofile;

import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.panes.TransitionPane;
import dev.webfx.extras.panes.transitions.TranslateTransition;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.styles.materialdesign.util.MaterialUtil;
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
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Person;
import one.modality.crm.frontoffice.activities.createaccount.CreateAccountI18nKeys;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;

public class ChangePasswordUI implements MaterialFactoryMixin {

    private PasswordField passwordField;
    private PasswordField newPasswordField;
    private PasswordField newPasswordRepeatedField;
    private VBox changePasswordVBox = new VBox();
    private VBox messageVBox = new VBox();
    private TransitionPane transitionPane = new TransitionPane(changePasswordVBox);
    private ScalePane container = new ScalePane(transitionPane);
    private static final int CONTAINER_WIDTH = 586;
    private static final int CONTAINER_HEIGHT = 506;
    private final ValidationSupport validationSupport = new ValidationSupport();
    private boolean validationSupportInitialised = false;
    private Person currentUser;
    private Label infoMessage = Bootstrap.textDanger(new Label());
    private Label resultMessage = Bootstrap.textDanger(new Label());
    private final UserProfileActivity parentActivity;
    private String emailAddress = "";
    private DialogCallback callback;

    public ChangePasswordUI(UserProfileActivity activity) {
        parentActivity = activity;
        transitionPane.setTransition(new TranslateTransition());
        container.setBackground(Background.fill(Color.PINK));
        Label title = Bootstrap.textPrimary(Bootstrap.h2(I18nControls.newLabel(UserProfileI18nKeys.ChangePassword)));
        title.setPadding(new Insets(0,0,50,0));

        Label description = Bootstrap.strong(I18nControls.newLabel(UserProfileI18nKeys.UpdatePasswordDesc));
        description.setPadding(new Insets(0,0,10,0));

        passwordField = newMaterialPasswordField(UserProfileI18nKeys.CurrentPassword);
        MaterialUtil.getMaterialTextField(passwordField).setAnimateLabel(false);

        newPasswordField = newMaterialPasswordField(UserProfileI18nKeys.NewPassword);
        MaterialUtil.getMaterialTextField(newPasswordField).setAnimateLabel(false);

        newPasswordRepeatedField = newMaterialPasswordField(UserProfileI18nKeys.NewPasswordAgain);
        MaterialUtil.getMaterialTextField(newPasswordRepeatedField).setAnimateLabel(false);

        Button actionButton = Bootstrap.largePrimaryButton(I18nControls.newButton(UserProfileI18nKeys.Confirm));

        FXProperties.runNowAndOnPropertyChange(user -> {
                //We reload in case we changed the email address recently
                FXUserPerson.reloadUserPerson();
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
                            .onFailure(failure -> Platform.runLater(() -> showInfoMessage(UserProfileI18nKeys.IncorrectPassword, Bootstrap.TEXT_DANGER)))
                            .onSuccess(ignored -> {
                                //Here we send an email
                                //TODO : Change the function when it's ready
                                Object updatePasswordCredentials = new UpdatePasswordCredentials(passwordField.getText().trim(), newPasswordField.getText().trim());
                                UiScheduler.runInUiThread(() -> {
                                    OperationUtil.turnOnButtonsWaitMode(actionButton);
                                    AuthenticationService.updateCredentials(updatePasswordCredentials)
                                        .onComplete(ar -> UiScheduler.runInUiThread(() -> OperationUtil.turnOffButtonsWaitMode(actionButton)))
                                        .onFailure(failure -> UiScheduler.runInUiThread(() -> {
                                            showResultMessage(failure.getMessage(), Bootstrap.TEXT_DANGER);
                                            transitionPane.transitToContent(messageVBox);
                                        }))
                                        .onSuccess(s -> UiScheduler.runInUiThread(() -> {
                                            showResultMessage(UserProfileI18nKeys.PasswordSuccessfullyChanged, Bootstrap.TEXT_SUCCESS);
                                            transitionPane.transitToContent(messageVBox);
                                        }));
                                });
                            });
                    }
                });
            },FXUserPerson.userPersonProperty());

        infoMessage.setVisible(false);
        infoMessage.setWrapText(true);
        resultMessage.setWrapText(true);

        changePasswordVBox.getChildren().setAll(title,description,passwordField,newPasswordField,newPasswordRepeatedField,infoMessage,actionButton);
        changePasswordVBox.setMinWidth(CONTAINER_WIDTH);
        changePasswordVBox.setMaxWidth(CONTAINER_WIDTH);
        changePasswordVBox.setMinHeight(CONTAINER_HEIGHT);
        changePasswordVBox.setMaxHeight(CONTAINER_HEIGHT);
        changePasswordVBox.getStyleClass().add("user-profile-modal-window");
        changePasswordVBox.setSpacing(20);
        changePasswordVBox.setAlignment(Pos.CENTER);

        Button closeButton = Bootstrap.largePrimaryButton(I18nControls.newButton(UserProfileI18nKeys.CloseWindow));

        messageVBox.getChildren().setAll(resultMessage,closeButton);
        messageVBox.setBackground(Background.fill(Color.GREEN));
        messageVBox.setMinWidth(CONTAINER_WIDTH);
        messageVBox.setMaxWidth(CONTAINER_WIDTH);
        messageVBox.setMinHeight(CONTAINER_HEIGHT);
        messageVBox.setMaxHeight(CONTAINER_HEIGHT);
        //messageVBox.getStyleClass().add("user-profile-modal-window");
        messageVBox.setSpacing(20);
        messageVBox.setAlignment(Pos.CENTER);


        closeButton.setOnAction(evt-> {
            callback.closeDialog();
            resetToInitialState();
        });

        //We prevent the mouse click event to pass through the Pane
        container.setOnMouseClicked(e->e.consume());

        container.setMinWidth(CONTAINER_WIDTH);
        container.setCanScaleX(true);
        container.setCanScaleY(true);
    }

    public ScalePane getView() {
        return container;
    }

    public void resetToInitialState() {
        transitionPane.transitToContent(changePasswordVBox);
        passwordField.setText("");
        newPasswordField.setText("");
        newPasswordRepeatedField.setText("");
        showResultMessage("","");
        showInfoMessage("","");
    }

    public void showResultMessage(String errorMessageKey, String cssClass) {
        I18nControls.bindI18nProperties(resultMessage,errorMessageKey);
        resultMessage.getStyleClass().setAll(cssClass);
    }

    public void showInfoMessage(String errorMessageKey, String cssClass) {
        I18nControls.bindI18nProperties(infoMessage,errorMessageKey);
        infoMessage.getStyleClass().setAll(cssClass);
        infoMessage.setVisible(true);
    }

    /**
     * This method is used to initialise the parameters for the form validation
     */
    private void initFormValidation() {
        if (!validationSupportInitialised) {
            FXProperties.runNowAndOnPropertyChange(dictionary -> {
                if (dictionary != null) {
                    validationSupport.reset();
                    validationSupport.addPasswordStrengthValidation(newPasswordField,newPasswordField, I18n.getI18nText(CreateAccountI18nKeys.PasswordStrength));
                    validationSupport.addPasswordMatchValidation(newPasswordField, newPasswordRepeatedField,newPasswordRepeatedField, I18n.getI18nText(CreateAccountI18nKeys.PasswordNotMatchingError));
                }
            }, I18n.dictionaryProperty());
            validationSupportInitialised = true;
        }
    }

    /**
     * We validate the form
     *
     * @return true if all the validation is success, false otherwise
     */
    public boolean validateForm() {
        if (!validationSupportInitialised) {
            initFormValidation();
            validationSupportInitialised = true;
        }
        return validationSupport.isValid();
    }

    public void setDialogCallback(DialogCallback callback) {
        this.callback = callback;
    }
}
