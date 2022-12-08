package one.modality.catering.backoffice.activities.kitchen;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Item;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AttendanceDayPanel extends GridPane {

    private static final Color DAY_NUMBER_TEXT_COLOR = Color.web("#0096d6");

    public AttendanceDayPanel(AttendanceCounts attendanceCounts, LocalDate date, List<Item> displayedMeals) {
        addDayOfMonthNumber(date);
        addMealTitles(displayedMeals);
        addDietaryOptions(attendanceCounts);
        addMealCounts(attendanceCounts, date, displayedMeals);
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

    private void addMealTitles(List<Item> displayedMeals) {
        int columnIndex = 1;
        for (Item meal : displayedMeals) {
            String mealTitle = getMealAbbreviation(meal.getName());
            Label mealLabel = new Label(mealTitle);
            add(mealLabel, columnIndex, 0);
            columnIndex++;
        }
    }

    private static String getMealAbbreviation(String mealName) {
        try {
            return Stream.of(mealName.split(" "))
                    .map(s -> String.valueOf(s.charAt(0)))
                    .collect(Collectors.joining());
        } catch (Exception e) {
            return mealName;
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

    private void addMealCounts(AttendanceCounts attendanceCounts, LocalDate date, List<Item> displayedMeals) {
        List<String> dietaryOptions = attendanceCounts.getSortedDietaryOptions();
        int columnIndex = 1;
        for (Item meal : displayedMeals) {
            int rowIndex = 1;
            for (String dietaryOption : dietaryOptions) {
                int count = attendanceCounts.getCount(date, meal.getName(), dietaryOption);
                Label countLabel = new Label(String.valueOf(count));
                add(countLabel, columnIndex, rowIndex);
                rowIndex++;
            }
            columnIndex++;
        }
    }

}
