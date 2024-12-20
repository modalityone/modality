package one.modality.crm.frontoffice.activities.userprofile;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.styles.materialdesign.util.MaterialUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.windowlocation.WindowLocation;
import dev.webfx.stack.authn.AuthenticateWithUsernamePasswordCredentials;
import dev.webfx.stack.authn.AuthenticationRequest;
import dev.webfx.stack.authn.InitiateEmailUpdateCredentials;
import dev.webfx.stack.authn.login.ui.FXLoginContext;
import dev.webfx.stack.authn.login.ui.spi.impl.gateway.password.PasswordI18nKeys;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.ui.controls.MaterialFactoryMixin;
import dev.webfx.stack.ui.operation.OperationUtil;
import dev.webfx.stack.ui.validation.ValidationSupport;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Person;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;

public class ChangeEmailUI implements MaterialFactoryMixin {

    private final Label currentEmailAddress = Bootstrap.textSecondary(new Label());
    protected TextField emailTextField;
    protected PasswordField passwordField;
    protected VBox container = new VBox();
    private final ValidationSupport validationSupport = new ValidationSupport();
    private Person currentUser;
    protected Label infoMessage = Bootstrap.textDanger(new Label());
    private final UserProfileActivity parentActivity;
    private String emailAddress = "";

    public ChangeEmailUI(UserProfileActivity activity) {
        parentActivity = activity;
        javafx.scene.control.Label title = Bootstrap.textPrimary(Bootstrap.h3(I18nControls.newLabel(UserProfileI18nKeys.ChangeEmailAddress)));
        container.setPadding(new Insets(100, 0, 0, 0));
        title.setPadding(new Insets(0, 0, 50, 0));

        emailTextField = newMaterialTextField(UserProfileI18nKeys.NewEmailAddress);
        MaterialUtil.getMaterialTextField(emailTextField).setAnimateLabel(false);

        passwordField = newMaterialPasswordField(UserProfileI18nKeys.CurrentPassword);
        MaterialUtil.getMaterialTextField(passwordField).setAnimateLabel(false);

        infoMessage.setVisible(false);
        infoMessage.setWrapText(true);

        Hyperlink backHyperlink = I18nControls.newHyperlink(UserProfileI18nKeys.BackToUserProfile);
        backHyperlink.setOnAction(e2 -> Platform.runLater(() -> parentActivity.getTransitionPane().transitToContent(parentActivity.getContainer())));

        Button actionButton = Bootstrap.largePrimaryButton(I18nControls.newButton(UserProfileI18nKeys.ChangeEmailTitleForButton));

        FXProperties.runNowAndOnPropertyChange(user -> {
            //We reload in case the user changed his email address in the last action
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
                            Object credentials2 = new InitiateEmailUpdateCredentials(emailTextField.getText().trim().toLowerCase(), WindowLocation.getOrigin(), WindowLocation.getPath(), I18n.getLanguage(), FXLoginContext.getLoginContext());
                            UiScheduler.runInUiThread(() -> {
                                OperationUtil.turnOnButtonsWaitMode(actionButton);
                                new AuthenticationRequest()
                                    .setUserCredentials(credentials2)
                                    .executeAsync()
                                    .onComplete(ar -> UiScheduler.runInUiThread(() -> OperationUtil.turnOffButtonsWaitMode(actionButton)))
                                    .onFailure(failure -> {
                                        // callback.notifyUserLoginFailed(failure);
                                        UiScheduler.runInUiThread(() -> {
                                            showMessage(failure.getMessage(), Bootstrap.TEXT_DANGER);
                                            actionButton.setDisable(true);
                                        });
                                    })
                                    .onSuccess(s -> UiScheduler.runInUiThread(() -> {
                                        showMessage(UserProfileI18nKeys.EmailSentForEmailChange, Bootstrap.TEXT_SUCCESS);
                                        actionButton.setDisable(true);
                                        emailTextField.setDisable(true);
                                        passwordField.setDisable(true);
                                    }));
                            });
                        });
                }
            });
        }, FXUserPerson.userPersonProperty());

        container.getChildren().setAll(title, currentEmailAddress, emailTextField, passwordField, infoMessage, actionButton, backHyperlink);
        int MAX_WIDTH = 486;
        container.setMaxWidth(MAX_WIDTH);
        container.setBackground(Background.fill(Color.WHITE));
        container.setAlignment(Pos.CENTER);
        container.setSpacing(20);
    }

    public VBox getView() {
        return container;
    }

    public void showMessage(String errorMessageKey, String cssClass) {
        I18nControls.bindI18nProperties(infoMessage, errorMessageKey);
        infoMessage.getStyleClass().setAll(cssClass);
        infoMessage.setVisible(true);
    }

    /**
     * This method is used to initialise the parameters for the form validation
     */
    private void initFormValidation() {
        if (validationSupport.isEmpty()) {
            validationSupport.addEmailValidation(emailTextField, emailTextField, I18n.i18nTextProperty(PasswordI18nKeys.InvalidEmail));
            validationSupport.addEmailNotEqualValidation(emailTextField, emailAddress, emailTextField, I18n.i18nTextProperty(UserProfileI18nKeys.EmailNotDifferentError));
            validationSupport.addRequiredInput(passwordField);
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

}
