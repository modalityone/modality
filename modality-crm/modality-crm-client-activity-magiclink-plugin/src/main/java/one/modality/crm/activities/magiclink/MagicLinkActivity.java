package one.modality.crm.activities.magiclink;

import dev.webfx.stack.authn.login.ui.LoginUiService;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

final class MagicLinkActivity extends ViewDomainActivityBase {

    private final StringProperty tokenProperty = new SimpleStringProperty();

    @Override
    protected void updateModelFromContextParameters() {
        tokenProperty.set(getParameter(MagicLinkRouting.PATH_TOKEN_PARAMETER_NAME));
    }

    @Override
    public Node buildUi() {
        return LoginUiService.createMagicLinkUi(tokenProperty, path -> getHistory().replace(path));
    }
}
