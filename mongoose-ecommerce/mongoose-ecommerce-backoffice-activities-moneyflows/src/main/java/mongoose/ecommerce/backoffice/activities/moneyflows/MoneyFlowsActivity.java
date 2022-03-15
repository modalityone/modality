package mongoose.ecommerce.backoffice.activities.moneyflows;

import javafx.scene.Node;
import javafx.scene.text.Text;
import mongoose.base.client.activity.organizationdependent.OrganizationDependentViewDomainActivity;

/**
 * @author Bruno Salmon
 */
public class MoneyFlowsActivity extends OrganizationDependentViewDomainActivity {

    @Override
    public Node buildUi() {
        return new Text("Money flows activity");
    }
}
