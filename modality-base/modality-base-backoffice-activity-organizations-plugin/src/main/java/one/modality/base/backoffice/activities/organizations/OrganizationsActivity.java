package one.modality.base.backoffice.activities.organizations;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import javafx.scene.text.Text;


/**
 * @author David Hello
 * @author Claude Code
 */
final class OrganizationsActivity extends ViewDomainActivityBase {

    @Override
    public Node buildUi() {
        return new Text("Organizations");
    }

}
