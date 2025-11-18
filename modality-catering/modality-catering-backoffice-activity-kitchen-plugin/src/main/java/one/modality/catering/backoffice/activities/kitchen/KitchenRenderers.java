package one.modality.catering.backoffice.activities.kitchen;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import one.modality.base.client.time.theme.TimeFacet;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Renderers for the Kitchen calendar view.
 * Provides methods to create nodes for calendar cells.
 *
 * @author Bruno Salmon
 */
public class KitchenRenderers {

    private static KitchenView kitchenView;

    /**
     * Sets the kitchen view instance for renderers to access data.
     */
    public static void setKitchenView(KitchenView view) {
        kitchenView = view;
    }

    /**
     * Creates a node for a day of the week header.
     */
    public static Node createDayOfWeekNode(DayOfWeek dayOfWeek) {
        return TimeFacet.createDayOfWeekFacet(dayOfWeek).getContainerNode();
    }

    /**
     * Creates a node for a date cell in the calendar.
     * If attendance data exists for this date, creates an AttendanceDayPanel.
     * Otherwise, creates a simple date panel.
     */
    public static Node createDateNode(LocalDate date) {
        if (kitchenView == null)
            return TimeFacet.createDatePanelFacet(date).getContainerNode();

        AttendanceDayPanel attendanceDayPanel = kitchenView.getAttendanceDayPanelForDate(date);
        if (attendanceDayPanel != null) {
            return attendanceDayPanel;
        }

        return TimeFacet.createDatePanelFacet(date).getContainerNode();
    }

    /**
     * Creates an attendance day panel for a specific date with styling.
     */
    public static AttendanceDayPanel createAttendanceDayPanel(
            LocalDate date,
            AttendanceCounts attendanceCounts,
            AbbreviationGenerator abbreviationGenerator) {

        if (kitchenView == null || attendanceCounts == null)
            return null;

        AttendanceDayPanel dayPanel = new AttendanceDayPanel(
                attendanceCounts,
                date,
                kitchenView.getDisplayedMeals(),
                abbreviationGenerator
        );

        GridPane.setMargin(dayPanel, new Insets(5));
        TimeFacet.createDatePanelFacet(date, null, dayPanel).style();

        return dayPanel;
    }
}
