package one.modality.crm.client.activities.login;

import javafx.scene.Node;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;

/**
 * @author Bruno Salmon
 */
final class LoginViewActivity extends ViewDomainActivityBase {

    private LoginPanel loginPanel;

    @Override
    public Node buildUi() {
        loginPanel = new LoginPanel(getUiSession());
        return loginPanel.getNode();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (loginPanel != null)
            loginPanel.prepareShowing();
    }
}
