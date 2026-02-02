package one.modality.base.client.application;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.authn.logout.client.operation.LogoutRequest;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.session.Session;
import dev.webfx.stack.session.state.client.fx.FXSession;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;

import java.util.Locale;

/**
 * @author Bruno Salmon
 */
public abstract class ModalityClientMainFrameActivity extends ViewDomainActivityBase
        implements ModalityButtonFactoryMixin
        , OperationActionFactoryMixin {

    { // I18n language storage management (using user session which is based on LocalStorage for now)
        // TODO Move this feature into WebFX Stack
        // Restoring the user language stored from the session
        FXProperties.runNowAndOnPropertyChange(session -> {
            if (session != null) { // The session may be null first time the user launches the application
                Object lang = session.get("lang");
                if (lang != null)
                    I18n.setLanguage(lang);
            }
        }, FXSession.sessionProperty());
        // Saving the user language into the session
        FXProperties.runOnPropertyChange(lang -> {
            Session session = FXSession.getSession();
            if (session != null) {
                session.put("lang", lang);
                session.store();
            }
        }, I18n.languageProperty());
        // Binding the local property to i18n language for LocalizedTimeFormat
        LocalizedTime.localeProperty().bind(I18n.languageProperty().map(lang -> new Locale(lang.toString())));
        // This is to ensure that LogoutRequest is registered in OperationActionRegistry (especially in the front-office
        // where it is not explicitly called but just referred via operation code from configuration). Then the Logout
        // action can be displayed once the user is logged in (via the authorization mechanism).
        newOperationAction(LogoutRequest::new);
    }

    @Override
    public abstract Node buildUi();

    @Override
    public Button newButton() {
        Button button = ModalityButtonFactoryMixin.super.newButton();
        button.setPadding(new Insets(5));
        button.getStyleClass().add("main-frame-header-button");
        return button;
    }

    protected Node createBrandNode() {
        return null;
    }

    protected Node createMainFrameHeaderCenterItem() {
        return Layouts.createHSpace(0);
    }

    protected Region createHeaderTabsBar() {
        return null;
    }

    protected Region createMainFrameFooter() {
        return null;
    }

}
