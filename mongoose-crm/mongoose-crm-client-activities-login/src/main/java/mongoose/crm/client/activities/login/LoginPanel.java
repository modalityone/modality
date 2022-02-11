package mongoose.crm.client.activities.login;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import mongoose.base.client.activity.MongooseButtonFactoryMixin;
import mongoose.event.client.controls.sectionpanel.SectionPanelFactory;
import mongoose.base.client.validation.MongooseValidationSupport;
import dev.webfx.framework.client.services.i18n.I18nControls;
import dev.webfx.framework.shared.services.authn.AuthenticationRequest;
import dev.webfx.framework.shared.services.authn.UsernamePasswordCredentials;
import dev.webfx.framework.client.ui.controls.button.ButtonUtil;
import dev.webfx.framework.client.ui.controls.dialog.GridPaneBuilder;
import dev.webfx.framework.client.ui.util.layout.LayoutUtil;
import dev.webfx.framework.client.ui.util.scene.SceneUtil;
import dev.webfx.framework.client.ui.uirouter.uisession.UiSession;
import dev.webfx.framework.client.ui.util.anim.Animations;
import dev.webfx.kit.util.properties.Properties;


/**
 * @author Bruno Salmon
 */
public final class LoginPanel implements MongooseButtonFactoryMixin {
    private final Node node;
    private final TextField usernameField;
    private final PasswordField passwordField;
    private final Button button;
    private final Property<Boolean> signInMode = new SimpleObjectProperty<>(true);
    private final MongooseValidationSupport validationSupport = new MongooseValidationSupport();

    public LoginPanel(UiSession uiSession) {
        BorderPane loginWindow = SectionPanelFactory.createSectionPanel("SignInWindowTitle");
        Hyperlink hyperLink = newHyperlink("ForgotPassword?", e -> signInMode.setValue(!signInMode.getValue()));
        GridPane gridPane;
        loginWindow.setCenter(
                gridPane = new GridPaneBuilder()
                        .addNodeFillingRow(usernameField = newMaterialTextField("Email"))
                        .addNodeFillingRow(passwordField = newMaterialPasswordField("Password"))
                        .addNewRow(hyperLink)
                        .addNodeFillingRow(button = newLargeGreenButton(null))
                        .build()
        );
        gridPane.setPadding(new Insets(20));
        GridPane.setHalignment(hyperLink, HPos.CENTER);
        hyperLink.setOnAction(e -> signInMode.setValue(!signInMode.getValue()));
        LayoutUtil.setUnmanagedWhenInvisible(passwordField, signInMode);
        Properties.runNowAndOnPropertiesChange(() ->
                        I18nControls.bindI18nProperties(button, signInMode.getValue() ? "SignIn>>" : "SendPassword>>")
                , signInMode);
        node = LayoutUtil.createGoldLayout(loginWindow);
        initValidation();
        button.setOnAction(event -> {
            if (validationSupport.isValid())
                new AuthenticationRequest()
                        .setUserCredentials(new UsernamePasswordCredentials(usernameField.getText(), passwordField.getText()))
                        .executeAsync().setHandler(ar -> Platform.runLater(() -> { // Executing in UI thread
                    if (ar.succeeded())
                        uiSession.setUserPrincipal(ar.result());
                    else
                        Animations.shake(loginWindow);
                }));
        });
        prepareShowing();
    }

    private void initValidation() {
        validationSupport.addRequiredInput(usernameField, "Username is required");
        validationSupport.addRequiredInput(passwordField, "Password is required");
    }

    public Node getNode() {
        return node;
    }

    public void prepareShowing() {
        // Resetting the default button (required for JavaFx if displayed a second time)
        ButtonUtil.resetDefaultButton(button);
        SceneUtil.autoFocusIfEnabled(usernameField);
    }
}
