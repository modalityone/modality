package one.modality.hotel.backoffice.activities.household.gantt.renderer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.hotel.backoffice.activities.household.gantt.model.BookingBar;
import one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition;
import one.modality.hotel.backoffice.activities.household.gantt.view.SvgIconFactory;

/**
 * Renders booking bars for single rooms.
 * Uses Gantt flow pattern (continuous bars across cells).
 *
 * @author Claude Code Assistant
 */
public class SingleRoomBookingBarRenderer implements BookingBarRenderer {

    private static final double BOOKING_BAR_TOP_MARGIN = 8;
    private static final double BOOKING_BAR_BOTTOM_MARGIN = 8;

    private final GanttColorScheme colorScheme;

    public SingleRoomBookingBarRenderer(GanttColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    @Override
    public Node render(BookingBar bar, double cellWidth, double cellHeight) {
        Region barRegion = new Region();

        // Set bar height
        double barHeight = cellHeight - BOOKING_BAR_TOP_MARGIN - BOOKING_BAR_BOTTOM_MARGIN;
        barRegion.setPrefHeight(barHeight);
        barRegion.setMinHeight(barHeight);
        barRegion.setMaxHeight(barHeight);

        // Set color based on status (red border if conflict)
        Color barColor = colorScheme.getBookingStatusColor(bar.status());
        CornerRadii radii = getBarRadii(bar.position());

        if (bar.hasConflict()) {
            // Add red border for conflicts
            barRegion.setBackground(new Background(new BackgroundFill(barColor, radii, Insets.EMPTY)));
            barRegion.setStyle("-fx-border-color: #FF0404; -fx-border-width: 2px;");
        } else {
            barRegion.setBackground(new Background(new BackgroundFill(barColor, radii, Insets.EMPTY)));
        }

        // Position based on booking position (Gantt flow - continuous bars)
        double leftMargin, rightMargin, width;

        switch (bar.position()) {
            case ARRIVAL:
                // Starts at middle (50%), extends to right edge with -1px for seamless flow
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
                // Single day booking - centered with full width
                leftMargin = cellWidth * 0.25;
                rightMargin = cellWidth * 0.25;
                width = cellWidth * 0.5;
                break;
            default:
                leftMargin = 0;
                rightMargin = 0;
                width = cellWidth;
        }

        barRegion.setPrefWidth(width);
        StackPane.setMargin(barRegion, new Insets(BOOKING_BAR_TOP_MARGIN, rightMargin, BOOKING_BAR_BOTTOM_MARGIN, leftMargin));

        // Create container for bar and icons
        StackPane container = new StackPane(barRegion);

        // Add icons
        HBox iconsBox = new HBox(3);
        iconsBox.setAlignment(Pos.CENTER_LEFT);
        iconsBox.setPickOnBounds(false);  // Don't block mouse events to children
        iconsBox.setMouseTransparent(false);  // Allow iconsBox to pass events to children

        // Person icon on ARRIVAL position (first cell of booking)
        if (bar.position() == BookingPosition.ARRIVAL || bar.position() == BookingPosition.SINGLE) {
            // Use red icon if guest should have arrived but hasn't (late arrival)
            SVGPath personIcon = bar.hasLateArrival()
                ? SvgIconFactory.createPersonIconRed()
                : SvgIconFactory.createPersonIcon();
            personIcon.setStyle("-fx-cursor: hand;");
            personIcon.setPickOnBounds(true);  // Enable mouse event handling
            personIcon.setMouseTransparent(false);  // Make sure icon can receive mouse events

            // Add click handler to show guest info tooltip
            if (bar.bookingData() != null) {
                // Add hover effect
                personIcon.setOnMouseEntered(event -> {
                    personIcon.setOpacity(0.7);
                });
                personIcon.setOnMouseExited(event -> {
                    personIcon.setOpacity(1.0);
                });

                personIcon.setOnMouseClicked(event -> {
                    event.consume();  // Prevent event propagation
                    showGuestInfoTooltip(event, bar.bookingData());
                });
            }

            iconsBox.getChildren().add(personIcon);
        }

        // Message icon if has comments (on second cell - MIDDLE position)
        if (bar.hasComments() && bar.position() == BookingPosition.MIDDLE) {
            SVGPath messageIcon = SvgIconFactory.createMessageIcon();
            messageIcon.setStyle("-fx-cursor: hand;");
            messageIcon.setPickOnBounds(true);  // Enable mouse event handling
            messageIcon.setMouseTransparent(false);  // Make sure icon can receive mouse events

            // Add click handler to show special needs tooltip
            if (bar.bookingData() != null) {
                // Add hover effect
                messageIcon.setOnMouseEntered(event -> {
                    messageIcon.setOpacity(0.7);
                });
                messageIcon.setOnMouseExited(event -> {
                    messageIcon.setOpacity(1.0);
                });

                messageIcon.setOnMouseClicked(event -> {
                    event.consume();  // Prevent event propagation
                    showSpecialNeedsTooltip(event, bar.bookingData());
                });
            }

            iconsBox.getChildren().add(messageIcon);
        }

        if (!iconsBox.getChildren().isEmpty()) {
            container.getChildren().add(iconsBox);
            StackPane.setAlignment(iconsBox, Pos.CENTER_LEFT);
            // Shift person icon more to the right on ARRIVAL, comment icon on left with 3px padding for MIDDLE
            double iconShift;
            if (bar.position() == BookingPosition.ARRIVAL) {
                iconShift = cellWidth * 0.25 + 20; // Person icon: quarter cell + 20px
            } else if (bar.position() == BookingPosition.MIDDLE) {
                iconShift = 3; // Comment icon: left edge with 3px padding
            } else {
                iconShift = 15; // SINGLE position
            }
            StackPane.setMargin(iconsBox, new Insets(0, 0, 0, iconShift));
        }

        return container;
    }

    /**
     * Gets the appropriate border radii for a booking bar position
     */
    private CornerRadii getBarRadii(BookingPosition position) {
        switch (position) {
            case ARRIVAL:
                // Rounded on LEFT side only
                return new CornerRadii(10, 0, 0, 10, false);
            case DEPARTURE:
                // Rounded on RIGHT side only
                return new CornerRadii(0, 10, 10, 0, false);
            case SINGLE:
                // Fully rounded
                return new CornerRadii(10);
            case MIDDLE:
            default:
                // No rounding for seamless flow
                return CornerRadii.EMPTY;
        }
    }

    /**
     * Shows a tooltip with guest information when person icon is clicked
     */
    private void showGuestInfoTooltip(javafx.scene.input.MouseEvent event, one.modality.hotel.backoffice.activities.household.gantt.model.GanttBookingData booking) {
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

        // Show tooltip at mouse position
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        javafx.geometry.Bounds bounds = source.localToScreen(source.getBoundsInLocal());
        tooltip.show(source, bounds.getMaxX() + 10, bounds.getMinY());

        // Auto-hide after 5 seconds
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(5));
        delay.setOnFinished(e -> tooltip.hide());
        delay.play();
    }

    /**
     * Shows a tooltip with special needs when comment icon is clicked
     */
    private void showSpecialNeedsTooltip(javafx.scene.input.MouseEvent event, one.modality.hotel.backoffice.activities.household.gantt.model.GanttBookingData booking) {
        // Create tooltip content
        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip();

        StringBuilder content = new StringBuilder();
        content.append("Guest: ").append(booking.getGuestName()).append("\n");
        content.append(booking.getStartDate()).append(" - ").append(booking.getEndDate()).append("\n\n");

        if (booking.getSpecialNeeds() != null && !booking.getSpecialNeeds().isEmpty()) {
            content.append("Special Needs:\n");
            for (String need : booking.getSpecialNeeds()) {
                content.append("• ").append(need).append("\n");
            }
        }

        tooltip.setText(content.toString());
        tooltip.setStyle("-fx-font-size: 12px;");

        // Show tooltip at mouse position
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        javafx.geometry.Bounds bounds = source.localToScreen(source.getBoundsInLocal());
        tooltip.show(source, bounds.getMaxX() + 10, bounds.getMinY());

        // Auto-hide after 5 seconds
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(5));
        delay.setOnFinished(e -> tooltip.hide());
        delay.play();
    }
}
