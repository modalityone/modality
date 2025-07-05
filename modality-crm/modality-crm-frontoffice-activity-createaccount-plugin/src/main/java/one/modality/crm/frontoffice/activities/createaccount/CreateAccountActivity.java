package one.modality.crm.frontoffice.activities.createaccount;


import dev.webfx.extras.panes.GoldenRatioPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.authn.AuthenticationService;
import dev.webfx.stack.authn.ContinueAccountCreationCredentials;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.shared.entities.Person;
import one.modality.crm.activities.magiclink.MagicLinkI18nKeys;

/**
 * @author Bruno Salmon
 */
final class CreateAccountActivity extends ViewDomainActivityBase implements ModalityButtonFactoryMixin  {

    private final MonoPane container = new MonoPane();
    private UserAccountUI accountUI = new UserAccountUI();
    private final StringProperty tokenProperty = new SimpleStringProperty();
    private BorderPane createAccountBorderPane;
    UpdateStore updateStore;

    protected void startLogic() {
        updateStore = UpdateStore.create(getDataSourceModel());
        accountUI.startLogic(updateStore, UserAccountUI.CREATION_MODE,getHistory());
    }

    public Node buildUi() {

        FXProperties.runNowAndOnPropertyChange(token -> {
            if (token == null) {
                I18n.bindI18nProperties(new Text(), MagicLinkI18nKeys.MagicLinkUnrecognisedError);
            } else {
                AuthenticationService.authenticate(new ContinueAccountCreationCredentials(token))
                    .onFailure(e -> {
                        String technicalMessage = e.getMessage();
                        Console.log("Technical error: " + technicalMessage);
                        UiScheduler.runInUiThread(() -> accountUI.displayError(e));
                    })
                    .onSuccess(email -> {
                        Person person = updateStore.insertEntity(Person.class);
                        person.setEmail(email.toString());
                        UiScheduler.runInUiThread(()->accountUI.initialiseUI(person,token));
                    });
            }
        }, tokenProperty);

        container.getStyleClass().add("login");
        container.getStyleClass().add("user-account");
        container.setAlignment(Pos.CENTER);
        container.setMaxWidth(Double.MAX_VALUE);

        GoldenRatioPane content = new GoldenRatioPane();
        container.setContent(content);

        content.setPadding(new Insets(300,0,50,0));
        content.setAlignment(Pos.CENTER);

        createAccountBorderPane = accountUI.getView();
        createAccountBorderPane.setMaxWidth(586);
        content.getChildren().add(createAccountBorderPane);
        createAccountBorderPane.getStyleClass().add("login-child");
        return container;
    }

    protected void updateModelFromContextParameters() {
        tokenProperty.set(getParameter(CreateAccountRouting.PATH_TOKEN_PARAMETER_NAME));
    }

}
