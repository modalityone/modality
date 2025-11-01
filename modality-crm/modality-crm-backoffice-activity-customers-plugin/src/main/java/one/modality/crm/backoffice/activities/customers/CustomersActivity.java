package one.modality.crm.backoffice.activities.customers;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import javafx.scene.text.Text;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
final class CustomersActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    @Override
    public Node buildUi() {
        return new Text("Customers");
    }

}
