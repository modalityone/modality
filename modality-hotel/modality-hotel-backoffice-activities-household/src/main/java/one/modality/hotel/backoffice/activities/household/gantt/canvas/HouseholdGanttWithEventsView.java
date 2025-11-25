package one.modality.hotel.backoffice.activities.household.gantt.canvas;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import one.modality.event.backoffice.events.ganttcanvas.EventsGanttCanvas;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;

/**
 * Combined view that shows EventsGanttCanvas on top and HouseholdGanttCanvas below.
 *
 * This follows the pattern from AccommodationActivity where the events gantt is shown
 * above the main content. The EventsGanttCanvas manages the day display, and the
 * HouseholdGanttCanvas is horizontally aligned with it via FXGanttTimeWindow pairing.
 *
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

    private final EventsGanttCanvas eventsGanttCanvas;
    private final HouseholdCanvasGanttView householdCanvasGanttView;
    private final BorderPane container;

    /**
     * Constructor initializes both gantt canvases.
     *
     * @param pm The presentation model
     */
    public HouseholdGanttWithEventsView(AccommodationPresentationModel pm) {
        // Initialize events gantt (shown on top)
        this.eventsGanttCanvas = new EventsGanttCanvas();

        // Initialize household gantt (shown below)
        this.householdCanvasGanttView = new HouseholdCanvasGanttView(pm);

        // Create container layout
        this.container = new BorderPane();

        // Set up layout: events on top, household in center
        container.setTop(eventsGanttCanvas.getCanvasContainer());
        container.setCenter(householdCanvasGanttView.getNode());
    }

    /**
     * Starts the logic for both canvases.
     *
     * @param mixin The mixin object for reactive chain lifecycle management
     */
    public void startLogic(Object mixin) {
        // Start events gantt logic
        eventsGanttCanvas.setupFXBindingsAndStartLogic(mixin);

        // Start household gantt logic
        householdCanvasGanttView.startLogic(mixin);
    }

    /**
     * Returns the root container node.
     *
     * @return The BorderPane containing both gantt canvases
     */
    public Node getNode() {
        return container;
    }

    /**
     * Gets the household gantt view for direct access if needed.
     *
     * @return The HouseholdCanvasGanttView instance
     */
    public HouseholdCanvasGanttView getHouseholdGanttView() {
        return householdCanvasGanttView;
    }

    /**
     * Gets the events gantt canvas for direct access if needed.
     *
     * @return The EventsGanttCanvas instance
     */
    public EventsGanttCanvas getEventsGanttCanvas() {
        return eventsGanttCanvas;
    }

    /**
     * Cleanup method to prevent memory leaks.
     */
    public void cleanup() {
        householdCanvasGanttView.cleanup();
    }
}
