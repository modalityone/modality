package one.modality.base.backoffice.formattersrenderers;


import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import one.modality.base.client.bootstrap.ModalityStyle;

public class ModalityBackofficeRenderers {

    private static final String BOOLEAN_BADGE = "booleanBadge";

    public static String getBooleanBadge() {
        return BOOLEAN_BADGE;
    }

    static {
        // Register boolean check renderer (used by both Operations and Routes views)
        ValueRendererRegistry.registerValueRenderer(BOOLEAN_BADGE, (value, context) -> {
            Boolean checked = (Boolean) value;
            // Show checkmark for true, empty for false (avoids inline CSS)
            String symbol = Boolean.TRUE.equals(checked) ? "✓" : "";
            Label check = new Label(symbol);
            check.setPadding(new Insets(4, 8, 4, 8));
            if (Boolean.TRUE.equals(checked)) {
                ModalityStyle.badgeLightSuccess(check);
            } else {
                ModalityStyle.badgeGray(check);
            }
            HBox container = new HBox(check);
            container.setAlignment(Pos.CENTER);
            return container;
        });
    }

}
