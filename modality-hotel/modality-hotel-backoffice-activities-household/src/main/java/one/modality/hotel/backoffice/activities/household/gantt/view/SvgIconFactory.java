package one.modality.hotel.backoffice.activities.household.gantt.view;

import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 * Factory for creating SVG icons used in the Gantt calendar.
 * Provides reusable icon components.
 *
 * @author Claude Code Assistant
 */
public class SvgIconFactory {

    /**
     * Creates a person/occupancy icon (white)
     */
    public static SVGPath createPersonIcon() {
        return createPersonIcon(Color.WHITE);
    }

    /**
     * Creates a person/occupancy icon with a specific color
     */
    public static SVGPath createPersonIcon(Color color) {
        SVGPath icon = new SVGPath();
        icon.setContent("M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z");
        icon.setFill(color);
        icon.setStroke(Color.TRANSPARENT);
        icon.setScaleX(0.8);
        icon.setScaleY(0.8);
        return icon;
    }

    /**
     * Creates a red person icon (for late arrivals - guest should have arrived but hasn't)
     */
    public static SVGPath createPersonIconRed() {
        return createPersonIcon(Color.web("#FF0404"));
    }

    /**
     * Creates a question mark icon (for unknown status)
     */
    public static SVGPath createQuestionIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 17h-2v-2h2v2zm2.07-7.75l-.9.92C13.45 12.9 13 13.5 13 15h-2v-.5c0-1.1.45-2.1 1.17-2.83l1.24-1.26c.37-.36.59-.86.59-1.41 0-1.1-.9-2-2-2s-2 .9-2 2H8c0-2.21 1.79-4 4-4s4 1.79 4 4c0 .88-.36 1.68-.93 2.25z");
        icon.setFill(Color.WHITE);
        icon.setStroke(Color.TRANSPARENT);
        icon.setScaleX(0.7);
        icon.setScaleY(0.7);
        return icon;
    }

    /**
     * Creates a message/comment icon
     */
    public static SVGPath createMessageIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z");
        icon.setFill(Color.WHITE);
        icon.setStroke(Color.TRANSPARENT);
        icon.setScaleX(0.7);
        icon.setScaleY(0.7);
        return icon;
    }

    /**
     * Creates a prohibition/blocked icon
     */
    public static SVGPath createProhibitionIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zM4 12c0-4.42 3.58-8 8-8 1.85 0 3.55.63 4.9 1.69L5.69 16.9C4.63 15.55 4 13.85 4 12zm8 8c-1.85 0-3.55-.63-4.9-1.69L18.31 7.1C19.37 8.45 20 10.15 20 12c0 4.42-3.58 8-8 8z");
        icon.setFill(Color.WHITE);
        icon.setStroke(Color.TRANSPARENT);
        icon.setScaleX(0.7);
        icon.setScaleY(0.7);
        return icon;
    }

    /**
     * Creates a warning/turnover triangle icon
     */
    public static SVGPath createWarningTriangleIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z");
        icon.setFill(Color.WHITE);
        icon.setStroke(Color.TRANSPARENT);
        icon.setScaleX(0.7);
        icon.setScaleY(0.7);
        return icon;
    }

    /**
     * Creates a chevron right icon (for expand)
     */
    public static SVGPath createChevronRightIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z");
        icon.setFill(Color.web("#333333"));
        icon.setStroke(Color.TRANSPARENT);
        icon.setScaleX(0.8);
        icon.setScaleY(0.8);
        return icon;
    }

    /**
     * Creates a chevron down icon (for collapse)
     */
    public static SVGPath createChevronDownIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M16.59 8.59L12 13.17 7.41 8.59 6 10l6 6 6-6z");
        icon.setFill(Color.web("#333333"));
        icon.setStroke(Color.TRANSPARENT);
        icon.setScaleX(0.8);
        icon.setScaleY(0.8);
        return icon;
    }

    /**
     * Creates a three-dot menu icon
     */
    public static SVGPath createThreeDotsIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z");
        icon.setFill(Color.web("#666666"));
        icon.setStroke(Color.TRANSPARENT);
        icon.setScaleX(0.7);
        icon.setScaleY(0.7);
        return icon;
    }

    /**
     * Creates a turnover indicator with orange background and white warning triangle.
     * Used to mark same-day check-out/check-in turnovers.
     * Returns a StackPane containing the orange background and triangle icon.
     */
    public static javafx.scene.layout.StackPane createTurnoverIndicator() {
        // Create orange background box
        javafx.scene.layout.Region background = new javafx.scene.layout.Region();
        background.setPrefSize(20, 20);
        background.setMinSize(20, 20);
        background.setMaxSize(20, 20);
        background.getStyleClass().add("gantt-turnover-bg");

        // Create white triangle icon
        SVGPath triangle = createWarningTriangleIcon();
        triangle.setScaleX(0.65);
        triangle.setScaleY(0.65);

        // Combine in StackPane
        javafx.scene.layout.StackPane indicator = new javafx.scene.layout.StackPane(background, triangle);
        indicator.setAlignment(javafx.geometry.Pos.CENTER);
        indicator.getStyleClass().add("gantt-turnover-indicator");

        return indicator;
    }
}
