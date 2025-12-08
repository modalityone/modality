package one.modality.hotel.backoffice.activities.reception;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import javafx.scene.text.Text;


/**
 *
 * @author David Hello
 * @author Claude Code
 */
final class ReceptionActivity extends ViewDomainActivityBase {

    @Override
    public Node buildUi() {
        return new Text("Reception");
    }

}
