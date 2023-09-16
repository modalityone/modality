package one.modality.crm.client.activities.unauthorized;

import javafx.scene.Node;
import javafx.scene.text.Text;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;

/**
 * @author Bruno Salmon
 */
final class UnauthorizedViewActivity extends ViewDomainActivityBase {

    @Override
    public Node buildUi() {
        return new Text("Sorry, you are not authorized to access this page");
    }
}
