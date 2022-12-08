package one.modality.catering.backoffice.activities.kitchen;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.util.List;

public class AttendanceDayPanel extends GridPane {

    private static final Color DAY_NUMBER_TEXT_COLOR = Color.web("#0096d6");

    public AttendanceDayPanel(AttendanceCounts attendanceCounts, LocalDate date) {
        addDayOfMonthNumber(date);
        addMealTitles(attendanceCounts);
        addDietaryOptions(attendanceCounts);
        addMealCounts(attendanceCounts, date);
    }

    private void addDayOfMonthNumber(LocalDate date) {
        int dayNumber = date.getDayOfMonth();
        Label dayNumberLabel = new Label(padWithLeadingZero(dayNumber));
        dayNumberLabel.setTextFill(DAY_NUMBER_TEXT_COLOR);
        add(dayNumberLabel, 0, 0);
    }

    private String padWithLeadingZero(int dayNumber) {
        return dayNumber > 9 ? String.valueOf(dayNumber) : "0" + String.valueOf(dayNumber);
    }

    private void addMealTitles(AttendanceCounts attendanceCounts) {
        List<String> meals = attendanceCounts.getSortedMeals();
        int columnIndex = 1;
        for (String meal : meals) {
            Label mealLabel = new Label(meal);
            add(mealLabel, columnIndex, 0);
            columnIndex++;
        }
    }

    private void addDietaryOptions(AttendanceCounts attendanceCounts) {
        List<String> dietaryOptions = attendanceCounts.getSortedDietaryOptions();
        int rowIndex = 1;
        for (String dietaryOption : dietaryOptions) {
            Label dietaryOptionLabel = new Label(dietaryOption);
            add(dietaryOptionLabel, 0, rowIndex);
            rowIndex++;
        }
    }

    private void addMealCounts(AttendanceCounts attendanceCounts, LocalDate date) {
        List<String> meals = attendanceCounts.getSortedMeals();
        List<String> dietaryOptions = attendanceCounts.getSortedDietaryOptions();
        int columnIndex = 1;
        for (String meal : meals) {
            int rowIndex = 1;
            for (String dietaryOption : dietaryOptions) {
                int count = attendanceCounts.getCount(date, meal, dietaryOption);
                Label countLabel = new Label(String.valueOf(count));
                add(countLabel, columnIndex, rowIndex);
                rowIndex++;
            }
            columnIndex++;
        }
    }

}
