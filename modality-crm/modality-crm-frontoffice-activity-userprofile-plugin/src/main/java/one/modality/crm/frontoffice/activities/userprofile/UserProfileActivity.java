package one.modality.crm.frontoffice.activities.userprofile;

import dev.webfx.extras.panes.TransitionPane;
import dev.webfx.extras.panes.transitions.TranslateTransition;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.authn.AuthenticationService;
import dev.webfx.stack.authn.FinaliseEmailUpdateCredentials;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.ui.dialog.DialogCallback;
import dev.webfx.stack.ui.dialog.DialogUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Person;
import one.modality.crm.frontoffice.activities.createaccount.UserAccountUI;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;

final class UserProfileActivity extends ViewDomainActivityBase {

    private final VBox container = new VBox();
    private final Hyperlink changeUserEmail = I18nControls.newHyperlink(UserProfileI18nKeys.ChangeEmail);
    private final Hyperlink changeUserPassword = I18nControls.newHyperlink(UserProfileI18nKeys.ChangePassword);
    private final Hyperlink changePersonalInformation = I18nControls.newHyperlink(UserProfileI18nKeys.ChangePersonalInformation);
    private final TransitionPane transitionPane = new TransitionPane();
    private final ChangeEmailUI changeEmailUI = new ChangeEmailUI(this);
    private final ChangePasswordUI changePasswordUI = new ChangePasswordUI();
    private final StringProperty tokenProperty = new SimpleStringProperty();
    private final UserProfileMessageUI messagePane = new UserProfileMessageUI(this);
    private final UserAccountUI accountUI = new UserAccountUI();
    private EntityStore entityStore;
    private UpdateStore updateStore;

    protected void startLogic() {
        entityStore = EntityStore.create(getDataSourceModel());
        updateStore = UpdateStore.createAbove(entityStore);

        accountUI.startLogic(updateStore, UserAccountUI.EDITION_MODE, getHistory());
        //We load the data of the person
        FXProperties.runNowAndOnPropertyChange(userPerson -> {
            if (userPerson != null) {
                entityStore.<Person>executeQuery(
                        "select organization, country, postCode,cityName, firstName, lastName, male, ordained, email, phone, layName from Person where id=?", FXUserPerson.getUserPerson())
                    .onFailure(Console::log)
                    .onSuccess(currentPersonList -> Platform.runLater(() -> {
                        Person currentPerson = updateStore.updateEntity(currentPersonList.get(0));
                        accountUI.initialiseUI(currentPerson, null);
                    }));
            }
        }, FXUserPerson.userPersonProperty());
    }

    @Override
    public Node buildUi() {
        transitionPane.setTransition(new TranslateTransition(HPos.RIGHT));
        container.setSpacing(20);
        container.getStyleClass().add("user-profile");
        container.setPadding(new Insets(50, 0, 0, 0));
        container.setAlignment(Pos.TOP_CENTER);
        container.getChildren().setAll(changeUserEmail, changeUserPassword, changePersonalInformation);

        transitionPane.transitToContent(container);

        ///FXMainFrameDialogArea.setDialogArea(container);
        changeUserEmail.setOnAction(e -> Platform.runLater(() -> transitionPane.transitToContent(changeEmailUI.getView())));
        changeUserPassword.setOnAction(e -> {
            DialogCallback callback = DialogUtil.showModalNodeInGoldLayout(changePasswordUI.getView(), FXMainFrameDialogArea.getDialogArea());
            FXMainFrameDialogArea.getDialogArea().setOnMouseClicked(ev-> {
                callback.closeDialog();
                changePasswordUI.resetToInitialState();
            });
            changePasswordUI.setDialogCallback(callback);
        });
        changePersonalInformation.setOnAction(e -> Platform.runLater(() -> transitionPane.transitToContent(accountUI.getView())));

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
                            transitionPane.transitToContent(messagePane.getView());
                        });
                    })
                    .onSuccess(email -> {
                        Console.log("Email change successfully: " + email);
                        Platform.runLater(() -> {
                            messagePane.setInfoMessage(UserProfileI18nKeys.PasswordSuccessfullyChanged, Bootstrap.TEXT_SUCCESS);
                            messagePane.setTitle(UserProfileI18nKeys.UserProfile);
                            transitionPane.transitToContent(messagePane.getView());
                        });
                    });
            }
        }, tokenProperty);

        return transitionPane;
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


}
