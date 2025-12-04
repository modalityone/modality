package one.modality.crm.backoffice.activities.operationsandroles;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import javafx.geometry.Insets;
import javafx.scene.control.Label;

/**
 * Route renderers
 *
 * @author Claude Code
 */
final class RouteRenderers {

    static void registerRouteRenderers() {
        // This call ensures the static initializer is called
    }

    static { // Called only once
        // Register route renderer (for displaying grant routes)
        ValueRendererRegistry.registerValueRenderer("routeLabel", (value, context) -> {
            String route = value != null ? value.toString() : "-";
            Label label = new Label(route);
            label.getStyleClass().add("admin-route-label");
            label.setPadding(new Insets(4, 8, 4, 8));
            return label;
        });
    }
}
