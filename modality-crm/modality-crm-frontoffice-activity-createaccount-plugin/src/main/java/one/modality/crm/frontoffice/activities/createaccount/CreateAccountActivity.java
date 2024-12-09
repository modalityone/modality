package one.modality.crm.frontoffice.activities.createaccount;


import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import javafx.scene.text.Text;

/**
 * @author Bruno Salmon
 */
final class CreateAccountActivity extends ViewDomainActivityBase {

    @Override
    public Node buildUi() {
        return new Text("Create account");
    }
}
