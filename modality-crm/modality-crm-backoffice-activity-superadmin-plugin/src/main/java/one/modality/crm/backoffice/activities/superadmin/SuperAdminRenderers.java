package one.modality.crm.backoffice.activities.superadmin;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Common renderers shared across super admin views.
 *
 * @author Claude Code
 */
final class SuperAdminRenderers {

    private static boolean registered = false;

    /**
     * Register common renderers used by multiple super admin views.
     * This method is idempotent - calling it multiple times is safe.
     */
    static void registerCommonRenderers() {
        if (registered) {
            return;
        }
        registered = true;

        // Register boolean check renderer (used by both Operations and Routes views)
        ValueRendererRegistry.registerValueRenderer("booleanCheck", (value, context) -> {
            Boolean checked = (Boolean) value;
            // Show checkmark for true, empty for false (avoids inline CSS)
            String symbol = Boolean.TRUE.equals(checked) ? "✓" : "";
            Label check = new Label(symbol);
            check.setPadding(new Insets(4, 8, 4, 8));
            if (Boolean.TRUE.equals(checked)) {
                Bootstrap.badgeLightSuccess(check);
            } else {
                Bootstrap.badgeGray(check);
            }
            HBox container = new HBox(check);
            container.setAlignment(Pos.CENTER);
            return container;
        });

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
