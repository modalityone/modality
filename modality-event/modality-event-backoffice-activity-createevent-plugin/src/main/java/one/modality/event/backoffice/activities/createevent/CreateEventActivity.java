package one.modality.event.backoffice.activities.createevent;

import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContextMixin;
import javafx.scene.Node;
import javafx.scene.text.Text;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;

/**
 * @author Bruno Salmon
 */
final class CreateEventActivity extends ViewDomainActivityBase
    implements UiRouteActivityContextMixin<ViewDomainActivityContextFinal>,
    ModalityButtonFactoryMixin,
    OperationActionFactoryMixin {


    @Override
    public Node buildUi() {
        return new Text("Create event");
    }

}
