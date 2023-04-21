package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import javafx.scene.text.Text;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentViewDomainActivity;

final class AccommodationActivity extends OrganizationDependentViewDomainActivity implements
        OperationActionFactoryMixin {

    @Override
    public Node buildUi() {
        return new Text("Accommodation");
    }

}
