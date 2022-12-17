package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.ui.util.border.BorderFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import one.modality.base.shared.entities.Item;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class AttendanceMonthPanel extends GridPane {

    private static final String[] DAY_NAMES = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
    private static final Color DAY_NAME_TEXT_COLOR = Color.web("#0096d6");
    private static final double HORIZONTAL_GAP = 10;
    private static final double VERTICAL_GAP = 10;

    public AttendanceMonthPanel(AttendanceCounts attendanceCounts, LocalDate month, List<Item> displayedMeals, AbbreviationGenerator abbreviationGenerator) {
        setHgap(HORIZONTAL_GAP);
        if (attendanceCounts == null)
            addDayNames(true);
        else {
            addDayNames(false);
            addDayPanels(attendanceCounts, month, displayedMeals, abbreviationGenerator);
            setVgap(VERTICAL_GAP);
        }
    }

    private void addDayNames(boolean visible) {
        int columnIndex = 0;
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setPercentWidth(100d / 7);
        getColumnConstraints().clear();
        for (String dayName : DAY_NAMES) {
            getColumnConstraints().add(columnConstraints);
            if (!visible)
                continue;
            Text dayNameText = new Text();
            I18n.bindI18nTextProperty(dayNameText.textProperty(), dayName);
            dayNameText.setFill(DAY_NAME_TEXT_COLOR);
            StackPane borderPane = new StackPane(dayNameText);
            borderPane.setBorder(BorderFactory.newBorder(Color.LIGHTGRAY, 7));
            borderPane.setMinHeight(30);
            add(borderPane, columnIndex, 0);
            columnIndex++;
        }
    }

    private void addDayPanels(AttendanceCounts attendanceCounts, LocalDate month, List<Item> displayedMeals, AbbreviationGenerator abbreviationGenerator) {
        int numDaysInMonth = YearMonth.of(month.getYear(), month.getMonth()).lengthOfMonth();
        int rowIndex = 1;
        for (int day = 0; day < numDaysInMonth; day++) {
            LocalDate dayDate = LocalDate.of(month.getYear(), month.getMonth(), day + 1);
            int columnIndex = dayDate.getDayOfWeek().ordinal();
            AttendanceDayPanel dayPanel = new AttendanceDayPanel(attendanceCounts, dayDate, displayedMeals, abbreviationGenerator);
            add(dayPanel, columnIndex, rowIndex);
            if (columnIndex == 6) {
                rowIndex++;
            }
        }
    }
}
