package one.modality.catering.backoffice.activities.kitchen;

import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

public class AttendanceMonthPanel extends GridPane {

    private static final List<String> DAY_NAMES = Arrays.asList("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");

    public AttendanceMonthPanel(AttendanceCounts attendanceCounts, LocalDate month) {
        if (attendanceCounts != null) {
            addDayNames();
            addDayPanels(attendanceCounts, month);
        }
    }

    private void addDayNames() {
        int columnIndex = 0;
        for (String dayName : DAY_NAMES) {
            Text dayNameText = new Text(dayName);
            add(dayNameText, columnIndex, 0);
            columnIndex++;
        }
    }

    private void addDayPanels(AttendanceCounts attendanceCounts, LocalDate month) {
        int numDaysInMonth = YearMonth.of(month.getYear(), month.getMonth()).lengthOfMonth();
        int rowIndex = 1;
        for (int day = 0; day < numDaysInMonth; day++) {
            LocalDate dayDate = LocalDate.of(month.getYear(), month.getMonth(), day + 1);
            int columnIndex = dayDate.getDayOfWeek().ordinal();
            AttendanceDayPanel dayPanel = new AttendanceDayPanel(attendanceCounts, dayDate);
            add(dayPanel, columnIndex, rowIndex);
            if (columnIndex == 6) {
                rowIndex++;
            }
        }
    }
}
