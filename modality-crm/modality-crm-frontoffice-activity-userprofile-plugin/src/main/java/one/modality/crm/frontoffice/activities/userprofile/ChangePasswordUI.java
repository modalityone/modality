package one.modality.crm.frontoffice.activities.userprofile;

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
import dev.webfx.stack.ui.operation.OperationUtil;
import dev.webfx.stack.ui.validation.ValidationSupport;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Person;
import one.modality.crm.frontoffice.activities.createaccount.CreateAccountI18nKeys;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;

public class ChangePasswordUI implements MaterialFactoryMixin {

    Label currentEmailAddress =  Bootstrap.textSecondary(new Label());
    protected PasswordField passwordField;
    protected PasswordField newPasswordField;
    protected PasswordField newPasswordRepeatedField;
    protected VBox container = new VBox();
    private static final int MAX_WIDTH = 486;
    private final ValidationSupport validationSupport = new ValidationSupport();
    private boolean validationSupportInitialised = false;
    private Person currentUser;
    protected Label infoMessage = Bootstrap.textDanger(new Label());
    private final UserProfileActivity parentActivity;
    private String emailAddress = "";

    public ChangePasswordUI(UserProfileActivity activity) {
        parentActivity = activity;
        Label title = Bootstrap.textPrimary(Bootstrap.h3(I18nControls.newLabel(UserProfileI18nKeys.ChangePassword)));
        container.setPadding(new Insets(100,0,0,0));
        title.setPadding(new Insets(0,0,50,0));

        passwordField = newMaterialPasswordField(UserProfileI18nKeys.CurrentPassword);
        MaterialUtil.getMaterialTextField(passwordField).setAnimateLabel(false);

        newPasswordField = newMaterialPasswordField(UserProfileI18nKeys.NewPassword);
        MaterialUtil.getMaterialTextField(newPasswordField).setAnimateLabel(false);

        newPasswordRepeatedField = newMaterialPasswordField(UserProfileI18nKeys.NewPasswordAgain);
        MaterialUtil.getMaterialTextField(newPasswordRepeatedField).setAnimateLabel(false);

        Button actionButton = Bootstrap.largePrimaryButton(I18nControls.newButton(UserProfileI18nKeys.ChangePasswordTitleForButton));

        FXProperties.runNowAndOnPropertyChange(user -> {
                //We reload in case we changed the email address recently
                FXUserPerson.reloadUserPerson();
                currentUser = FXUserPerson.getUserPerson();
                if (currentUser != null)
                    emailAddress = currentUser.getEmail();
                I18nControls.bindI18nProperties(currentEmailAddress,UserProfileI18nKeys.CurrentEmail,emailAddress);

                actionButton.setOnAction(e -> {
                    if (validateForm()) {
                        //First we check if the password entered is the correct
                        Object credentials = new AuthenticateWithUsernamePasswordCredentials(emailAddress, passwordField.getText().trim());
                        OperationUtil.turnOnButtonsWaitMode(actionButton);
                        new AuthenticationRequest()
                            .setUserCredentials(credentials)
                            .executeAsync()
                            .onComplete(ar -> UiScheduler.runInUiThread(() -> OperationUtil.turnOffButtonsWaitMode(actionButton)))
                            .onFailure(failure -> Platform.runLater(() -> showMessage(UserProfileI18nKeys.IncorrectPassword, Bootstrap.TEXT_DANGER)))
                            .onSuccess(ignored -> {
                                //Here we send an email
                                //TODO : Change the function when it's ready
                                Object updatePasswordCredentials = new UpdatePasswordCredentials(passwordField.getText().trim(), newPasswordField.getText().trim());
                                UiScheduler.runInUiThread(() -> {
                                    OperationUtil.turnOnButtonsWaitMode(actionButton);
                                    AuthenticationService.updateCredentials(updatePasswordCredentials)
                                        .onComplete(ar -> UiScheduler.runInUiThread(() -> OperationUtil.turnOffButtonsWaitMode(actionButton)))
                                        .onFailure(failure -> UiScheduler.runInUiThread(() -> {
                                            showMessage(failure.getMessage(), Bootstrap.TEXT_DANGER);
                                            actionButton.setDisable(true);
                                        }))
                                        .onSuccess(s -> UiScheduler.runInUiThread(() -> {
                                            showMessage(UserProfileI18nKeys.PasswordSuccessfullyChanged, Bootstrap.TEXT_SUCCESS);
                                            passwordField.setText("");
                                            newPasswordField.setText("");
                                            newPasswordRepeatedField.setText("");
                                        }));
                                });
                            });
                    }
                });
            },FXUserPerson.userPersonProperty());

        infoMessage.setVisible(false);
        infoMessage.setWrapText(true);

        Hyperlink backHyperlink = I18nControls.newHyperlink(UserProfileI18nKeys.BackToUserProfile);
        backHyperlink.setOnAction(e2-> Platform.runLater(()-> parentActivity.getTransitionPane().transitToContent(parentActivity.getContainer())));

        container.getChildren().setAll(title,currentEmailAddress,passwordField,newPasswordField,newPasswordRepeatedField, infoMessage,actionButton,backHyperlink);
        container.setMaxWidth(MAX_WIDTH);
        container.setSpacing(20);
    }

    public VBox getView() {
        return container;
    }

    public void showMessage(String errorMessageKey, String cssClass) {
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
}
