package one.modality.crm.client.activities.login;

import dev.webfx.stack.authn.login.ui.LoginUiService;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.session.state.client.fx.FXAuthorizationsWaiting;
import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
final class LoginActivity extends ViewDomainActivityBase {


    @Override
    public Node buildUi() {
        FXAuthorizationsWaiting.init();
        return LoginUiService.createLoginUI();
    }

}
