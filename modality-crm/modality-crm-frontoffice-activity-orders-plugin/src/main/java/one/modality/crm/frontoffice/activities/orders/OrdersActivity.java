package one.modality.crm.frontoffice.activities.orders;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;

/**
 * @author David Hello
 */
final class OrdersActivity extends ViewDomainActivityBase implements ModalityButtonFactoryMixin {

    protected void startLogic() {
    }

    @Override
    public Node buildUi() {
        return new BorderPane(new Text("Your orders"));
    }

}
