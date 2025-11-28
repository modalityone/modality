package one.modality.hotel.backoffice.activities.household.gantt.canvas;

import dev.webfx.extras.canvas.bar.BarDrawer;
import dev.webfx.extras.geometry.Bounds;
import dev.webfx.extras.geometry.MutableBounds;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.gantt.fx.highlight.FXGanttHighlight;
import one.modality.base.client.gantt.fx.selection.FXGanttSelection;
import one.modality.base.client.gantt.fx.timewindow.FXGanttTimeWindow;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.activities.household.gantt.model.BookingStatus;
import one.modality.hotel.backoffice.activities.household.gantt.renderer.GanttColorScheme;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Canvas-based Gantt chart for household/housekeeping management.
 * Replaces the GridPane-based implementation with high-performance Canvas rendering.
 * <p>
 * This implementation follows the patterns from AccommodationGantt and AttendanceGantt,
 * using LocalDateGanttLayout for positioning and BarDrawer for rendering.
 * <p>
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
    private static final double ROOM_HEADER_WIDTH = 180;  // Increased from 130 for better room name display

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

    // BarDrawer instances - reusable for all bars/headers
    protected final BarDrawer bookingBarDrawer = new BarDrawer()
            .setTextFill(Color.WHITE)
            .setRadius(BAR_RADIUS);

    // BarDrawer for parent room headers (left column)
    protected final BarDrawer parentRoomDrawer = new BarDrawer()
            .setBackgroundFill(Color.WHITE)
            .setStroke(Color.grayRgb(130))
            .setTextFill(Color.BLACK)
            .setTextAlignment(TextAlignment.LEFT)
            .setTextFont(Font.font("System", FontWeight.BOLD, 11));

    // BarDrawer for grandparent room type headers (top row) - light grey background, white text
    protected final BarDrawer grandparentRoomTypeDrawer = new BarDrawer()
            .setStroke(Color.grayRgb(130))
            .setBackgroundFill(Color.web("#9CA3AF"))  // Light grey (matches active filter buttons)
            .setTextAlignment(TextAlignment.CENTER)
            .setTextFill(Color.WHITE)
            .setTextFont(Font.font("System", FontWeight.BOLD, 11));

    // ParentsCanvasDrawer - draws parent (room names) and grandparent (category) headers
    protected final ParentsCanvasDrawer parentsCanvasDrawer;

    // Color scheme for styling
    private final GanttColorScheme colorScheme = new GanttColorScheme();

    // Canvas-based tooltip (replaces JavaFX Tooltip which doesn't compile with WebFX)
    private final CanvasTooltip canvasTooltip;

    // Hovered row tracking for row highlight effects
    private double hoveredMouseY = -1; // -1 means no hover

    public HouseholdGanttCanvas(AccommodationPresentationModel pm) {
        // Presentation model for binding time window
        // Presenter for expand/collapse state

        // Initialize ParentsCanvasDrawer for drawing room names (parent) and category headers (grandparent)
        this.parentsCanvasDrawer = ParentsCanvasDrawer.create(barsLayout, barsDrawer)
                .setParentDrawer(this::drawParentRoom)
                .setGrandparentDrawer(this::drawGrandparentCategory);

        // Binding the presentation model and the barsLayout time window
        barsLayout.bindTimeWindowBidirectional(pm);

        // Telling the bars layout how to read start & end times of bars (CRITICAL!)
        // For hotel bookings:
        // - startDate = check-in day (inclusive - bar starts at beginning of this day)
        // - endDate = checkout day (exclusive - bar ends at beginning of this day, NOT including it)
        // This ensures back-to-back bookings (checkout = next check-in) don't visually overlap
        barsLayout.setInclusiveChildStartTimeReader(dev.webfx.extras.time.layout.bar.TimeBar::getStartTime);
        barsLayout.setExclusiveChildEndTimeReader(dev.webfx.extras.time.layout.bar.TimeBar::getEndTime);

        // Configure layout for bed display
        // Grandparent (room type) = TOP (horizontal header spanning category)
        // Parent (room name) = LEFT (vertical list in left column)
        // Child rows (bed labels) = drawn by drawBedRow, should appear in same column as parents
        barsLayout
                .setGrandparentHeaderPosition(dev.webfx.extras.time.layout.gantt.HeaderPosition.TOP)
                .setParentHeaderPosition(dev.webfx.extras.time.layout.gantt.HeaderPosition.LEFT);

        // Add day backgrounds and grid lines before drawing bars
        barsDrawer.addOnBeforeDraw(this::drawDayBackgroundsAndGrid);

        // Add expanded multi-bed room block backgrounds (light grey for room + beds as a group)
        barsDrawer.addOnBeforeDraw(this::drawExpandedBlockBackgrounds);

        // Overbooking is now indicated with diagonal stripes on bars instead of red background
        // barsDrawer.addOnBeforeDraw(this::drawOverbookingRowBackgrounds);  // Disabled - using diagonal stripes instead

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

        // Add hovered row background drawing (AFTER day backgrounds so it's visible on top)
        // Note: Mouse events are handled in HouseholdCanvasGanttView.setupClickHandler()
        // which calls setHoveredMouseY() to update the hover position
        barsDrawer.addOnBeforeDraw(this::drawHoveredRowBackground);
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
     * Sets the Y position of the mouse for row hover highlighting.
     * Called from HouseholdCanvasGanttView when mouse moves over the canvas.
     * @param y The mouse Y coordinate in canvas space, or -1 to clear hover
     */
    public void setHoveredMouseY(double y) {
        if (y != hoveredMouseY) {
            hoveredMouseY = y;
            barsDrawer.markDrawAreaAsDirty(); // Trigger redraw to update highlight
        }
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
     * Draws a booking bar.
     * This is called for each bar by LocalDateCanvasDrawer.
     * <p>
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
        MutableBounds adjustedBounds = getMutableBounds(b, dayWidth);

        // Get bar color based on booking status
        Color barColor = colorScheme.getBookingStatusColor(block.getStatus());

        // Check if this is an aggregate bar (multi-bed room collapsed)
        if (block.isAggregateBar()) {
            // Draw aggregate bar with daily occupancy text for each day
            drawAggregateBar(bar, block, adjustedBounds, barColor, dayWidth, gc);
        } else {
            // Draw regular booking bar with guest name
            drawRegularBookingBar(block, adjustedBounds, barColor, dayWidth, gc);
        }

        // Draw turnover indicator if needed (orange badge at top left)
        // Shows when another booking checks out on the same day this booking checks in
        if (block.hasTurnover()) {
            drawTurnoverIndicator(adjustedBounds, gc);
        }
    }

    private static MutableBounds getMutableBounds(Bounds b, double dayWidth) {
        double halfDayWidth = dayWidth / 2;

        // Create adjusted bounds:
        // - X: shift right by half a day (to middle of arrival day)
        // - Y: center vertically within row using BAR_HEIGHT
        // - Width: keep same width (shift both start AND end by half day to center the bar)
        //          Original bounds: [start of arrival day, start of checkout day)
        //          After shift: [middle of arrival day, middle of checkout day)
        // - Height: use BAR_HEIGHT constant for consistent bar sizing
        MutableBounds adjustedBounds = new MutableBounds();
        adjustedBounds.setMinX(b.getMinX() + halfDayWidth);
        adjustedBounds.setMinY(b.getMinY() + (b.getHeight() - BAR_HEIGHT) / 2);  // Center bar vertically
        adjustedBounds.setWidth(b.getWidth());  // Width stays same - both start and end shift by half day
        adjustedBounds.setHeight(BAR_HEIGHT);
        return adjustedBounds;
    }

    /**
     * Draws a regular booking bar with person icon on first day (no text).
     * Used for single rooms and individual bed bookings in expanded multi-bed rooms.
     * The person icon is clickable to show guest information in a tooltip.
     */
    private void drawRegularBookingBar(HouseholdBookingBlock block, Bounds adjustedBounds, Color barColor, double dayWidth, GraphicsContext gc) {
        // Always show person icon on first day of booking (ARRIVAL or SINGLE position)
        // The icon is clickable to show guest info tooltip
        one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition position = block.getPosition();

        // Draw bar - use striped pattern if overbooking, solid color otherwise
        if (block.hasConflict()) {
            // Draw overbooked bar (red with white stripes)
            drawOverbookedDaySegment(adjustedBounds.getMinX(), adjustedBounds.getMinY(),
                    adjustedBounds.getWidth(), adjustedBounds.getHeight(), true, true, gc);
        } else {
            // Configure the bar drawer with NO TEXT (only icon)
            bookingBarDrawer
                    .setBackgroundFill(barColor)
                    .setTextFill(Color.WHITE)
                    .setStroke(null)
                    .setRadius(BAR_RADIUS)
                    .setMiddleText(null);  // No text - only icon

            // Clear any icon from BarDrawer - we'll draw person icon manually with proper scaling
            bookingBarDrawer.setIcon(null, null, 0, 0, null, null, null, 0, 0);

            // Draw the bar with configured properties
            bookingBarDrawer.drawBar(adjustedBounds, gc);
        }

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
            // Use the dayWidth passed from drawBar() for consistent positioning
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

        // Draw room not ready warning icon if guest arriving today and room needs cleaning/inspection
        if (block.hasRoomNotReadyWarning()) {
            drawRoomNotReadyWarning(adjustedBounds, gc);
        }
    }

    /**
     * Draws a "room not ready" warning indicator (red circular badge with "!" icon).
     * This shows when a guest is arriving today but the room still needs cleaning or inspection.
     * Positioned at the top-left of the booking bar, slightly offset from the turnover indicator position.
     */
    private void drawRoomNotReadyWarning(Bounds b, GraphicsContext gc) {
        // Badge size
        double badgeSize = 16;

        // Position at the top-left corner of the bar, above the bar
        // Offset slightly to the right of bar start to not overlap with turnover indicator
        double badgeX = b.getMinX() + 4;
        double badgeY = b.getMinY() - badgeSize + 2; // Position above the bar with slight overlap

        // Draw red circular background (distinct from orange turnover indicator)
        gc.setFill(Color.web("#DC2626")); // Red-600 - danger color
        gc.fillOval(badgeX, badgeY, badgeSize, badgeSize);

        // Draw darker red border for visibility
        gc.setStroke(Color.web("#B91C1C")); // Red-700 - darker border
        gc.setLineWidth(1.5);
        gc.strokeOval(badgeX, badgeY, badgeSize, badgeSize);

        // Draw warning symbol "!" in white
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 11));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);
        gc.fillText("!", badgeX + badgeSize / 2, badgeY + badgeSize / 2);
    }

    /**
     * Draws an aggregate bar for collapsed multi-bed rooms.
     * Shows occupancy/capacity text for each day (e.g., "4/5", "5/5", "3/5").
     */
    private void drawAggregateBar(LocalDateBar<HouseholdBookingBlock> bar, HouseholdBookingBlock block,
                                  Bounds adjustedBounds, Color barColor, double dayWidth, GraphicsContext gc) {
        LocalDate startDate = bar.getStartTime();
        LocalDate endDate = bar.getEndTime();
        java.util.Map<LocalDate, Integer> dailyOccupancy = block.getDailyOccupancy();
        int totalCapacity = block.getTotalCapacity();

        // First pass: Draw bar segments - normal color for regular days, striped for overbooked days
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // Get occupancy for this day
            Integer occupancy;
            if (currentDate.equals(endDate)) {
                LocalDate lastNight = currentDate.minusDays(1);
                occupancy = dailyOccupancy != null ? dailyOccupancy.get(lastNight) : null;
            } else {
                occupancy = dailyOccupancy != null ? dailyOccupancy.get(currentDate) : null;
            }

            if (occupancy != null && occupancy > 0) {
                // Calculate segment bounds for this day
                double dayX = barsLayout.getTimeProjector().timeToX(currentDate, true, false);
                double segmentStartX, segmentEndX;

                if (currentDate.equals(startDate)) {
                    // First day: bar starts at middle of day
                    segmentStartX = dayX + dayWidth / 2;
                    segmentEndX = dayX + dayWidth;
                } else if (currentDate.equals(endDate)) {
                    // Last day: bar ends at middle of day
                    segmentStartX = dayX;
                    segmentEndX = dayX + dayWidth / 2;
                } else {
                    // Middle days: full day width
                    segmentStartX = dayX;
                    segmentEndX = dayX + dayWidth;
                }

                double segmentWidth = segmentEndX - segmentStartX;
                boolean isOverbooked = occupancy > totalCapacity;

                // Draw segment with appropriate style
                if (isOverbooked) {
                    // Overbooked day: red background with white stripes
                    drawOverbookedDaySegment(segmentStartX, adjustedBounds.getMinY(),
                            segmentWidth, adjustedBounds.getHeight(),
                            currentDate.equals(startDate), currentDate.equals(endDate), gc);
                } else {
                    // Normal day: blue bar color
                    drawNormalDaySegment(segmentStartX, adjustedBounds.getMinY(),
                            segmentWidth, adjustedBounds.getHeight(), barColor,
                            currentDate.equals(startDate), currentDate.equals(endDate), gc);
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        // Second pass: Draw occupancy text for each day
        if (dailyOccupancy == null) {
            return;
        }

        gc.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 11));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);

        currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            Integer occupancy;
            if (currentDate.equals(endDate)) {
                LocalDate lastNight = currentDate.minusDays(1);
                occupancy = dailyOccupancy.get(lastNight);
            } else {
                occupancy = dailyOccupancy.get(currentDate);
            }

            if (occupancy != null && occupancy > 0) {
                double dayX = barsLayout.getTimeProjector().timeToX(currentDate, true, false);
                double dayMiddleX;

                if (currentDate.equals(startDate)) {
                    dayMiddleX = dayX + (dayWidth * 0.75);
                } else if (currentDate.equals(endDate)) {
                    dayMiddleX = dayX + (dayWidth * 0.25);
                } else {
                    dayMiddleX = dayX + dayWidth / 2;
                }

                // Draw occupancy text
                gc.setFill(Color.WHITE);
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

                    // Calculate pixel offset from bar start to booking start
                    double barStartX = barsLayout.getTimeProjector().timeToX(startDate, true, false);
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

        // Draw room not ready warning icon if someone is arriving today and room is not ready
        if (block.hasRoomNotReadyWarning()) {
            drawRoomNotReadyWarning(adjustedBounds, gc);
        }
    }

    /**
     * Draws a normal (non-overbooked) day segment of an aggregate bar.
     */
    private void drawNormalDaySegment(double x, double y, double width, double height,
                                      Color barColor, boolean isFirstDay, boolean isLastDay, GraphicsContext gc) {
        gc.save();
        gc.setFill(barColor);

        // Determine corner radii based on position
        double leftRadius = isFirstDay ? BAR_RADIUS * 2 : 0;
        double rightRadius = isLastDay ? BAR_RADIUS * 2 : 0;

        // Draw rounded rectangle segment
        if (leftRadius > 0 || rightRadius > 0) {
            // Use path for mixed corners
            gc.beginPath();
            if (leftRadius > 0) {
                gc.moveTo(x + leftRadius / 2, y);
                gc.arcTo(x, y, x, y + leftRadius / 2, leftRadius / 2);
                gc.lineTo(x, y + height - leftRadius / 2);
                gc.arcTo(x, y + height, x + leftRadius / 2, y + height, leftRadius / 2);
            } else {
                gc.moveTo(x, y);
                gc.lineTo(x, y + height);
            }
            if (rightRadius > 0) {
                gc.lineTo(x + width - rightRadius / 2, y + height);
                gc.arcTo(x + width, y + height, x + width, y + height - rightRadius / 2, rightRadius / 2);
                gc.lineTo(x + width, y + rightRadius / 2);
                gc.arcTo(x + width, y, x + width - rightRadius / 2, y, rightRadius / 2);
            } else {
                gc.lineTo(x + width, y + height);
                gc.lineTo(x + width, y);
            }
            gc.closePath();
            gc.fill();
        } else {
            // Simple rectangle for middle segments
            gc.fillRect(x, y, width, height);
        }

        gc.restore();
    }

    /**
     * Draws an overbooked day segment with red background and white diagonal stripes.
     */
    private void drawOverbookedDaySegment(double x, double y, double width, double height,
                                          boolean isFirstDay, boolean isLastDay, GraphicsContext gc) {
        gc.save();

        // Red background color for overbooking
        Color redColor = Color.rgb(229, 53, 53);  // #E53935

        // Determine corner radii based on position
        double leftRadius = isFirstDay ? BAR_RADIUS * 2 : 0;
        double rightRadius = isLastDay ? BAR_RADIUS * 2 : 0;

        // Draw red background
        gc.setFill(redColor);
        if (leftRadius > 0 || rightRadius > 0) {
            gc.beginPath();
            if (leftRadius > 0) {
                gc.moveTo(x + leftRadius / 2, y);
                gc.arcTo(x, y, x, y + leftRadius / 2, leftRadius / 2);
                gc.lineTo(x, y + height - leftRadius / 2);
                gc.arcTo(x, y + height, x + leftRadius / 2, y + height, leftRadius / 2);
            } else {
                gc.moveTo(x, y);
                gc.lineTo(x, y + height);
            }
            if (rightRadius > 0) {
                gc.lineTo(x + width - rightRadius / 2, y + height);
                gc.arcTo(x + width, y + height, x + width, y + height - rightRadius / 2, rightRadius / 2);
                gc.lineTo(x + width, y + rightRadius / 2);
                gc.arcTo(x + width, y, x + width - rightRadius / 2, y, rightRadius / 2);
            } else {
                gc.lineTo(x + width, y + height);
                gc.lineTo(x + width, y);
            }
            gc.closePath();
            gc.fill();
        } else {
            gc.fillRect(x, y, width, height);
        }

        // Draw white diagonal stripes clipped to segment bounds
        double stripeWidth = 3;
        double stripeSpacing = 6;
        gc.setStroke(Color.rgb(255, 255, 255, 0.7));  // Semi-transparent white
        gc.setLineWidth(stripeWidth);

        double maxDimension = Math.max(width, height) * 2;

        for (double offset = -maxDimension; offset < width + maxDimension; offset += stripeSpacing) {
            double x1 = x + offset;
            double y1 = y + height;
            double x2 = x + offset + height;
            double y2 = y;

            // Clip line to segment bounds
            double[] clipped = clipLineToRect(x1, y1, x2, y2, x, y, x + width, y + height);
            if (clipped != null) {
                gc.strokeLine(clipped[0], clipped[1], clipped[2], clipped[3]);
            }
        }

        gc.restore();
    }

    /**
     * Clips a line to a rectangle.
     * Returns null if line is completely outside, or [x1, y1, x2, y2] of clipped line.
     */
    private double[] clipLineToRect(double x1, double y1, double x2, double y2,
                                    double minX, double minY, double maxX, double maxY) {
        // Simple clipping using line equation
        double dx = x2 - x1;
        double dy = y2 - y1;

        double tMin = 0, tMax = 1;

        // Clip against each edge
        if (dx != 0) {
            double t1 = (minX - x1) / dx;
            double t2 = (maxX - x1) / dx;
            if (dx < 0) { double tmp = t1; t1 = t2; t2 = tmp; }
            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);
        } else if (x1 < minX || x1 > maxX) {
            return null;
        }

        if (dy != 0) {
            double t1 = (minY - y1) / dy;
            double t2 = (maxY - y1) / dy;
            if (dy < 0) { double tmp = t1; t1 = t2; t2 = tmp; }
            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);
        } else if (y1 < minY || y1 > maxY) {
            return null;
        }

        if (tMin > tMax) {
            return null;
        }

        return new double[]{
            x1 + tMin * dx, y1 + tMin * dy,
            x1 + tMax * dx, y1 + tMax * dy
        };
    }

    /**
     * Draws a turnover indicator (orange circular badge with warning icon) centered between the two bars.
     * This indicates that housekeeping needs to clean the room between guests on the same day.
     */
    private void drawTurnoverIndicator(Bounds b, GraphicsContext gc) {
        // Badge size
        double badgeSize = 16;

        // Position centered at the junction point between departing and arriving bars
        // b.getMinX() is the start of the arriving bar (middle of the day)
        // Center the badge horizontally at the junction, positioned above the bars
        double badgeX = b.getMinX() - badgeSize / 2;
        double badgeY = b.getMinY() - badgeSize + 2; // Position above the bar with slight overlap

        // Draw orange circular background
        gc.setFill(Color.web("#F59E0B")); // Amber-500 - modern warning color
        gc.fillOval(badgeX, badgeY, badgeSize, badgeSize);

        // Draw dark border for visibility
        gc.setStroke(Color.web("#D97706")); // Amber-600 - darker border
        gc.setLineWidth(1.5);
        gc.strokeOval(badgeX, badgeY, badgeSize, badgeSize);

        // Draw warning symbol "!" in white
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 11));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);
        gc.fillText("!", badgeX + badgeSize / 2, badgeY + badgeSize / 2);
    }

    /**
     * Draws a highlighted background for the row under the mouse cursor.
     * Uses a light warm color similar to the yellow used for today's column.
     */
    private void drawHoveredRowBackground() {
        if (hoveredMouseY < 0) {
            return; // No hover
        }

        GraphicsContext gc = barsDrawer.getCanvas().getGraphicsContext2D();
        double canvasWidth = barsDrawer.getCanvas().getWidth();

        // Get scroll offset to adjust Y coordinates
        double layoutOriginY = barsDrawer.getLayoutOriginY();

        // Get all parent rows from the layout
        java.util.List<dev.webfx.extras.time.layout.gantt.impl.ParentRow<LocalDateBar<HouseholdBookingBlock>>> parentRows = barsLayout.getParentRows();
        if (parentRows.isEmpty()) {
            return;
        }

        // Find the row that contains the mouse Y position
        for (dev.webfx.extras.time.layout.gantt.impl.ParentRow<LocalDateBar<HouseholdBookingBlock>> parentRow : parentRows) {
            Bounds rowBounds = parentRow.getHeader();
            if (rowBounds == null) {
                continue;
            }

            // Adjust Y coordinate for scroll position
            double adjustedY = rowBounds.getMinY() - layoutOriginY;
            double rowTop = adjustedY;
            double rowBottom = adjustedY + rowBounds.getHeight();

            // Check if mouse is within this row
            if (hoveredMouseY >= rowTop && hoveredMouseY < rowBottom) {
                // Draw light warm highlight - using a soft blue tint for better visibility
                // This is drawn AFTER day backgrounds so it overlays them
                gc.setFill(Color.rgb(200, 230, 255, 0.5)); // Light blue with transparency
                gc.fillRect(0, adjustedY, canvasWidth, rowBounds.getHeight());
                break; // Found the hovered row, no need to continue
            }
        }
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
     * Draws light grey backgrounds for expanded multi-bed room blocks.
     * When a multi-bed room is unfolded, both the room row and all its bed rows
     * are highlighted with a light grey background to visually group them together.
     * This has lower priority than danger backgrounds (overbooking) and today's highlight.
     */
    private void drawExpandedBlockBackgrounds() {
        GraphicsContext gc = barsDrawer.getCanvas().getGraphicsContext2D();
        double canvasWidth = barsDrawer.getCanvas().getWidth();

        // Get scroll offset to adjust Y coordinates
        double layoutOriginY = barsDrawer.getLayoutOriginY();

        // Get all parent rows from the layout
        java.util.List<dev.webfx.extras.time.layout.gantt.impl.ParentRow<LocalDateBar<HouseholdBookingBlock>>> parentRows = barsLayout.getParentRows();
        if (parentRows.isEmpty()) {
            return;
        }

        // Light grey color for expanded block background
        Color expandedBlockColor = Color.rgb(245, 245, 245); // Very light grey

        // Calculate today's column bounds to skip it (preserve today's yellow highlight)
        LocalDate today = LocalDate.now();
        double todayStartX = barsLayout.getTimeProjector().timeToX(today, true, false);
        double todayEndX = barsLayout.getTimeProjector().timeToX(today.plusDays(1), true, false);

        // Draw expanded block backgrounds
        for (dev.webfx.extras.time.layout.gantt.impl.ParentRow<LocalDateBar<HouseholdBookingBlock>> parentRow : parentRows) {
            Object parent = parentRow.getParent();
            if (parent instanceof GanttParentRow ganttParent) {

                // Get the row bounds from the ParentRow
                Bounds rowBounds = parentRow.getHeader();
                if (rowBounds == null) {
                    continue;
                }

                // Adjust Y coordinate for scroll position
                double adjustedY = rowBounds.getMinY() - layoutOriginY;

                // Draw background for expanded room rows and their bed rows
                // Case 1: Expanded multi-bed room row
                // Case 2: Bed row (belongs to an expanded multi-bed room)
                if ((ganttParent.expanded() && ganttParent.isMultiBedRoom()) || ganttParent.isBed()) {
                    gc.setFill(expandedBlockColor);
                    // Draw grey background in two parts, skipping today's column
                    // Part 1: From left edge to today's start
                    if (todayStartX > 0) {
                        gc.fillRect(0, adjustedY, todayStartX, rowBounds.getHeight());
                    }
                    // Part 2: From today's end to right edge
                    if (todayEndX < canvasWidth) {
                        gc.fillRect(todayEndX, adjustedY, canvasWidth - todayEndX, rowBounds.getHeight());
                    }
                }
            }
        }
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
        if (parentRows.isEmpty()) {
            return;
        }

        // Draw danger backgrounds
        for (dev.webfx.extras.time.layout.gantt.impl.ParentRow<LocalDateBar<HouseholdBookingBlock>> parentRow : parentRows) {
            Object parent = parentRow.getParent();
            if (parent instanceof GanttParentRow ganttParent) {

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
                    int roomCapacity = ganttParent.room().getCapacity();

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
                        Set<LocalDate> overbookedDates = getLocalDates(ganttParent, allBars, roomCapacity);

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

    private static Set<LocalDate> getLocalDates(GanttParentRow ganttParent, List<LocalDateBar<HouseholdBookingBlock>> allBars, int roomCapacity) {
        Set<LocalDate> overbookedDates = new java.util.HashSet<>();

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
                        int overlappingCount = getOverlappingCount(ganttParent, allBars, checkDate);

                        // If more than capacity bookings overlap, mark this day as overbooked
                        if (overlappingCount > roomCapacity) {
                            overbookedDates.add(checkDate);
                        }

                        currentDate = currentDate.plusDays(1);
                    }
                }
            }
        }
        return overbookedDates;
    }

    private static int getOverlappingCount(GanttParentRow ganttParent, List<LocalDateBar<HouseholdBookingBlock>> allBars, LocalDate checkDate) {
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
        return overlappingCount;
    }

    /**
     * Draws a grandparent category header (room type/category name).
     * This is called by ParentsCanvasDrawer for each category.
     */
    protected void drawGrandparentCategory(Object category, Bounds b, GraphicsContext gc) {
        String categoryName = category != null ? category.toString() : "";
        grandparentRoomTypeDrawer
                .setMiddleText(categoryName)
                .drawBar(b, gc);
    }

    /**
     * Truncates text with ellipsis if it exceeds the available width.
     * Uses WebFX-compatible text measurement.
     */
    private String truncateWithEllipsis(String text, double maxWidth, GraphicsContext gc) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // Measure the full text width
        double textWidth = measureTextWidth(text, gc);
        if (textWidth <= maxWidth) {
            return text;
        }

        // Need to truncate - find the longest substring that fits with "..."
        String ellipsis = "...";
        double ellipsisWidth = measureTextWidth(ellipsis, gc);
        double availableWidth = maxWidth - ellipsisWidth;

        if (availableWidth <= 0) {
            return ellipsis;
        }

        // Binary search for the optimal truncation point
        int low = 0;
        int high = text.length();
        while (low < high) {
            int mid = (low + high + 1) / 2;
            String truncated = text.substring(0, mid);
            if (measureTextWidth(truncated, gc) <= availableWidth) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }

        return low > 0 ? text.substring(0, low) + ellipsis : ellipsis;
    }

    /**
     * Measures the width of text using the current font.
     * WebFX-compatible text measurement.
     */
    private double measureTextWidth(String text, GraphicsContext gc) {
        javafx.geometry.Bounds textBounds = dev.webfx.kit.launcher.WebFxKitLauncher.measureText(text, gc.getFont());
        return textBounds != null ? textBounds.getWidth() : text.length() * 7; // Fallback estimation
    }

    /**
     * Checks if the given bounds correspond to the currently hovered row.
     */
    private boolean isRowHovered(Bounds b) {
        if (hoveredMouseY < 0) {
            return false;
        }
        double layoutOriginY = barsDrawer.getLayoutOriginY();
        double adjustedY = b.getMinY() - layoutOriginY;
        return hoveredMouseY >= adjustedY && hoveredMouseY < adjustedY + b.getHeight();
    }

    /**
     * Draws a parent room header (room name in the left column).
     * This is called by ParentsCanvasDrawer for each room/bed row.
     * Layout: [arrow (if multi-bed)] [status dot] [room name] ... [action icon]
     */
    protected void drawParentRoom(Object parent, Bounds b, GraphicsContext gc) {
        if (parent instanceof GanttParentRow ganttParent) {
            String roomName = ganttParent.room() != null ? ganttParent.room().getName() : "";

            // Check if this row is hovered for highlighting
            boolean isHovered = isRowHovered(b);

            // Constants for layout
            double leftPadding = 8;
            double arrowWidth = 14;
            double dotRadius = 4;
            double gap = 6;

            // For bed rows, show bed label instead of room name
            if (ganttParent.isBed()) {
                roomName = ganttParent.bed() != null ? ganttParent.bed().getName() : roomName;
                boolean isOverbooking = ganttParent.isOverbooking();

                // Draw background - hover highlight, danger red for overbooking, or grey for normal beds
                Color bgColor;
                if (isHovered) {
                    bgColor = Color.rgb(200, 230, 255); // Light blue highlight (no transparency for header)
                } else if (isOverbooking) {
                    bgColor = Color.web("#FFCDD2");
                } else {
                    bgColor = Color.grayRgb(248);
                }
                gc.setFill(bgColor);
                gc.fillRect(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());

                // Draw border (right and bottom)
                gc.setStroke(Color.grayRgb(200));
                gc.setLineWidth(1);
                gc.strokeLine(b.getMaxX(), b.getMinY(), b.getMaxX(), b.getMaxY());
                gc.strokeLine(b.getMinX(), b.getMaxY(), b.getMaxX(), b.getMaxY());

                // Draw bed name (indented) - white text for overbooking, grey for normal
                // No status dot here - status indicator is on right edge
                double textX = b.getMinX() + leftPadding + arrowWidth + gap;
                double centerY = b.getMinY() + b.getHeight() / 2;
                Color textColor = isOverbooking ? Color.WHITE : Color.web("#666666");
                gc.setFill(textColor);
                gc.setFont(Font.font("System", isOverbooking ? FontWeight.BOLD : FontWeight.NORMAL, 12));
                gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
                gc.setTextBaseline(javafx.geometry.VPos.CENTER);
                gc.fillText(roomName, textX, centerY);

                // Draw prominent status indicator along entire right edge
                double indicatorWidth = 6;
                Color statusColor = ganttParent.bed() != null ?
                        colorScheme.getRoomStatusColor(ganttParent.bed().getStatus()) : Color.GRAY;
                gc.setFill(statusColor);
                gc.fillRect(b.getMaxX() - indicatorWidth, b.getMinY(), indicatorWidth, b.getHeight());

            } else {
                // Room row
                // Draw background - hover highlight takes priority
                Color bgColor;
                if (isHovered) {
                    bgColor = Color.rgb(200, 230, 255); // Light blue highlight
                } else if (ganttParent.expanded()) {
                    bgColor = Color.web("#ECECEC");
                } else {
                    bgColor = Color.WHITE;
                }
                gc.setFill(bgColor);
                gc.fillRect(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());

                // Draw border (right and bottom)
                gc.setStroke(Color.grayRgb(200));
                gc.setLineWidth(1);
                gc.strokeLine(b.getMaxX(), b.getMinY(), b.getMaxX(), b.getMaxY());
                gc.strokeLine(b.getMinX(), b.getMaxY(), b.getMaxX(), b.getMaxY());

                double currentX = b.getMinX() + leftPadding;
                double centerY = b.getMinY() + b.getHeight() / 2;

                // Check if room has a comment to display
                String roomComment = ganttParent.room() != null ? ganttParent.room().getRoomComments() : null;
                boolean hasComment = roomComment != null && !roomComment.isEmpty();

                // Adjust vertical positions if comment exists (two-line layout)
                double roomNameY = hasComment ? centerY - 6 : centerY;
                double commentY = centerY + 8;

                // Draw expand/collapse arrow for multi-bed rooms
                if (ganttParent.isMultiBedRoom()) {
                    String arrow = ganttParent.expanded() ? "∨" : "›";
                    gc.setFill(Color.web("#333333"));
                    gc.setFont(Font.font("System", FontWeight.BOLD, 14));
                    gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
                    gc.setTextBaseline(javafx.geometry.VPos.CENTER);
                    gc.fillText(arrow, currentX, centerY); // Arrow stays centered
                }
                currentX += arrowWidth + gap;

                // Draw room name (shifted up if comment exists)
                gc.setFill(Color.web("#333333"));
                gc.setFont(Font.font("System", FontWeight.BOLD, 13));
                gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
                gc.setTextBaseline(javafx.geometry.VPos.CENTER);
                gc.fillText(roomName, currentX, roomNameY);

                // Draw comment below room name (if exists)
                if (hasComment) {
                    // Calculate available width for comment (leave space for action icon and status indicator)
                    double availableWidth = b.getMaxX() - currentX - 30; // 30 = action icon area + padding

                    // Set smaller font for comment
                    gc.setFont(Font.font("System", FontWeight.NORMAL, 10));
                    gc.setFill(Color.web("#666666"));

                    // Truncate with ellipsis if needed
                    String displayComment = truncateWithEllipsis(roomComment, availableWidth, gc);
                    gc.fillText(displayComment, currentX, commentY);
                }

                // Draw action icon (⋮) on the right side before status indicator
                gc.setFill(Color.grayRgb(100));
                gc.setFont(Font.font("System", 14));
                gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
                gc.fillText("⋮", b.getMaxX() - 22, centerY);

                // Draw prominent status indicator along entire right edge
                double indicatorWidth = 6;
                Color statusColor = ganttParent.room() != null ?
                        colorScheme.getRoomStatusColor(ganttParent.room().getStatus()) : Color.GRAY;
                gc.setFill(statusColor);
                gc.fillRect(b.getMaxX() - indicatorWidth, b.getMinY(), indicatorWidth, b.getHeight());
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
        private final boolean hasRoomNotReadyWarning; // True if guest arriving today and room is not ready
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
            this(guestName, status, position, hasConflict, hasComments, hasTurnover, hasLateArrival, false,
                 bookingData, occupancy, totalCapacity, null, null);
        }

        public HouseholdBookingBlock(String guestName, BookingStatus status,
                                     one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition position,
                                     boolean hasConflict, boolean hasComments, boolean hasTurnover,
                                     boolean hasLateArrival, boolean hasRoomNotReadyWarning,
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
            this.hasRoomNotReadyWarning = hasRoomNotReadyWarning;
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

        public boolean hasRoomNotReadyWarning() {
            return hasRoomNotReadyWarning;
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
