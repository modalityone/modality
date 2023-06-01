package one.modality.hotel.backoffice.accommodation;

import dev.webfx.extras.util.animation.Animations;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.client.gantt.fx.selection.FXGanttSelection;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ScheduledResource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AccommodationStatusBar extends GridPane {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy");

    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(LocalDate.now());

    private List<Attendance> attendances = Collections.emptyList();
    private List<ScheduledResource> allScheduledResource = Collections.emptyList();

    public AccommodationStatusBar() {
        // Bind the date to the one selected in the Gantt chart, provided it is a date as opposed to a month
        FXGanttSelection.ganttSelectedObjectProperty().addListener((observable, oldValue, newValue) -> {
            if (FXGanttSelection.ganttSelectedObjectProperty().get() instanceof LocalDate) {
                date.set((LocalDate) FXGanttSelection.ganttSelectedObjectProperty().get());
            }
        });
        date.addListener((observable, oldValue, newValue) -> refresh());
        setAlignment(Pos.CENTER); // Makes a difference for the Web version (otherwise children appears on top)
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

        List<ScheduledResource> scheduledResourcesForDate = filterScheduledResourcesByDate();
        long numRoomsAvailable = countAllRooms(scheduledResourcesForDate) - numRoomsOccupied;
        int numBeds = countAllBeds(scheduledResourcesForDate);
        long numBedsAvailable = countBedsAvailable(scheduledResourcesForDate);
        long numBedsOccupied = numBeds - numBedsAvailable;
        long numGuests = countGuests(attendancesForDate);

        getChildren().clear();
        add(buildLabel("Status today " + formatDate()), 0, 0);
        add(buildLabel("Rooms occupied: " + numRoomsOccupied), 1, 0);
        add(buildLabel("Rooms available: " + numRoomsAvailable), 2, 0);
        add(buildLabel("Beds occupied: " + numBedsOccupied), 3, 0);
        add(buildLabel("Beds available: " + numBedsAvailable), 4, 0);
        add(buildLabel("Guests: " + numGuests), 5, 0);
        updateColumnWidths();
    }

    private Label buildLabel(String text) {
        Label label = new Label(text);
        GridPane.setHalignment(label, HPos.CENTER);
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

    private List<ScheduledResource> filterScheduledResourcesByDate() {
        return allScheduledResource.stream()
                .filter(scheduledResource -> matchesDate(scheduledResource.getDate()))
                .collect(Collectors.toList());
    }

    private long countRoomsOccupied(List<Attendance> attendancesForDate) {
        return attendancesForDate.stream()
                .map(Attendance::getResourceConfiguration)
                .distinct()
                .count();
    }

    private long countAllRooms(List<ScheduledResource> scheduledResourcesForDate) {
        return scheduledResourcesForDate.stream()
                .filter(scheduledResource -> matchesDate(scheduledResource.getDate()))
                .count();
    }

    private int countAllBeds(List<ScheduledResource> scheduledResourcesForDate) {
        return scheduledResourcesForDate.stream()
                .map(ScheduledResource::getMax)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private long countBedsAvailable(List<ScheduledResource> scheduledResourcesForDate) {
        // The "booked" field is an extra computed fields added by the ReactiveEntitiesMapper in AccommodationGanttCanvas
        return scheduledResourcesForDate.stream()
                .map(scheduledResource -> scheduledResource.getMax() - scheduledResource.getIntegerFieldValue("booked"))
                .mapToInt(Integer::intValue)
                .sum();
    }

    private long countGuests(List<Attendance> attendancesForDate) {
        return attendancesForDate.size();
    }

    double prefWidth;

    public BorderPane embedAccommodationGanttWithStatusBar(AccommodationGantt<?> accommodationGantt) {
        BorderPane borderPane = new BorderPane();
        GridPane attendeeLegend = AttendeeLegend.createLegend();
        borderPane.setLeft(attendeeLegend); // Left first, so it's behind center when animating it
        borderPane.setCenter(accommodationGantt.buildCanvasContainer());
        attendeeLegend.setVisible(false);
        UiScheduler.scheduleInAnimationFrame(() -> {
            prefWidth = attendeeLegend.prefWidth(-1);
            attendeeLegend.setMinWidth(0);
            attendeeLegend.setPrefWidth(0);
        }, 4);

        CheckBox allRoomsCheckBox = new CheckBox("All rooms");
        allRoomsCheckBox.setSelected(false);
        accommodationGantt.parentsProvidedProperty().bind(allRoomsCheckBox.selectedProperty());

        CheckBox legendCheckBox = new CheckBox("Show Legend");
        legendCheckBox.setOnAction(e -> {
            attendeeLegend.setVisible(true);
            Animations.animateProperty(attendeeLegend.prefWidthProperty(), legendCheckBox.isSelected() ? prefWidth + 5 : 0);
        });
        HBox bottomPane = new HBox(10, allRoomsCheckBox, legendCheckBox, this);
        bottomPane.setBackground(new Background(new BackgroundFill(Color.web("#e0dcdc"), null, null)));
        bottomPane.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(this, Priority.ALWAYS);
        borderPane.setBottom(bottomPane);

        return borderPane;
    }
}
