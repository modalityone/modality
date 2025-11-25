package one.modality.hotel.backoffice.activities.household.gantt.canvas;

import dev.webfx.extras.canvas.bar.BarDrawer;
import dev.webfx.extras.geometry.Bounds;
import dev.webfx.extras.time.layout.bar.LocalDateBar;
import dev.webfx.extras.time.layout.canvas.LocalDateCanvasDrawer;
import dev.webfx.extras.time.layout.canvas.TimeCanvasUtil;
import dev.webfx.extras.time.layout.gantt.LocalDateGanttLayout;
import dev.webfx.extras.time.layout.gantt.canvas.ParentsCanvasDrawer;
import dev.webfx.extras.time.projector.TimeProjector;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.gantt.fx.highlight.FXGanttHighlight;
import one.modality.base.client.gantt.fx.selection.FXGanttSelection;
import one.modality.base.client.gantt.fx.timewindow.FXGanttTimeWindow;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.activities.household.gantt.model.BookingStatus;
import one.modality.hotel.backoffice.activities.household.gantt.model.GanttRoomData;
import one.modality.hotel.backoffice.activities.household.gantt.renderer.GanttColorScheme;

import java.time.LocalDate;

/**
 * Canvas-based Gantt chart for household/housekeeping management.
 * Replaces the GridPane-based implementation with high-performance Canvas rendering.
 *
 * This implementation follows the patterns from AccommodationGantt and AttendanceGantt,
 * using LocalDateGanttLayout for positioning and BarDrawer for rendering.
 *
 * Architecture:
 * - LocalDateGanttLayout: Positions bars in time (handles date-based X coordinates)
 * - ParentsCanvasDrawer: Renders parent (room) and grandparent (category) headers
 * - LocalDateCanvasDrawer: Renders booking bars
 * - VirtualCanvasPane: Optimizes scrolling performance for large room lists
 * - BarDrawer: Reusable utility for drawing bars with rounded corners, text, icons
 *
 * @author Claude Code Assistant
 */
public class HouseholdGanttCanvas {

    private static final double ROW_HEIGHT = 34;
    private static final double BAR_HEIGHT = 18;  // Bars are centered vertically within ROW_HEIGHT
    private static final double BAR_RADIUS = 10;
    private static final double ROOM_HEADER_WIDTH = 130;

    // Presentation model for binding time window
    private final AccommodationPresentationModel pm;

    // Layout infrastructure - positions bars in time
    protected final LocalDateGanttLayout<LocalDateBar<HouseholdBookingBlock>> barsLayout =
            new LocalDateGanttLayout<LocalDateBar<HouseholdBookingBlock>>()
                    .setChildFixedHeight(ROW_HEIGHT)
                    .setChildParentReader(bar -> bar.getInstance().getParentRow())
                    .setParentGrandparentReader(parent -> ((GanttParentRow) parent).getCategory())
                    .setParentHeaderHeight(ROW_HEIGHT)
                    .setGrandparentHeaderHeight(ROW_HEIGHT)  // Category header takes full row height
                    .setGrandparentHeaderPosition(dev.webfx.extras.time.layout.gantt.HeaderPosition.TOP)  // Category headers on top (horizontal)
                    .setParentHeaderPosition(dev.webfx.extras.time.layout.gantt.HeaderPosition.LEFT)  // Room/bed names on left
                    .setParentHeaderWidth(ROOM_HEADER_WIDTH)
                    .setTetrisPacking(false) // Disable tetris packing - each bed is its own parent now
                    .setHSpacing(2)  // Horizontal spacing between bars
                    .setVSpacing(0.6)  // Vertical spacing between rows
                    .setParentsProvided(true); // We provide the parent list explicitly (all rooms/beds)

    // Canvas drawer - draws bars using drawBar() method
    protected final LocalDateCanvasDrawer<LocalDateBar<HouseholdBookingBlock>> barsDrawer =
            new LocalDateCanvasDrawer<>(barsLayout, this::drawBar)
                    // Enable canvas interaction (user can move & zoom in/out the time window)
                    .enableCanvasInteraction();

    // Parents drawer - draws room and category headers
    // IMPORTANT: This must be a field (not local variable) so it stays alive and continues drawing
    public final ParentsCanvasDrawer parentsCanvasDrawer = ParentsCanvasDrawer.create(barsLayout, barsDrawer)
            .setParentDrawer(this::drawParentRoom)
            .setGrandparentDrawer(this::drawGrandparentCategory)
            // No child row header drawer - beds are now parents themselves when expanded
            .setHorizontalStroke(Color.rgb(220, 220, 220))
            .setVerticalStroke(Color.rgb(233, 233, 233), false)
            .setTetrisAreaFill(Color.rgb(243, 243, 243));

    // BarDrawer instances - reusable for all bars/headers
    protected final BarDrawer bookingBarDrawer = new BarDrawer()
            .setTextFill(Color.WHITE)
            .setRadius(BAR_RADIUS);

    protected final BarDrawer parentRoomDrawer = new BarDrawer()
            .setBackgroundFill(Color.WHITE)
            .setStroke(Color.grayRgb(130))
            .setTextFill(Color.BLACK)
            .setTextAlignment(TextAlignment.LEFT);

    protected final BarDrawer grandparentCategoryDrawer = new BarDrawer()
            .setStroke(Color.grayRgb(130))
            .setBackgroundFill(Color.WHITE)
            .setTextAlignment(TextAlignment.CENTER)
            .setTextFill(GanttColorScheme.COLOR_BLUE);

    // Color scheme for styling
    private final GanttColorScheme colorScheme = new GanttColorScheme();

    // Presenter for expand/collapse state
    private final one.modality.hotel.backoffice.activities.household.gantt.presenter.GanttPresenter presenter;

    // Canvas-based tooltip (replaces JavaFX Tooltip which doesn't compile with WebFX)
    private CanvasTooltip canvasTooltip;

    public HouseholdGanttCanvas(AccommodationPresentationModel pm, one.modality.hotel.backoffice.activities.household.gantt.presenter.GanttPresenter presenter) {
        this.pm = pm;
        this.presenter = presenter;

        // Binding the presentation model and the barsLayout time window
        barsLayout.bindTimeWindowBidirectional(pm);

        // Telling the bars layout how to read start & end times of bars (CRITICAL!)
        dev.webfx.extras.time.layout.bar.TimeBarUtil.setBarsLayoutTimeReaders(barsLayout);

        // Configure layout for bed display
        // Grandparent (room type) = TOP (horizontal header spanning category)
        // Parent (room name) = LEFT (vertical list in left column)
        // Child rows (bed labels) = drawn by drawBedRow, should appear in same column as parents
        barsLayout
                .setGrandparentHeaderPosition(dev.webfx.extras.time.layout.gantt.HeaderPosition.TOP)
                .setParentHeaderPosition(dev.webfx.extras.time.layout.gantt.HeaderPosition.LEFT);

        // Add day backgrounds and grid lines before drawing bars
        barsDrawer.addOnBeforeDraw(this::drawDayBackgroundsAndGrid);

        // Add overbooking row backgrounds before drawing bars
        barsDrawer.addOnBeforeDraw(this::drawOverbookingRowBackgrounds);

        // Pairing this Gantt canvas with the referent one (ie the event Gantt canvas on top), so it always stays
        // horizontally aligned with the event Gantt dates, even when this canvas is horizontally shifted
        FXGanttTimeWindow.setupPairedTimeProjectorWhenReady(barsLayout, barsDrawer.getCanvas());

        // Add day highlight (shows today column)
        FXGanttHighlight.addDayHighlight(barsLayout, barsDrawer);

        // Redrawing the canvas when Gantt selected object changes
        FXProperties.runOnPropertyChange(barsDrawer::markDrawAreaAsDirty, FXGanttSelection.ganttSelectedObjectProperty());

        // Initialize canvas-based tooltip and add it as an overlay layer (drawn last, on top of everything)
        canvasTooltip = new CanvasTooltip(barsDrawer.getCanvas(), barsDrawer::markDrawAreaAsDirty);
        barsDrawer.addOnAfterDraw(() -> canvasTooltip.drawTooltip(barsDrawer.getCanvas().getGraphicsContext2D()));
    }

    /**
     * Gets the bars layout for direct manipulation if needed.
     */
    public LocalDateGanttLayout<LocalDateBar<HouseholdBookingBlock>> getBarsLayout() {
        return barsLayout;
    }

    /**
     * Gets the bars drawer for canvas manipulation if needed.
     */
    public LocalDateCanvasDrawer<LocalDateBar<HouseholdBookingBlock>> getBarsDrawer() {
        return barsDrawer;
    }

    /**
     * Gets the canvas-based tooltip for showing information overlays.
     * This replaces JavaFX Tooltip which doesn't compile with WebFX/GWT.
     */
    public CanvasTooltip getCanvasTooltip() {
        return canvasTooltip;
    }

    /**
     * Builds the canvas container with scrolling support.
     * Uses VirtualCanvasPane for optimal performance with large datasets.
     */
    public Node buildCanvasContainer() {
        // Embed everything in a scrollPane for vertical scrolling
        // (horizontal scrolling is managed by the interactive canvas itself)
        ScrollPane scrollPane = new ScrollPane();

        // We embed the canvas in a VirtualCanvasPane which has 2 functions:
        // 1) As a CanvasPane it is responsible for automatically resizing the canvas when the user resizes the UI
        // 2) VirtualCanvasPane keeps the canvas size as small as possible when used in a scrollPane to prevent memory
        //    overflow. The real canvas will be only the size of the scrollPane viewport
        javafx.scene.layout.Region virtualCanvasPane = TimeCanvasUtil.createTimeVirtualCanvasPane(
                barsLayout, barsDrawer,
                scrollPane.viewportBoundsProperty(),
                scrollPane.vvalueProperty());

        // Set up the scrollPane for vertical scrolling only (no horizontal scrollbar)
        Controls.setupVerticalScrollPane(scrollPane, virtualCanvasPane);
        return scrollPane;
    }

    /**
     * Draws a category header (grandparent).
     * Categories are displayed as vertical headers on the left side.
     */
    protected void drawGrandparentCategory(String category, Bounds b, GraphicsContext gc) {
        grandparentCategoryDrawer
                .setMiddleText(category)
                .drawBar(b, gc);
    }

    /**
     * Draws a parent row (either room or bed).
     * - For room rows: Shows expand/collapse arrow (if multi-bed), status dot, room name, action icon
     * - For bed rows: Shows indented bed label
     *
     * Layout uses FIXED positions to ensure alignment across all rows:
     * - Position 8: Expand arrow (only drawn for multi-bed rooms, but space reserved)
     * - Position 26: Status dot (always at same X regardless of expand arrow)
     * - Position 40: Room name
     * - Right edge - 16: Action icon (⋮)
     */
    protected void drawParentRoom(GanttParentRow parentRow, Bounds b, GraphicsContext gc) {
        GanttRoomData room = parentRow.getRoom();

        // Fixed positions for consistent alignment (based on UX design)
        final double EXPAND_ARROW_X = b.getMinX() + 8;      // Expand arrow position
        final double STATUS_DOT_X = b.getMinX() + 26;       // Status dot center X (after expand arrow space)
        final double ROOM_NAME_X = b.getMinX() + 40;        // Room name start X
        final double ACTION_ICON_X = b.getMinX() + b.getWidth() - 16;  // Action icon X (right side)
        final double centerY = b.getMinY() + b.getHeight() / 2;

        // Different styling for bed rows vs room rows
        if (parentRow.isBed()) {
            // Draw background for bed row - danger color for overbooking, light gray for normal beds
            Color bgColor = parentRow.isOverbooking() ?
                Color.rgb(255, 230, 230) :  // Light red for overbooking
                Color.rgb(248, 248, 248);   // Light gray for normal beds

            parentRoomDrawer
                    .setBackgroundFill(bgColor)
                    .setMiddleText(null)
                    .drawBar(b, gc);

            // Draw bed label with indent - red text for overbooking
            // Bed labels use "└" prefix in blue, then bed name at fixed position
            Color textColor = parentRow.isOverbooking() ?
                Color.rgb(200, 0, 0) :     // Red for overbooking
                Color.rgb(130, 130, 130);  // Gray for normal beds

            gc.setFont(javafx.scene.text.Font.font("System", 11));
            gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
            gc.setTextBaseline(javafx.geometry.VPos.CENTER);

            // Draw indent marker "└" in blue at status dot position
            gc.setFill(Color.web("#0096D6"));
            gc.fillText("└", STATUS_DOT_X - 6, centerY);

            // Draw bed name at room name position for alignment
            gc.setFill(textColor);
            gc.fillText(parentRow.getDisplayName(), ROOM_NAME_X, centerY);
        } else {
            // Draw room row
            parentRoomDrawer
                    .setBackgroundFill(Color.WHITE)
                    .setMiddleText(null)  // Don't draw text yet
                    .drawBar(b, gc);

            // Erase the left side of the stroke rectangle to match UX design
            gc.setFill(Color.WHITE);
            gc.fillRect(b.getMinX(), b.getMinY(), 2, b.getHeight());

            // Draw expand/collapse arrow for multi-bed rooms (at fixed position)
            if (parentRow.isMultiBedRoom()) {
                boolean isExpanded = presenter.isRoomExpanded(room.getId());
                String arrow = isExpanded ? "∨" : "›";  // Down arrow when expanded, right arrow when collapsed

                gc.setFill(Color.web("#333333"));
                gc.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 14));
                gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
                gc.setTextBaseline(javafx.geometry.VPos.CENTER);
                gc.fillText(arrow, EXPAND_ARROW_X, centerY);
            }

            // Draw status dot (at fixed position - always aligned)
            if (room.getStatus() != null) {
                Color statusColor = colorScheme.getRoomStatusColor(room.getStatus());
                double dotRadius = 4;

                gc.setFill(statusColor);
                gc.fillOval(STATUS_DOT_X - dotRadius, centerY - dotRadius, dotRadius * 2, dotRadius * 2);
            }

            // Draw room name (at fixed position - always aligned)
            gc.setFill(Color.BLACK);
            gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
            gc.setTextBaseline(javafx.geometry.VPos.CENTER);
            gc.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 12));
            gc.fillText(room.getName(), ROOM_NAME_X, centerY);

            // Draw action icon (⋮) at right side of cell
            gc.setFill(Color.web("#333333"));
            gc.setFont(javafx.scene.text.Font.font("System", 14));
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.setTextBaseline(javafx.geometry.VPos.CENTER);
            gc.fillText("⋮", ACTION_ICON_X, centerY);

            // Draw room comments if present (small italic text after room name, truncated)
            if (room.getRoomComments() != null && !room.getRoomComments().isEmpty()) {
                // Calculate available width for comment (between room name and action icon)
                double roomNameWidth = room.getName().length() * 7.2;  // Approximate width at 12px bold
                double commentStartX = ROOM_NAME_X + roomNameWidth + 8;
                double availableWidth = ACTION_ICON_X - commentStartX - 10;

                if (availableWidth > 30) {  // Only show if there's reasonable space
                    String comment = room.getRoomComments();
                    // Truncate if needed (approx 6px per char at 10px font)
                    int maxChars = (int) (availableWidth / 6);
                    if (comment.length() > maxChars && maxChars > 3) {
                        comment = comment.substring(0, maxChars - 3) + "...";
                    }
                    gc.setFill(Color.web("#666666"));
                    gc.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontPosture.ITALIC, 10));
                    gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
                    gc.fillText(comment, commentStartX, centerY);
                }
            }
        }
    }

    /**
     * Draws a booking bar.
     * This is called for each bar by LocalDateCanvasDrawer.
     *
     * Booking bars use the Gantt flow pattern:
     * - Bar spans multiple days as a continuous visual element
     * - Rounded corners only on the start/end of the booking
     * - Icons and text are drawn on appropriate days
     * - Bar starts at the MIDDLE of arrival day
     * - Bar ends at the MIDDLE of departure day
     */
    protected void drawBar(LocalDateBar<HouseholdBookingBlock> bar, Bounds b, GraphicsContext gc) {
        HouseholdBookingBlock block = bar.getInstance();

        // Skip drawing placeholder bars for empty beds (used to create bed rows in expanded view)
        if (block.getOccupancy() == 0 && block.getGuestName().isEmpty()) {
            return; // Don't draw anything for empty placeholder bars
        }

        // Adjust bounds to start/end at middle of days
        // Get day width from the time projector
        LocalDate startDate = bar.getStartTime();
        LocalDate dayAfterStart = startDate.plusDays(1);
        double dayWidth = barsLayout.getTimeProjector().timeToX(dayAfterStart, true, false)
                         - barsLayout.getTimeProjector().timeToX(startDate, true, false);
        double halfDayWidth = dayWidth / 2;

        // Create adjusted bounds:
        // - X: shift right by half a day (to middle of arrival day)
        // - Y: center vertically within row using BAR_HEIGHT
        // - Width: reduce by one day width (to end at middle of departure day)
        // - Height: use BAR_HEIGHT constant for consistent bar sizing
        dev.webfx.extras.geometry.MutableBounds adjustedBounds = new dev.webfx.extras.geometry.MutableBounds();
        adjustedBounds.setMinX(b.getMinX() + halfDayWidth);
        adjustedBounds.setMinY(b.getMinY() + (b.getHeight() - BAR_HEIGHT) / 2);  // Center bar vertically
        adjustedBounds.setWidth(b.getWidth() - dayWidth);
        adjustedBounds.setHeight(BAR_HEIGHT);

        // Get bar color based on booking status
        Color barColor = colorScheme.getBookingStatusColor(block.getStatus());

        // Check if this is an aggregate bar (multi-bed room collapsed)
        if (block.isAggregateBar()) {
            // Draw aggregate bar with daily occupancy text for each day
            drawAggregateBar(bar, block, adjustedBounds, barColor, dayWidth, gc);
        } else {
            // Draw regular booking bar with guest name
            drawRegularBookingBar(block, adjustedBounds, barColor, gc);
        }

        // Draw turnover indicator if needed (orange badge at top center)
        if (block.hasTurnover()) {
            drawTurnoverIndicator(adjustedBounds, gc);
        }
    }

    /**
     * Draws a regular booking bar with person icon on first day (no text).
     * Used for single rooms and individual bed bookings in expanded multi-bed rooms.
     * The person icon is clickable to show guest information in a tooltip.
     */
    private void drawRegularBookingBar(HouseholdBookingBlock block, Bounds adjustedBounds, Color barColor, GraphicsContext gc) {
        // Configure the bar drawer with NO TEXT (only icon)
        bookingBarDrawer
                .setBackgroundFill(barColor)
                .setTextFill(Color.WHITE)
                .setStroke(block.hasConflict() ? GanttColorScheme.COLOR_RED : null)
                .setRadius(BAR_RADIUS)
                .setMiddleText(null);  // No text - only icon

        // Always show person icon on first day of booking (ARRIVAL or SINGLE position)
        // The icon is clickable to show guest info tooltip
        one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition position = block.getPosition();

        // Clear any icon from BarDrawer - we'll draw person icon manually with proper scaling
        bookingBarDrawer.setIcon(null, null, 0, 0, null, null, null, 0, 0);

        // Draw the bar with configured properties
        bookingBarDrawer.drawBar(adjustedBounds, gc);

        // Draw person icon for ARRIVAL or SINGLE positions (manually with proper scaling)
        if (position == one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition.ARRIVAL ||
            position == one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition.SINGLE) {

            // Use red icon if late arrival, white otherwise
            Color iconColor = block.hasLateArrival() ?
                HouseholdGanttIcons.PERSON_ICON_RED_FILL :
                HouseholdGanttIcons.PERSON_ICON_SVG_FILL;

            // Scale icon to fit within BAR_HEIGHT with some padding (icon ~80% of bar height)
            double iconSize = BAR_HEIGHT * 0.8;

            // Position icon: left side of bar with padding, vertically centered
            double personIconX = adjustedBounds.getMinX() + 8;
            double personIconY = adjustedBounds.getMinY() + adjustedBounds.getHeight() / 2;

            // Draw person icon using SVG path with proper scaling
            javafx.scene.shape.SVGPath svgPath = new javafx.scene.shape.SVGPath();
            svgPath.setContent(HouseholdGanttIcons.PERSON_ICON_SVG_PATH);

            double iconScaleFactor = iconSize / HouseholdGanttIcons.PERSON_ICON_SVG_HEIGHT;
            gc.save();
            gc.translate(personIconX, personIconY);
            gc.scale(iconScaleFactor, iconScaleFactor);
            gc.translate(-HouseholdGanttIcons.PERSON_ICON_SVG_WIDTH / 2, -HouseholdGanttIcons.PERSON_ICON_SVG_HEIGHT / 2);
            gc.setFill(iconColor);
            gc.beginPath();
            gc.appendSVGPath(HouseholdGanttIcons.PERSON_ICON_SVG_PATH);
            gc.fill();
            gc.restore();
        }

        // Draw comment icon if booking has comments (positioned on second day of booking)
        if (block.hasComments() && (position == one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition.ARRIVAL ||
                                     position == one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition.SINGLE)) {
            // Get day width from the layout
            double dayWidth = barsLayout.getTimeProjector().timeToX(java.time.LocalDate.now().plusDays(1), true, false) -
                              barsLayout.getTimeProjector().timeToX(java.time.LocalDate.now(), true, false);
            double halfDayWidth = dayWidth / 2;

            // Position comment icon at halfDayWidth + 3px padding
            double commentIconX = adjustedBounds.getMinX() + halfDayWidth + 3;
            double commentIconY = adjustedBounds.getMinY() + adjustedBounds.getHeight() / 2;

            // Draw comment icon using SVG path (42% size - 58% reduction total)
            javafx.scene.shape.SVGPath svgPath = new javafx.scene.shape.SVGPath();
            svgPath.setContent(HouseholdGanttIcons.MESSAGE_ICON_SVG_PATH);

            double iconScaleFactor = 0.42; // 0.6 * 0.7 = 0.42 (reduced by additional 30%)
            gc.save();
            gc.translate(commentIconX, commentIconY);
            gc.scale((HouseholdGanttIcons.MESSAGE_ICON_SVG_WIDTH / svgPath.prefWidth(-1)) * iconScaleFactor,
                     (HouseholdGanttIcons.MESSAGE_ICON_SVG_HEIGHT / svgPath.prefHeight(-1)) * iconScaleFactor);
            gc.translate(-svgPath.prefWidth(-1) / 2, -svgPath.prefHeight(-1) / 2);
            gc.setFill(HouseholdGanttIcons.MESSAGE_ICON_SVG_FILL);
            gc.beginPath();
            gc.appendSVGPath(HouseholdGanttIcons.MESSAGE_ICON_SVG_PATH);
            gc.fill();
            gc.restore();
        }
    }

    /**
     * Draws an aggregate bar for collapsed multi-bed rooms.
     * Shows occupancy/capacity text for each day (e.g., "4/5", "5/5", "3/5").
     */
    private void drawAggregateBar(LocalDateBar<HouseholdBookingBlock> bar, HouseholdBookingBlock block,
                                  Bounds adjustedBounds, Color barColor, double dayWidth, GraphicsContext gc) {
        // Draw the bar background (no text, no icons - we'll draw occupancy text per day)
        bookingBarDrawer
                .setBackgroundFill(barColor)
                .setTextFill(Color.WHITE)
                .setStroke(null)
                .setRadius(BAR_RADIUS)
                .setMiddleText(null)  // No centered text
                .setIcon(null, null, 0, 0, null, null, null, 0, 0);  // No icons

        bookingBarDrawer.drawBar(adjustedBounds, gc);

        // Now draw occupancy text for each day
        LocalDate startDate = bar.getStartTime();
        LocalDate endDate = bar.getEndTime();
        java.util.Map<LocalDate, Integer> dailyOccupancy = block.getDailyOccupancy();
        int totalCapacity = block.getTotalCapacity();

        if (dailyOccupancy == null) {
            return;
        }

        // Set up text drawing
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 11));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);

        // Draw occupancy text for each day (including the last day where bar ends at middle)
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // Get occupancy for this day
            // For the last day (check-out day), use previous day's occupancy since guests check out that morning
            Integer occupancy;
            if (currentDate.equals(endDate)) {
                // Last visible portion - show occupancy from previous day (last night stayed)
                LocalDate lastNight = currentDate.minusDays(1);
                occupancy = dailyOccupancy.get(lastNight);
            } else {
                occupancy = dailyOccupancy.get(currentDate);
            }

            if (occupancy != null && occupancy > 0) {
                // Calculate X position for this day
                double dayX = barsLayout.getTimeProjector().timeToX(currentDate, true, false);
                double dayMiddleX;

                // For first day, adjust position to account for bar starting at middle of day
                if (currentDate.equals(startDate)) {
                    // Bar starts at middle of first day, so position text at 3/4 of the day
                    dayMiddleX = dayX + (dayWidth * 0.75);
                } else if (currentDate.equals(endDate)) {
                    // Last day - bar ends at middle, so position text at 1/4 of the day
                    dayMiddleX = dayX + (dayWidth * 0.25);
                } else {
                    // For middle days, center the text
                    dayMiddleX = dayX + dayWidth / 2;
                }

                // Draw occupancy text
                String occupancyText = occupancy + "/" + totalCapacity;
                gc.fillText(occupancyText, dayMiddleX, adjustedBounds.getMinY() + adjustedBounds.getHeight() / 2);
            }

            currentDate = currentDate.plusDays(1);
        }

        // Draw comment icon for each booking in this aggregate bar that has comments
        // Position each icon based on the booking's start date (non-clickable, visual only)
        if (block.getSegmentBookings() != null) {
            for (one.modality.hotel.backoffice.activities.household.gantt.model.GanttBookingData booking : block.getSegmentBookings()) {
                if (booking.getComments() != null && !booking.getComments().isEmpty() &&
                    booking.getStartDate() != null) {

                    // Calculate the X position for this booking's comment icon
                    // The icon should be at the same position as on bed lines
                    LocalDate bookingStart = booking.getStartDate();
                    LocalDate barStart = startDate;

                    // Calculate pixel offset from bar start to booking start
                    double barStartX = barsLayout.getTimeProjector().timeToX(barStart, true, false);
                    double bookingStartX = barsLayout.getTimeProjector().timeToX(bookingStart, true, false);
                    double offsetFromBarStart = bookingStartX - barStartX;

                    // Position comment icon at: offset + halfDayWidth + 3px padding
                    // adjustedBounds.getMinX() is already at middle of bar start day
                    double halfDayWidth = dayWidth / 2;
                    double commentIconX = adjustedBounds.getMinX() + offsetFromBarStart + halfDayWidth + 3;
                    double commentIconY = adjustedBounds.getMinY() + adjustedBounds.getHeight() / 2;

                    // Draw comment icon using SVG path (42% size - same as bed lines)
                    javafx.scene.shape.SVGPath svgPath = new javafx.scene.shape.SVGPath();
                    svgPath.setContent(HouseholdGanttIcons.MESSAGE_ICON_SVG_PATH);

                    double iconScaleFactor = 0.42; // 0.6 * 0.7 = 0.42 (reduced by additional 30%)
                    gc.save();
                    gc.translate(commentIconX, commentIconY);
                    gc.scale((HouseholdGanttIcons.MESSAGE_ICON_SVG_WIDTH / svgPath.prefWidth(-1)) * iconScaleFactor,
                             (HouseholdGanttIcons.MESSAGE_ICON_SVG_HEIGHT / svgPath.prefHeight(-1)) * iconScaleFactor);
                    gc.translate(-svgPath.prefWidth(-1) / 2, -svgPath.prefHeight(-1) / 2);
                    gc.setFill(HouseholdGanttIcons.MESSAGE_ICON_SVG_FILL);
                    gc.beginPath();
                    gc.appendSVGPath(HouseholdGanttIcons.MESSAGE_ICON_SVG_PATH);
                    gc.fill();
                    gc.restore();
                }
            }
        }
    }

    /**
     * Draws a turnover indicator (orange badge with warning triangle) at the top center of a cell.
     */
    private void drawTurnoverIndicator(Bounds b, GraphicsContext gc) {
        double badgeSize = 20;
        double badgeX = b.getMinX() + (b.getWidth() - badgeSize) / 2;
        double badgeY = b.getMinY() + 2;

        // Draw orange rounded rectangle background
        gc.setFill(Color.web("#FFA500"));
        gc.fillRoundRect(badgeX, badgeY, badgeSize, badgeSize, 3, 3);

        // Draw warning triangle icon on top
        gc.save();

        // Position and scale the icon to fit in the badge
        double iconScale = badgeSize / HouseholdGanttIcons.WARNING_ICON_SVG_WIDTH;
        gc.translate(badgeX, badgeY);
        gc.scale(iconScale, iconScale);

        // Draw the SVG path
        gc.setFill(HouseholdGanttIcons.WARNING_ICON_SVG_FILL);
        gc.beginPath();
        gc.appendSVGPath(HouseholdGanttIcons.WARNING_ICON_SVG_PATH);
        gc.fill();

        gc.restore();
    }

    /**
     * Draws day backgrounds with different colors for weekends and today.
     * Also draws vertical grid lines between days.
     * Called before drawing bars to provide visual day separation.
     */
    private void drawDayBackgroundsAndGrid() {
        GraphicsContext gc = barsDrawer.getCanvas().getGraphicsContext2D();
        double canvasHeight = barsDrawer.getCanvas().getHeight();

        // Get time window from layout
        java.time.LocalDate startDate = barsLayout.getTimeWindowStart();
        java.time.LocalDate endDate = barsLayout.getTimeWindowEnd();

        if (startDate == null || endDate == null) {
            return;
        }

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate currentDate = startDate;

        // Draw background for each day
        while (!currentDate.isAfter(endDate)) {
            TimeProjector<LocalDate> timeProjector = barsLayout.getTimeProjector();
            double x = timeProjector.timeToX(currentDate, true, false);
            double nextX = barsLayout.getTimeProjector().timeToX(currentDate.plusDays(1), true, false);
            double dayWidth = nextX - x;

            // Determine background color
            Color bgColor;
            if (currentDate.equals(today)) {
                // Today: light yellow
                bgColor = Color.rgb(255, 255, 224);
            } else if (currentDate.getDayOfWeek() == java.time.DayOfWeek.SATURDAY ||
                       currentDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
                // Weekend: light gray
                bgColor = Color.rgb(245, 245, 245);
            } else {
                // Weekday: white
                bgColor = Color.WHITE;
            }

            // Fill day background
            gc.setFill(bgColor);
            gc.fillRect(x, 0, dayWidth, canvasHeight);

            // Draw vertical grid line at day boundary
            gc.setStroke(Color.rgb(220, 220, 220));
            gc.setLineWidth(0.5);
            gc.strokeLine(x, 0, x, canvasHeight);

            currentDate = currentDate.plusDays(1);
        }

        // Draw final vertical grid line at the end
        double finalX = barsLayout.getTimeProjector().timeToX(endDate.plusDays(1), true, false);
        gc.strokeLine(finalX, 0, finalX, canvasHeight);
    }

    /**
     * Draws danger-colored backgrounds for overbooking situations.
     * For overbooking beds: highlights the entire row (including label cell)
     * For regular rooms with overbooking: highlights only specific day cells where overbooking occurs
     * Called before drawing bars to provide visual indication.
     */
    private void drawOverbookingRowBackgrounds() {
        GraphicsContext gc = barsDrawer.getCanvas().getGraphicsContext2D();
        double canvasWidth = barsDrawer.getCanvas().getWidth();

        // Get scroll offset to adjust Y coordinates
        double layoutOriginY = barsDrawer.getLayoutOriginY();

        // Get time window
        LocalDate timeWindowStart = barsLayout.getTimeWindowStart();
        LocalDate timeWindowEnd = barsLayout.getTimeWindowEnd();
        if (timeWindowStart == null || timeWindowEnd == null) {
            return;
        }

        // Get all parent rows from the layout
        java.util.List<dev.webfx.extras.time.layout.gantt.impl.ParentRow<LocalDateBar<HouseholdBookingBlock>>> parentRows = barsLayout.getParentRows();
        if (parentRows == null || parentRows.isEmpty()) {
            return;
        }

        // Draw danger backgrounds
        for (dev.webfx.extras.time.layout.gantt.impl.ParentRow<LocalDateBar<HouseholdBookingBlock>> parentRow : parentRows) {
            Object parent = parentRow.getParent();
            if (parent instanceof GanttParentRow) {
                GanttParentRow ganttParent = (GanttParentRow) parent;

                // Get the row bounds from the ParentRow
                Bounds rowBounds = parentRow.getHeader();
                if (rowBounds == null) {
                    continue;
                }

                // Adjust Y coordinate for scroll position
                double adjustedY = rowBounds.getMinY() - layoutOriginY;

                // Case 1: Overbooking bed row - highlight entire row (including label cell)
                if (ganttParent.isOverbooking()) {
                    gc.setFill(Color.rgb(255, 200, 200)); // More aggressive red
                    gc.fillRect(0, adjustedY, canvasWidth, rowBounds.getHeight());
                }
                // Case 2: Regular room (single or multi-bed collapsed) - highlight overbooking day cells only
                else if (!ganttParent.isBed()) {
                    // For rooms, check bars to find overbooking situations
                    java.util.List<LocalDateBar<HouseholdBookingBlock>> allBars = barsLayout.getChildren();
                    int roomCapacity = ganttParent.getRoom().getCapacity();

                    // Collect ALL daily occupancy data for this room across all aggregate bars
                    java.util.Map<LocalDate, Integer> combinedOccupancy = new java.util.HashMap<>();
                    boolean hasAggregateBars = false;

                    // First pass: collect all occupancy data from all aggregate bars for this room
                    for (LocalDateBar<HouseholdBookingBlock> bar : allBars) {
                        HouseholdBookingBlock block = bar.getInstance();

                        if (block.getParentRow() == ganttParent && block.isAggregateBar()) {
                            hasAggregateBars = true;
                            if (block.getDailyOccupancy() != null) {
                                // Merge this bar's occupancy data into combined map
                                combinedOccupancy.putAll(block.getDailyOccupancy());
                            }
                        }
                    }

                    // For aggregate bars: draw danger backgrounds based on combined occupancy
                    if (hasAggregateBars && !combinedOccupancy.isEmpty()) {
                        for (java.util.Map.Entry<LocalDate, Integer> entry : combinedOccupancy.entrySet()) {
                            LocalDate date = entry.getKey();
                            int occupancy = entry.getValue();

                            // If occupancy exceeds capacity, draw danger background for this day cell
                            if (occupancy > roomCapacity) {
                                double dayStartX = barsLayout.getTimeProjector().timeToX(date, true, false);
                                double dayEndX = barsLayout.getTimeProjector().timeToX(date.plusDays(1), true, false);
                                double dayWidth = dayEndX - dayStartX;
                                double halfDayWidth = dayWidth / 2;

                                gc.setFill(Color.rgb(255, 200, 200)); // More aggressive red
                                gc.fillRect(dayStartX + halfDayWidth, adjustedY, dayWidth, rowBounds.getHeight());
                            }
                        }
                    }
                    // For single rooms: check for overlapping bookings
                    else if (!hasAggregateBars && roomCapacity == 1) {
                        // Collect all booking bars for this room
                        java.util.Set<LocalDate> overbookedDates = new java.util.HashSet<>();

                        for (LocalDateBar<HouseholdBookingBlock> bar : allBars) {
                            HouseholdBookingBlock block = bar.getInstance();

                            if (block.getParentRow() == ganttParent && !block.isAggregateBar()) {
                                LocalDate startDate = bar.getStartTime();
                                LocalDate endDate = bar.getEndTime();

                                if (startDate != null && endDate != null) {
                                    // For each day in this booking, check if other bookings overlap
                                    LocalDate currentDate = startDate;
                                    while (currentDate.isBefore(endDate)) {
                                        final LocalDate checkDate = currentDate;

                                        // Count how many bookings overlap this date
                                        int overlappingCount = 0;
                                        for (LocalDateBar<HouseholdBookingBlock> otherBar : allBars) {
                                            HouseholdBookingBlock otherBlock = otherBar.getInstance();
                                            if (otherBlock.getParentRow() == ganttParent && !otherBlock.isAggregateBar()) {
                                                LocalDate otherStart = otherBar.getStartTime();
                                                LocalDate otherEnd = otherBar.getEndTime();
                                                if (otherStart != null && otherEnd != null &&
                                                    !checkDate.isBefore(otherStart) && checkDate.isBefore(otherEnd)) {
                                                    overlappingCount++;
                                                }
                                            }
                                        }

                                        // If more than capacity bookings overlap, mark this day as overbooked
                                        if (overlappingCount > roomCapacity) {
                                            overbookedDates.add(checkDate);
                                        }

                                        currentDate = currentDate.plusDays(1);
                                    }
                                }
                            }
                        }

                        // Draw danger backgrounds for all overbooked dates
                        for (LocalDate date : overbookedDates) {
                            double dayStartX = barsLayout.getTimeProjector().timeToX(date, true, false);
                            double dayEndX = barsLayout.getTimeProjector().timeToX(date.plusDays(1), true, false);
                            double dayWidth = dayEndX - dayStartX;
                            double halfDayWidth = dayWidth / 2;

                            gc.setFill(Color.rgb(255, 200, 200)); // More aggressive red
                            gc.fillRect(dayStartX + halfDayWidth, adjustedY, dayWidth, rowBounds.getHeight());
                        }
                    }
                }
            }
        }
    }

    /**
     * Block class that wraps booking information for Canvas rendering.
     * Bridges the existing BookingBar model with Canvas LocalDateBar requirements.
     */
    public static class HouseholdBookingBlock {
        private final String guestName;
        private final BookingStatus status;
        private final one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition position;
        private final boolean hasConflict;
        private final boolean hasComments;
        private final boolean hasTurnover;
        private final boolean hasLateArrival;
        private final one.modality.hotel.backoffice.activities.household.gantt.model.GanttBookingData bookingData;
        private final int occupancy;
        private final int totalCapacity;
        private final java.util.Map<java.time.LocalDate, Integer> dailyOccupancy; // Daily occupancy for aggregate bars
        private final java.util.List<one.modality.hotel.backoffice.activities.household.gantt.model.GanttBookingData> segmentBookings; // All bookings in this segment (for aggregate bars)
        private GanttParentRow parentRow; // Set by adapter for parent/child relationship

        public HouseholdBookingBlock(String guestName, BookingStatus status,
                                     one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition position,
                                     boolean hasConflict, boolean hasComments, boolean hasTurnover,
                                     boolean hasLateArrival,
                                     one.modality.hotel.backoffice.activities.household.gantt.model.GanttBookingData bookingData,
                                     int occupancy, int totalCapacity) {
            this(guestName, status, position, hasConflict, hasComments, hasTurnover, hasLateArrival,
                 bookingData, occupancy, totalCapacity, null, null);
        }

        public HouseholdBookingBlock(String guestName, BookingStatus status,
                                     one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition position,
                                     boolean hasConflict, boolean hasComments, boolean hasTurnover,
                                     boolean hasLateArrival,
                                     one.modality.hotel.backoffice.activities.household.gantt.model.GanttBookingData bookingData,
                                     int occupancy, int totalCapacity,
                                     java.util.Map<java.time.LocalDate, Integer> dailyOccupancy,
                                     java.util.List<one.modality.hotel.backoffice.activities.household.gantt.model.GanttBookingData> segmentBookings) {
            this.guestName = guestName;
            this.status = status;
            this.position = position;
            this.hasConflict = hasConflict;
            this.hasComments = hasComments;
            this.hasTurnover = hasTurnover;
            this.hasLateArrival = hasLateArrival;
            this.bookingData = bookingData;
            this.occupancy = occupancy;
            this.totalCapacity = totalCapacity;
            this.dailyOccupancy = dailyOccupancy;
            this.segmentBookings = segmentBookings;
        }

        public String getGuestName() {
            return guestName;
        }

        public BookingStatus getStatus() {
            return status;
        }

        public one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition getPosition() {
            return position;
        }

        public boolean hasConflict() {
            return hasConflict;
        }

        public boolean hasComments() {
            return hasComments;
        }

        public boolean hasTurnover() {
            return hasTurnover;
        }

        public boolean hasLateArrival() {
            return hasLateArrival;
        }

        public one.modality.hotel.backoffice.activities.household.gantt.model.GanttBookingData getBookingData() {
            return bookingData;
        }

        public int getOccupancy() {
            return occupancy;
        }

        public int getTotalCapacity() {
            return totalCapacity;
        }

        public GanttParentRow getParentRow() {
            return parentRow;
        }

        public void setParentRow(GanttParentRow parentRow) {
            this.parentRow = parentRow;
        }

        public boolean isMultiOccupancy() {
            return totalCapacity > 1;
        }

        public java.util.Map<java.time.LocalDate, Integer> getDailyOccupancy() {
            return dailyOccupancy;
        }

        public boolean isAggregateBar() {
            return dailyOccupancy != null;
        }

        public java.util.List<one.modality.hotel.backoffice.activities.household.gantt.model.GanttBookingData> getSegmentBookings() {
            return segmentBookings;
        }
    }
}
