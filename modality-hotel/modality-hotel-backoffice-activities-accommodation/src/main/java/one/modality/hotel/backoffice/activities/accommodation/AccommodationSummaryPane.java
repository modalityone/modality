package one.modality.hotel.backoffice.activities.accommodation;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import one.modality.base.client.gantt.fx.selection.FXGanttSelection;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ScheduledResource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AccommodationSummaryPane extends GridPane {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy");

    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(LocalDate.now());

    private List<Attendance> attendances = Collections.emptyList();
    private List<ScheduledResource> allScheduledResource = Collections.emptyList();

    public AccommodationSummaryPane() {
        setStyle("-fx-background-color: #e0dcdc");

        // Bind the date to the one selected in the Gantt chart, provided it is a date as opposed to a month
        FXGanttSelection.ganttSelectedObjectProperty().addListener((observable, oldValue, newValue) -> {
            if (FXGanttSelection.ganttSelectedObjectProperty().get() instanceof LocalDate) {
                date.set((LocalDate) FXGanttSelection.ganttSelectedObjectProperty().get());
            }
        });
        date.addListener((observable, oldValue, newValue) -> refresh());
    }
    public void setEntities(List<Attendance> attendances) {
        this.attendances = new ArrayList<>(attendances);
        refresh();
    }

    public void setAllScheduledResource(List<ScheduledResource> allScheduledResource) {
        this.allScheduledResource = new ArrayList<>(allScheduledResource);
        refresh();
    }

    private void refresh() {
        List<Attendance> attendancesForDate = filterAttendancesByDate();
        long numRoomsOccupied = countRoomsOccupied(attendancesForDate);
        long numRoomsAvailable = countAllRooms() - numRoomsOccupied;
        long numGuests = countGuests(attendancesForDate);

        getChildren().clear();
        add(buildLabel("Status, " + formatDate()), 0, 0);
        add(buildLabel("Rooms occupied: " + numRoomsOccupied), 1, 0);
        add(buildLabel("Rooms available: " + numRoomsAvailable), 2, 0);
        add(buildLabel("Guests: " + numGuests), 3, 0);
        updateColumnWidths();
    }

    private Label buildLabel(String text) {
        Label label = new Label(text);
        setHalignment(label, HPos.CENTER);
        return label;
    }

    private void updateColumnWidths() {
        int numColumns = getColumnCount();
        double columnPercentageWidth = 100.0 / numColumns;
        double percentageRemaining = 100.0;
        List<ColumnConstraints> columnConstraints = new ArrayList<>(numColumns);
        for (int i = 0; i < numColumns - 1; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(columnPercentageWidth);
            columnConstraints.add(column);
            percentageRemaining -= columnPercentageWidth;
        }
        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(percentageRemaining);
        columnConstraints.add(column);
        getColumnConstraints().setAll(columnConstraints);
    }

    private String formatDate() {
        return DATE_FORMATTER.format(date.get());
    }

    private List<Attendance> filterAttendancesByDate() {
        return attendances.stream()
                .filter(attendance -> matchesDate(attendance.getDate()))
                .collect(Collectors.toList());
    }

    private boolean matchesDate(LocalDate otherDate) {
        return date.get().getYear() == otherDate.getYear() &&
                date.get().getMonth() == otherDate.getMonth() &&
                date.get().getDayOfMonth() == otherDate.getDayOfMonth();
    }

    private long countRoomsOccupied(List<Attendance> attendancesForDate) {
        return attendancesForDate.stream()
                .map(Attendance::getResourceConfiguration)
                .distinct()
                .count();
    }

    private long countAllRooms() {
        return allScheduledResource.stream()
                .filter(scheduledResource -> matchesDate(scheduledResource.getDate()))
                .count();
    }

    private long countGuests(List<Attendance> attendancesForDate) {
        return attendancesForDate.size();
    }
}
