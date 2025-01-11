package one.modality.crm.client.activities.login;

import dev.webfx.stack.authn.login.ui.LoginUiService;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
public final class LoginActivity extends ViewDomainActivityBase {

    @Override
    public Node buildUi() {
        return buildDefaultUi();
    }

    public static Node buildDefaultUi() {
        return LoginUiService.createLoginUI();
    }

}
