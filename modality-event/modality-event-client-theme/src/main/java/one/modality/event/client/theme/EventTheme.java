package one.modality.event.client.theme;

import dev.webfx.extras.theme.luminance.FXLuminanceMode;
import javafx.scene.paint.Color;

/**
 * @author Bruno Salmon
 */
public final class EventTheme { // Used for canvas operations only so far but should implement Theme later for Nodes

    public static Color getEventTextColor() {
        return //PaletteMode.isEssentialPalette() ? TextTheme.getTextColor(TextFacetCategory.PRIMARY_TEXT_FACET) :
                Color.WHITE;
    }

    public static Color getEventBackgroundColor(boolean selected) {
        return  selected ? Color.RED :
                FXLuminanceMode.isLightMode() ? Color.rgb(154, 77, 152) : Color.rgb(123, 61, 122);
    }

    public static Color getEventBorderColor() {
        return Color.BLACK;
    }

}
