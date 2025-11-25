package one.modality.hotel.backoffice.activities.household.gantt.canvas;

import dev.webfx.extras.time.layout.bar.LocalDateBar;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.activities.household.gantt.data.HouseholdGanttDataLoader;
import one.modality.hotel.backoffice.activities.household.gantt.adapter.EntityDataAdapter;
import one.modality.hotel.backoffice.activities.household.gantt.model.GanttRoomData;
import one.modality.hotel.backoffice.activities.household.gantt.presenter.GanttPresenter;

import java.util.List;

/**
 * Canvas-based Gantt view implementation for household/housekeeping management.
 *
 * This class serves as a drop-in replacement for the GridPane-based HouseholdGanttView,
 * using the hybrid approach to preserve all existing business logic while gaining
 * Canvas performance benefits.
 *
 * Architecture:
 * - Uses HouseholdGanttCanvas for rendering
 * - Uses HouseholdBarAdapter to convert presenter data to Canvas format
 * - Maintains the same external API as the original HouseholdGanttView
 * - Reuses GanttPresenter for all business logic
 *
 * @author Claude Code Assistant
 */
public class HouseholdCanvasGanttView {

    private final GanttPresenter presenter;
    private final HouseholdGanttCanvas canvas;
    private final HouseholdBarAdapter barAdapter;
    private final HouseholdGanttDataLoader dataLoader;

    // Cache the container node to avoid rebuilding
    private final Node containerNode;

    // Listener references for cleanup
    private final ListChangeListener<Object> resourceConfigListener;
    private final ListChangeListener<Object> attendanceListener;

    /**
     * Constructor initializes the Canvas-based Gantt view.
     *
     * @param pm The presentation model (provides organization context for queries)
     */
    public HouseholdCanvasGanttView(AccommodationPresentationModel pm) {
        // Initialize presenter (handles business logic - PRESERVED from original)
        this.presenter = new GanttPresenter();

        // Initialize Canvas implementation (pass presenter for expand/collapse state)
        this.canvas = new HouseholdGanttCanvas(pm, presenter);

        // Initialize adapter (bridges presenter logic with Canvas format)
        this.barAdapter = new HouseholdBarAdapter(presenter);

        // Initialize data loader (same as original)
        this.dataLoader = new HouseholdGanttDataLoader(pm, presenter);

        // Build and cache the container node
        this.containerNode = canvas.buildCanvasContainer();

        // Sync presenter time window with canvas layout time window (one-time initial sync)
        if (pm.getTimeWindowStart() != null && pm.getTimeWindowEnd() != null) {
            presenter.setDateRange(pm.getTimeWindowStart(), pm.getTimeWindowEnd());
        }

        // IMPORTANT: We DON'T add listeners to time window changes because:
        // 1. The data loader has reactive queries that automatically re-execute when time window changes
        // 2. Those reactive queries update the observable lists (resourceConfigurations, attendances)
        // 3. Our existing listeners on those lists (below) will trigger refreshDisplay() automatically
        // 4. The bidirectional binding already handles layout time window updates
        //
        // Therefore, no additional listeners are needed here - the reactive chain handles everything!

        // Initialize listeners with stored references for cleanup
        this.resourceConfigListener = c -> refreshDisplay();
        this.attendanceListener = c -> refreshDisplay();

        // Listen to data changes and update view
        dataLoader.getResourceConfigurations().addListener(resourceConfigListener);
        dataLoader.getAttendances().addListener(attendanceListener);

        // Listen to expand/collapse state changes
        presenter.getExpandedRoomIds().addListener((javafx.collections.SetChangeListener<String>) change ->
            refreshDisplay()
        );

        // Add click handler for expand/collapse functionality
        setupClickHandler();

        // Initial display refresh (will be empty until data loads, but sets up the canvas)
        // Subsequent refreshes will be triggered by data change listeners
        refreshDisplay();
    }

    /**
     * Sets up click handler for expand/collapse functionality on room headers.
     */
    private void setupClickHandler() {
        javafx.scene.canvas.Canvas canvasNode = canvas.getBarsDrawer().getCanvas();

        // Add mouse moved handler to change cursor when hovering over multi-bed room headers or booking icons
        canvasNode.setOnMouseMoved(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();
            double parentHeaderWidth = canvas.getBarsLayout().getParentHeaderWidth();

            // Check if hovering over a multi-bed room header
            if (mouseX <= parentHeaderWidth) {
                // Adjust Y coordinate for scroll position using layoutOriginY
                // layoutOriginY is positive when scrolled down, so we add it to get absolute Y
                double adjustedY = mouseY + canvas.getBarsDrawer().getLayoutOriginY();

                // Use built-in method to find parent at this Y position
                dev.webfx.extras.time.layout.gantt.impl.ParentRow<?> parentRow =
                    canvas.getBarsLayout().getParentRowAtY(adjustedY);

                if (parentRow != null) {
                    Object parent = parentRow.getParent();
                    if (parent instanceof GanttParentRow) {
                        GanttParentRow ganttParent = (GanttParentRow) parent;
                        if (!ganttParent.isBed() && ganttParent.isMultiBedRoom()) {
                            canvasNode.setCursor(javafx.scene.Cursor.HAND);
                            return;
                        }
                    }
                }
            } else {
                // Check if hovering over a booking bar with an icon
                double adjustedX = mouseX + canvas.getBarsDrawer().getLayoutOriginX();
                double adjustedY = mouseY + canvas.getBarsDrawer().getLayoutOriginY();

                java.util.List<LocalDateBar<HouseholdGanttCanvas.HouseholdBookingBlock>> children = canvas.getBarsLayout().getChildren();
                for (int i = 0; i < children.size(); i++) {
                    LocalDateBar<HouseholdGanttCanvas.HouseholdBookingBlock> bar = children.get(i);
                    dev.webfx.extras.geometry.Bounds bounds = canvas.getBarsLayout().getChildBounds(i);

                    if (bounds != null && bounds.contains(adjustedX, adjustedY)) {
                        // Found a bar - check if hovering over a clickable icon
                        HouseholdGanttCanvas.HouseholdBookingBlock block = bar.getInstance();
                        one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition position = block.getPosition();

                        // Only show hand cursor when hovering directly over icon positions
                        if (position == one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition.ARRIVAL ||
                            position == one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition.SINGLE) {

                            // Calculate icon positions (same as click detection)
                            double dayWidth = canvas.getBarsLayout().getTimeProjector().timeToX(
                                java.time.LocalDate.now().plusDays(1), true, false) -
                                canvas.getBarsLayout().getTimeProjector().timeToX(
                                java.time.LocalDate.now(), true, false);
                            double halfDayWidth = dayWidth / 2;
                            double relativeX = adjustedX - bounds.getMinX();

                            // Icon sizes (no padding) - scaled by 60% and 42% respectively
                            double personIconWidth = HouseholdGanttIcons.PERSON_ICON_SVG_WIDTH * 0.6;
                            double commentIconWidth = HouseholdGanttIcons.MESSAGE_ICON_SVG_WIDTH * 0.42;

                            // Person icon at 7/12 day from bar start (1/3 + 1/4 = 7/12)
                            double personIconStart = dayWidth * 7 / 12;
                            double personIconEnd = personIconStart + personIconWidth;

                            // Comment icon is rendered centered at halfDayWidth + 3, plus 1/2 day adjustment
                            // Account for centering: icon spans from center - width/2 to center + width/2
                            double commentIconCenter = halfDayWidth + 3 + (dayWidth / 2);
                            double commentIconStart = commentIconCenter - (commentIconWidth / 2);
                            double commentIconEnd = commentIconCenter + (commentIconWidth / 2);

                            // Check if hovering over either icon
                            if ((block.hasComments() && relativeX >= commentIconStart && relativeX <= commentIconEnd) ||
                                (relativeX >= personIconStart && relativeX <= personIconEnd)) {
                                canvasNode.setCursor(javafx.scene.Cursor.HAND);
                                return;
                            }
                        }
                        break;
                    }
                }
            }
            canvasNode.setCursor(javafx.scene.Cursor.DEFAULT);
        });

        // Add click handler on the canvas to detect clicks on room headers AND booking bar icons
        canvasNode.setOnMouseClicked(event -> {
            double clickX = event.getX();
            double clickY = event.getY();

            // Calculate total header width (parent header area)
            double parentHeaderWidth = canvas.getBarsLayout().getParentHeaderWidth();

            // Adjust Y coordinate for scroll position (CRITICAL!)
            // layoutOriginY is positive when scrolled down, so we add it to get absolute Y
            double layoutOriginY = canvas.getBarsDrawer().getLayoutOriginY();
            double adjustedY = clickY + layoutOriginY;

            if (clickX > parentHeaderWidth) {
                // Click is in the bars area - check if clicking on an icon
                handleBarIconClick(clickX, clickY, adjustedY, event);
                return;
            }

            // Click is in the parent header area (room/bed names on the left)
            // Use built-in method to find the parent row at this Y coordinate
            // This handles all the complex Y-position calculations including category headers
            dev.webfx.extras.time.layout.gantt.impl.ParentRow<?> parentRow =
                canvas.getBarsLayout().getParentRowAtY(adjustedY);

            if (parentRow == null) {
                return;
            }

            // Get the actual parent object (GanttParentRow)
            Object parent = parentRow.getParent();
            if (!(parent instanceof GanttParentRow)) {
                return;
            }

            GanttParentRow ganttParent = (GanttParentRow) parent;

            // Only toggle if this is a room row (not a bed row) and it's multi-bed
            if (!ganttParent.isBed() && ganttParent.isMultiBedRoom()) {
                presenter.toggleRoomExpanded(ganttParent.getRoom().getId());
            }
        });
    }

    /**
     * Handles clicks on booking bar icons (person icon and special needs icon).
     * Detects which bar and icon was clicked, then shows appropriate tooltip.
     */
    private void handleBarIconClick(double clickX, double clickY, double adjustedY, javafx.scene.input.MouseEvent event) {
        // Adjust coordinates for scroll offset
        double adjustedX = clickX + canvas.getBarsDrawer().getLayoutOriginX();

        // Icon sizes for hit detection (no padding - icon only) - scaled by 60% and 42% respectively
        double personIconWidth = HouseholdGanttIcons.PERSON_ICON_SVG_WIDTH * 0.6;
        double commentIconWidth = HouseholdGanttIcons.MESSAGE_ICON_SVG_WIDTH * 0.42;

        // Iterate through all bars to find one that contains this point
        java.util.List<LocalDateBar<HouseholdGanttCanvas.HouseholdBookingBlock>> children = canvas.getBarsLayout().getChildren();

        for (int i = 0; i < children.size(); i++) {
            LocalDateBar<HouseholdGanttCanvas.HouseholdBookingBlock> bar = children.get(i);
            dev.webfx.extras.geometry.Bounds bounds = canvas.getBarsLayout().getChildBounds(i);

            if (bounds != null && bounds.contains(adjustedX, adjustedY)) {
                // Found the clicked bar
                HouseholdGanttCanvas.HouseholdBookingBlock block = bar.getInstance();
                one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition position = block.getPosition();

                // For ARRIVAL or SINGLE position bars, check which icon was clicked
                if (position == one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition.ARRIVAL ||
                    position == one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition.SINGLE) {

                    // Calculate day width for icon positioning
                    double dayWidth = canvas.getBarsLayout().getTimeProjector().timeToX(
                        java.time.LocalDate.now().plusDays(1), true, false) -
                        canvas.getBarsLayout().getTimeProjector().timeToX(
                        java.time.LocalDate.now(), true, false);
                    double halfDayWidth = dayWidth / 2;

                    // Calculate relative X position within the bar
                    double relativeX = adjustedX - bounds.getMinX();

                    // Person icon is positioned at 7/12 day from bar start (1/3 + 1/4 = 7/12)
                    double personIconStart = dayWidth * 7 / 12;
                    double personIconEnd = personIconStart + personIconWidth;

                    // Comment icon is rendered centered at halfDayWidth + 3, plus 1/2 day adjustment
                    // Account for centering: icon spans from center - width/2 to center + width/2
                    double commentIconCenter = halfDayWidth + 3 + (dayWidth / 2);
                    double commentIconStart = commentIconCenter - (commentIconWidth / 2);
                    double commentIconEnd = commentIconCenter + (commentIconWidth / 2);

                    // Check which icon was clicked (prioritize comment icon if both present)
                    if (block.hasComments() && relativeX >= commentIconStart && relativeX <= commentIconEnd) {
                        // Click is within comment icon bounds
                        showSpecialNeedsTooltip(event, block.getBookingData());
                        return;
                    } else if (relativeX >= personIconStart && relativeX <= personIconEnd) {
                        // Click is within person icon bounds
                        showGuestInfoTooltip(event, block.getBookingData());
                        return;
                    }
                }
                // MIDDLE position - comment icon only (old logic, kept for compatibility)
                else if (block.hasComments() &&
                         position == one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition.MIDDLE) {
                    showSpecialNeedsTooltip(event, block.getBookingData());
                    return;
                }

                return;  // Found the bar, stop searching
            }
        }
    }

    /**
     * Shows a tooltip with guest information when person icon is clicked.
     * Replicates the behavior from the old GridPane-based gantt view.
     */
    private void showGuestInfoTooltip(javafx.scene.input.MouseEvent event,
                                      one.modality.hotel.backoffice.activities.household.gantt.model.GanttBookingData booking) {
        if (booking == null) {
            return;
        }

        // Create tooltip content
        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip();

        StringBuilder content = new StringBuilder();
        content.append("Guest Information\n\n");

        if (booking.getFirstName() != null && booking.getLastName() != null) {
            content.append("First Name: ").append(booking.getFirstName()).append("\n");
            content.append("Last Name: ").append(booking.getLastName()).append("\n");
        } else if (booking.getGuestName() != null) {
            content.append("Guest: ").append(booking.getGuestName()).append("\n");
        }

        if (booking.getGender() != null) {
            content.append("Gender: ").append(booking.getGender()).append("\n");
        }

        if (booking.getEvent() != null) {
            content.append("Event: ").append(booking.getEvent()).append("\n");
        }

        content.append("\nCheck-in: ").append(booking.getStartDate());
        content.append("\nCheck-out: ").append(booking.getEndDate());

        tooltip.setText(content.toString());
        tooltip.setStyle("-fx-font-size: 12px;");

        // Show tooltip at mouse position (using screen coordinates from click event)
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        double tooltipX = event.getScreenX() + 10; // 10px offset to the right
        double tooltipY = event.getScreenY();
        tooltip.show(source, tooltipX, tooltipY);

        // Auto-hide after 5 seconds
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(5));
        delay.setOnFinished(e -> tooltip.hide());
        delay.play();
    }

    /**
     * Shows a tooltip with special needs when comment icon is clicked.
     * Replicates the behavior from the old GridPane-based gantt view.
     */
    private void showSpecialNeedsTooltip(javafx.scene.input.MouseEvent event,
                                        one.modality.hotel.backoffice.activities.household.gantt.model.GanttBookingData booking) {
        if (booking == null) {
            return;
        }

        // Create tooltip content
        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip();

        StringBuilder content = new StringBuilder();
        content.append("Guest: ").append(booking.getGuestName()).append("\n");
        content.append(booking.getStartDate()).append(" - ").append(booking.getEndDate()).append("\n\n");

        if (booking.getComments() != null && !booking.getComments().isEmpty()) {
            content.append("Special Needs:\n");
            content.append(booking.getComments());
        }

        tooltip.setText(content.toString());
        tooltip.setStyle("-fx-font-size: 12px;");

        // Show tooltip at mouse position (using screen coordinates from click event)
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        double tooltipX = event.getScreenX() + 10; // 10px offset to the right
        double tooltipY = event.getScreenY();
        tooltip.show(source, tooltipX, tooltipY);

        // Auto-hide after 5 seconds
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(5));
        delay.setOnFinished(e -> tooltip.hide());
        delay.play();
    }

    /**
     * Starts the data loading logic.
     * Must be called after construction to begin loading data.
     *
     * @param mixin The mixin object for reactive chain lifecycle management
     */
    public void startLogic(Object mixin) {
        dataLoader.startLogic(mixin);
    }

    /**
     * Refreshes the display with current data.
     * Called when data changes or expand/collapse state changes.
     */
    private void refreshDisplay() {
        // Step 1: Convert database entities to gantt model using adapter pattern
        List<GanttRoomData> rooms = EntityDataAdapter.adaptRooms(
            dataLoader.getResourceConfigurations(),
            dataLoader.getAttendances()
        );

        // Step 2: Convert rooms to Canvas parent rows and bars using HouseholdBarAdapter
        // CRITICAL: Adapter creates parent rows AND bars together to ensure they reference same objects
        HouseholdBarAdapter.AdaptedRoomData adapted = barAdapter.adaptAllRoomsWithParents(rooms);

        // Step 3: Update Canvas with parent rows and bars
        try {
            displayRoomsAndBars(adapted.getParentRows(), adapted.getBars());
        } catch (Exception e) {
            // Catch any rendering errors to prevent UI crash
            System.err.println("[HouseholdCanvasGanttView] Error displaying bars: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Displays rooms and bars in the Canvas.
     *
     * CRITICAL: The GanttLayout needs both parents AND children to work correctly.
     * - Parents: Room/bed headers displayed on the left (GanttParentRow objects)
     * - Children: Booking bars displayed in the timeline
     *
     * Parent rows are created by the adapter to ensure bars reference the correct parent objects.
     *
     * @param parentRows The parent rows (rooms and/or beds)
     * @param bars The booking bars (children)
     */
    private void displayRoomsAndBars(List<GanttParentRow> parentRows, List<LocalDateBar<HouseholdGanttCanvas.HouseholdBookingBlock>> bars) {
        // STEP 1: Set the parent rows in the layout
        canvas.getBarsLayout().getParents().setAll(parentRows);

        // STEP 2: Set the booking bars as children
        canvas.getBarsLayout().getChildren().clear();
        canvas.getBarsLayout().getChildren().addAll(bars);

        // STEP 3: Mark layout as dirty to trigger redraw
        canvas.getBarsLayout().markLayoutAsDirty();

        // STEP 4: Mark the drawer as dirty to ensure canvas is redrawn
        canvas.getBarsDrawer().markDrawAreaAsDirty();
    }

    /**
     * Returns the root container node for embedding in the UI.
     *
     * @return The root JavaFX Node containing the entire gantt chart UI
     */
    public Node getNode() {
        return containerNode;
    }

    /**
     * Gets the presenter (for advanced customization if needed).
     *
     * @return The GanttPresenter instance managing business logic
     */
    public GanttPresenter getPresenter() {
        return presenter;
    }

    /**
     * Cleanup method to remove listeners and prevent memory leaks.
     */
    public void cleanup() {
        // Remove listeners from data loader's observable lists
        if (resourceConfigListener != null) {
            dataLoader.getResourceConfigurations().removeListener(resourceConfigListener);
        }
        if (attendanceListener != null) {
            dataLoader.getAttendances().removeListener(attendanceListener);
        }
    }
}
