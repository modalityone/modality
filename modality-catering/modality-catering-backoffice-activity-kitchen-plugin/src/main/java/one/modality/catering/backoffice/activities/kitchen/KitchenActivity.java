package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.extras.util.control.Controls;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContextMixin;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.catering.backoffice.activities.kitchen.controller.KitchenController;

/**
 * Kitchen Activity - thin coordinator using MVC pattern.
 * Delegates all operations to KitchenController.
 *
 * @author Bruno Salmon
 * @author Claude Code (Refactored to MVC pattern)
 */
final class KitchenActivity extends ViewDomainActivityBase
        implements UiRouteActivityContextMixin<ViewDomainActivityContextFinal>,
        ModalityButtonFactoryMixin {

    private KitchenController controller;

    @Override
    public Node buildUi() {
        dev.webfx.platform.console.Console.log("KitchenActivity.buildUi called");
        controller = new KitchenController(getDataSourceModel());
        controller.initialize();
        Node viewNode = controller.getViewNode();
        dev.webfx.platform.console.Console.log("KitchenActivity.buildUi returning node: " + viewNode);

        // Wrap the entire view in a vertical ScrollPane with padding (like RecurringEventsActivity)
        return Controls.createVerticalScrollPaneWithPadding(10, (Region) viewNode);
    }

    @Override
    protected void startLogic() {
        dev.webfx.platform.console.Console.log("KitchenActivity.startLogic called, controller=" + (controller != null ? "present" : "null"));
        if (controller != null) {
            controller.startLogic();
        }
    }

    @Override
    public void onResume() {
        if (controller != null) {
            controller.onResume();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (controller != null) {
            controller.onPause();
        }
        super.onPause();
    }

}
