package one.modality.hotel.backoffice.activities.household.gantt.canvas;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import one.modality.event.backoffice.events.ganttcanvas.EventsGanttCanvas;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;

/**
 * Combined view that shows EventsGanttCanvas on top and HouseholdGanttCanvas below.
 * <p>
 * This follows the pattern from AccommodationActivity where the events gantt is shown
 * above the main content. The EventsGanttCanvas manages the day display, and the
 * HouseholdGanttCanvas is horizontally aligned with it via FXGanttTimeWindow pairing.
 * <p>
 * Layout:
 * ┌─────────────────────────────────┐
 * │   EventsGanttCanvas (Events)    │ <- Top (manages days display)
 * ├─────────────────────────────────┤
 * │   HouseholdGanttCanvas (Rooms)  │ <- Center (aligned with events)
 * └─────────────────────────────────┘
 *
 * @author Claude Code Assistant
 */
public class HouseholdGanttWithEventsView {

    private final BorderPane container;

    /**
     * Constructor initializes both gantt canvases.
     *
     * @param pm The presentation model
     */
    public HouseholdGanttWithEventsView(AccommodationPresentationModel pm) {
        // Initialize events gantt (shown on top)
        EventsGanttCanvas eventsGanttCanvas = new EventsGanttCanvas();

        // Initialize household gantt (shown below)
        HouseholdCanvasGanttView householdCanvasGanttView = new HouseholdCanvasGanttView(pm);

        // Create container layout
        this.container = new BorderPane();

        // Set up layout: events on top, household in center
        container.setTop(eventsGanttCanvas.getCanvasContainer());
        container.setCenter(householdCanvasGanttView.getNode());
    }

    /**
     * Returns the root container node.
     *
     * @return The BorderPane containing both gantt canvases
     */
    public Node getNode() {
        return container;
    }

}
