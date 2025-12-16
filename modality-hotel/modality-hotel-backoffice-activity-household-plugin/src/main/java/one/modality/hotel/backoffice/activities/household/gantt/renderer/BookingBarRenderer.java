package one.modality.hotel.backoffice.activities.household.gantt.renderer;

import javafx.scene.Node;
import one.modality.hotel.backoffice.activities.household.gantt.model.BookingBar;

/**
 * Interface for rendering booking bars.
 * Allows different rendering strategies for single vs multi-bed rooms.
 *
 * @author Claude Code Assistant
 */
public interface BookingBarRenderer {
    /**
     * Renders a booking bar as a JavaFX node.
     *
     * @param bar The booking bar data
     * @param cellWidth The width of the containing cell
     * @param cellHeight The height of the containing cell
     * @return The rendered node
     */
    Node render(BookingBar bar, double cellWidth, double cellHeight);
}
