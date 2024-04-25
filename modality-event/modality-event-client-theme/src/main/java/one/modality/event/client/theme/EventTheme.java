package one.modality.event.client.theme;

import dev.webfx.extras.theme.luminance.FXLuminanceMode;
import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;


/**
 * @author Bruno Salmon
 */
public final class EventTheme { // Used for canvas operations only so far but should implement Theme later for Nodes

    private static final Color SELECTED_COLOR = Color.rgb(0, 150, 214);
    private static final Color LIGHT_MODE_EVENT_COLOR = Color.rgb(154, 77, 152);
    private static final Color DARK_MODE_EVENT_COLOR = Color.rgb(123, 61, 122);
    private static final Color LIGHT_RECURRING_EVENT_COLOR = LIGHT_MODE_EVENT_COLOR.deriveColor(-45, 1, 1, 1);
    private static final Color DARK_RECURRING_EVENT_COLOR = DARK_MODE_EVENT_COLOR.deriveColor(-45, 1, 1, 1);

    public static Color getEventTextColor() {
        return //PaletteMode.isEssentialPalette() ? TextTheme.getTextColor(TextFacetCategory.PRIMARY_TEXT_FACET) :
                Color.WHITE;
    }

    public static Color getEventBackgroundColor(Event event, boolean selected) {
        if (selected)
            return SELECTED_COLOR;

        return FXLuminanceMode.isLightMode() ? LIGHT_MODE_EVENT_COLOR : DARK_MODE_EVENT_COLOR;
    }

    public static Color getRecurringEventDateBackgroundColor(ScheduledItem scheduledItem, boolean selected) {
        if (selected)
            return SELECTED_COLOR;

        return FXLuminanceMode.isLightMode() ? LIGHT_RECURRING_EVENT_COLOR : DARK_RECURRING_EVENT_COLOR;
    }

}
