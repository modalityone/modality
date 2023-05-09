package one.modality.hotel.backoffice.activities.accommodation;

import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import one.modality.base.shared.entities.Attendance;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AccommodationSummaryPane extends HBox {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy");
    private List<Attendance> attendances = Collections.emptyList();
    private LocalDate date = LocalDate.now();

    public void setEntities(List<Attendance> attendances) {
        this.attendances = new ArrayList<>(attendances);
        refresh();
    }

    private void refresh() {
        List<Attendance> attendancesForDate = filterAttendancesByDate();
        long numRoomsOccupied = countRoomsOccupied(attendancesForDate);
        long numGuests = countGuests(attendancesForDate);

        getChildren().clear();
        getChildren().add(new Label("Status, " + formatDate()));
        getChildren().add(new Label("Rooms occupied: " + numRoomsOccupied));
        getChildren().add(new Label("Guests: " + numGuests));
    }

    private String formatDate() {
        return DATE_FORMATTER.format(date);
    }

    private List<Attendance> filterAttendancesByDate() {
        return attendances.stream()
                .filter(attendance -> matchesDate(attendance.getDate()))
                .collect(Collectors.toList());
    }

    private boolean matchesDate(LocalDate otherDate) {
        return date.getYear() == otherDate.getYear() &&
                date.getMonth() == otherDate.getMonth() &&
                date.getDayOfMonth() == otherDate.getDayOfMonth();
    }

    private long countRoomsOccupied(List<Attendance> attendancesForDate) {
        return attendancesForDate.stream()
                .map(Attendance::getResourceConfiguration)
                .distinct()
                .count();
    }

    private long countGuests(List<Attendance> attendancesForDate) {
        return attendancesForDate.size();
    }
}
