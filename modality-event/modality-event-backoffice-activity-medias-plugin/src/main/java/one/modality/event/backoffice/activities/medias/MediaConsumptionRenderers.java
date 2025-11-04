package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import one.modality.event.backoffice.activities.medias.MediaDashboardTabView.UserConsumptionData;

/**
 * Custom renderers for the Media Dashboard consumption views.
 *
 * @author Claude Code
 */
final class MediaConsumptionRenderers {

    static void registerRenderers() {
        // Register the consumption type badge renderer
        ValueRendererRegistry.registerValueRenderer("consumptionType", (value, context) -> {
            UserConsumptionData userData = (UserConsumptionData) value;

            HBox container = new HBox();
            container.setAlignment(Pos.CENTER);

            Label badge = new Label(userData.getTypeLabel());
            badge.setPadding(new Insets(3, 8, 3, 8));

            // Apply appropriate badge styling based on consumption type
            if (userData.hasLivestreamed && userData.hasRecorded) {
                // BOTH - info style (light blue)
                Bootstrap.badge(badge, Bootstrap.INFO);
            } else if (userData.hasLivestreamed) {
                // LIVE - primary style (blue)
                Bootstrap.primaryBadge(badge);
            } else {
                // RECORDING - success style (green)
                Bootstrap.successBadge(badge);
            }

            container.getChildren().add(badge);
            return container;
        });

        // Register the duration formatter renderer
        ValueRendererRegistry.registerValueRenderer("durationMinutes", (value, context) -> {
            UserConsumptionData userData = (UserConsumptionData) value;
            Label label = new Label(userData.getFormattedDuration());
            label.setAlignment(Pos.CENTER_LEFT);
            return label;
        });
    }
}
