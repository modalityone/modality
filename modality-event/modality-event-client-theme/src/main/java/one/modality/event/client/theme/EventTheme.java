package one.modality.event.client.theme;

import dev.webfx.extras.theme.luminance.FXLuminanceMode;

import javafx.scene.paint.Color;

import one.modality.base.shared.entities.Event;

/**
 * @author Bruno Salmon
 */
public final
class EventTheme { // Used for canvas operations only so far but should implement Theme later for
    // Nodes

    public static Color getEventTextColor() {
        return // PaletteMode.isEssentialPalette() ?
        // TextTheme.getTextColor(TextFacetCategory.PRIMARY_TEXT_FACET) :
        Color.WHITE;
    }

    public static Color getEventBackgroundColor(Event event, boolean selected) {
        return selected
                ? Color.rgb(0, 150, 214)
                :
                // FXPaletteMode.isVariedPalette() && LocalDate.now().isAfter(event.getEndDate()) ?
                // Color.GRAY :
                // FXPaletteMode.isVariedPalette() && LocalDate.now().isBefore(event.getStartDate())
                // ? Color.ORANGE :
                FXLuminanceMode.isLightMode() ? Color.rgb(154, 77, 152) : Color.rgb(123, 61, 122);
    }

    public static Color getEventBorderColor() {
        return Color.BLACK;
    }
}
