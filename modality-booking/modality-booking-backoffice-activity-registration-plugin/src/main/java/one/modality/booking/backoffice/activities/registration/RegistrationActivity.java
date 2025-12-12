package one.modality.booking.backoffice.activities.registration;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import javafx.scene.text.Text;


/**
 *
 * @author David Hello
 * @author Claude Code
 */
final class RegistrationActivity extends ViewDomainActivityBase {

    @Override
    public Node buildUi() {
        return new Text("Registration");
    }

}
