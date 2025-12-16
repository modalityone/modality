package one.modality.crm.backoffice.activities.lettersetup;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import javafx.scene.text.Text;


/**
 *
 * @author David Hello
 * @author Claude Code
 */
final class LetterSetupActivity extends ViewDomainActivityBase {

    @Override
    public Node buildUi() {
        return new Text("Letter Setup");
    }

}
