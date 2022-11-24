package one.modality.crm.client.activities.login;

import dev.webfx.stack.auth.login.ui.LoginUi;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
final class LoginViewActivity extends ViewDomainActivityBase {

    //private LoginPanel loginPanel;

    @Override
    public Node buildUi() {
        return LoginUi.createLoginUI();
/*
        loginPanel = new LoginPanel(getUiSession());
        return loginPanel.getNode();
*/
    }

    @Override
    public void onResume() {
        super.onResume();
/*
        if (loginPanel != null)
            loginPanel.prepareShowing();
*/
    }
}
