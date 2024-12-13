package one.modality.event.backoffice.activities.pricing;


import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import javafx.scene.text.Text;

/**
 * @author Bruno Salmon
 */
final class EventPricingActivity extends ViewDomainActivityBase {

    @Override
    public Node buildUi() {
        return new Text("Event pricing");
    }
}
