package one.modality.hotel.backoffice.activities.household.gantt.renderer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.hotel.backoffice.activities.household.gantt.model.BookingBar;
import one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition;
import one.modality.hotel.backoffice.activities.household.gantt.view.SvgIconFactory;

/**
 * Renders booking bars for multi-bed rooms (double rooms, dormitories).
 * Pattern B: Gantt flow with seamless bars using negative margins.
 * Displays occupancy ratio (e.g., "3/6") on the bar.
 *
 * @author Claude Code Assistant
 */
public class MultiRoomBookingBarRenderer implements BookingBarRenderer {

    private static final double BOOKING_BAR_TOP_MARGIN = 8;
    private static final double BOOKING_BAR_BOTTOM_MARGIN = 8;

    private final GanttColorScheme colorScheme;

    public MultiRoomBookingBarRenderer(GanttColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    @Override
    public Node render(BookingBar bar, double cellWidth, double cellHeight) {
        // Create container for bar and label
        StackPane container = new StackPane();

        // Create the bar region
        Region barRegion = createBarRegion(bar, cellWidth, cellHeight);

        container.getChildren().add(barRegion);

        // Create occupancy label - centered
        Label occupancyLabel = createOccupancyLabel(bar);
        container.getChildren().add(occupancyLabel);

        // Add icons (no person icon - only for individual beds)
        HBox iconsBox = new HBox(3);
        iconsBox.setAlignment(Pos.CENTER_LEFT);
        iconsBox.setPickOnBounds(false);

        // Message icon if has comments (appears on the same date where individual beds show it)
        if (bar.hasComments()) {
            SVGPath messageIcon = SvgIconFactory.createMessageIcon();
            iconsBox.getChildren().add(messageIcon);
        }

        if (!iconsBox.getChildren().isEmpty()) {
            container.getChildren().add(iconsBox);
            StackPane.setAlignment(iconsBox, Pos.CENTER_LEFT); // CENTER_LEFT for vertical centering
            // Comment icon on left edge of cell
            StackPane.setMargin(iconsBox, new Insets(0, 0, 0, 4)); // Small left margin from edge
        }

        return container;
    }

    private Region createBarRegion(BookingBar bar, double cellWidth, double cellHeight) {
        Region barRegion = new Region();

        // Set bar height
        double barHeight = cellHeight - BOOKING_BAR_TOP_MARGIN - BOOKING_BAR_BOTTOM_MARGIN;
        barRegion.setPrefHeight(barHeight);
        barRegion.setMinHeight(barHeight);
        barRegion.setMaxHeight(barHeight);

        // Set color based on status
        Color barColor = colorScheme.getBookingStatusColor(bar.status());
        CornerRadii radii = getBarRadii(bar.position());
        barRegion.setBackground(new Background(new BackgroundFill(barColor, radii, Insets.EMPTY)));

        // Position based on booking position (Pattern B - Gantt flow)
        double leftMargin, rightMargin, width;

        switch (bar.position()) {
            case ARRIVAL:
                // Starts at middle (50%), extends to right edge
                // Use -1px for seamless flow with adjacent cells
                leftMargin = cellWidth * 0.5;
                rightMargin = -1;
                width = cellWidth - leftMargin + 1;
                break;
            case MIDDLE:
                // Full width with -1px on each side for seamless flow
                leftMargin = -1;
                rightMargin = -1;
                width = cellWidth + 2;
                break;
            case DEPARTURE:
                // Starts from left, ends at middle (50%)
                leftMargin = -1;
                rightMargin = cellWidth * 0.5;
                width = cellWidth - rightMargin + 1;
                break;
            case SINGLE:
                // Centered, fully rounded (small dot)
                leftMargin = cellWidth * 0.5 - 5;
                rightMargin = cellWidth * 0.5 - 5;
                width = 10;
                break;
            default:
                leftMargin = 0;
                rightMargin = 0;
                width = cellWidth;
        }

        barRegion.setPrefWidth(width);
        StackPane.setMargin(barRegion, new Insets(BOOKING_BAR_TOP_MARGIN, rightMargin, BOOKING_BAR_BOTTOM_MARGIN, leftMargin));

        return barRegion;
    }

    /**
     * Creates the occupancy label (e.g., "3/6")
     */
    private Label createOccupancyLabel(BookingBar bar) {
        Label label = new Label(bar.occupancy() + "/" + bar.totalCapacity());
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-size: 9px; -fx-font-weight: bold;");
        label.setPadding(new Insets(0, 3, 0, 0));
        StackPane.setAlignment(label, Pos.CENTER_RIGHT);
        return label;
    }

    /**
     * Gets the appropriate border radii for a booking bar position
     */
    private CornerRadii getBarRadii(BookingPosition position) {
        return switch (position) {
            case ARRIVAL ->
                // Rounded on LEFT side only
                    new CornerRadii(10, 0, 0, 10, false);
            case DEPARTURE ->
                // Rounded on RIGHT side only
                    new CornerRadii(0, 10, 10, 0, false);
            case SINGLE ->
                // Fully rounded
                    new CornerRadii(10);
            default ->
                // No rounding for seamless flow
                    CornerRadii.EMPTY;
        };
    }
}
