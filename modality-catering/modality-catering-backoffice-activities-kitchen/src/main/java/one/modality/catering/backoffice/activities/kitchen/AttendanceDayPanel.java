package one.modality.catering.backoffice.activities.kitchen;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Item;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AttendanceDayPanel extends GridPane {

    private static final Background BACKGROUND = new Background (new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(5), Insets.EMPTY));
    private static final Color DAY_NUMBER_TEXT_COLOR = Color.web("#0096d6");

    private DoubleProperty firstColumnWidthProperty = new SimpleDoubleProperty(0);

    public AttendanceDayPanel(AttendanceCounts attendanceCounts, LocalDate date, List<Item> displayedMeals) {
        List<Item> sortedDisplayedMeals = sortMeals(displayedMeals);
        setBackground(BACKGROUND);
        setPadding(new Insets(4));
        addDayOfMonthNumber(date);
        addMealTitles(sortedDisplayedMeals);
        addDietaryOptions(attendanceCounts);
        addMealCounts(attendanceCounts, date, sortedDisplayedMeals);
    }

    private List<Item> sortMeals(List<Item> meals) {
        return meals.stream()
                .sorted((meal1, meal2) -> Integer.compare(meal1.getOrd(), meal2.getOrd()))
                .collect(Collectors.toList());
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
            mealLabel.setAlignment(Pos.CENTER);
            bindMealLabelWidth(mealLabel, displayedMeals);
            add(mealLabel, columnIndex, 0);
            columnIndex++;
        }
    }

    private void bindMealLabelWidth(Label label, List<Item> displayedMeals) {
        label.prefWidthProperty().bind(widthProperty().subtract(firstColumnWidthProperty).divide(displayedMeals.size()));
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
            dietaryOptionLabel.widthProperty().addListener((observableValue, oldValue, newValue) -> {
                if (newValue.doubleValue() > firstColumnWidthProperty.doubleValue()) {
                    firstColumnWidthProperty.set(newValue.doubleValue());
                }
            });
            dietaryOptionLabel.setMinWidth(Region.USE_PREF_SIZE);
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
                int count = attendanceCounts.getCount(date, meal.getCode(), dietaryOption);
                Label countLabel = new Label(String.valueOf(count));
                countLabel.setAlignment(Pos.CENTER);
                bindMealLabelWidth(countLabel, displayedMeals);
                add(countLabel, columnIndex, rowIndex);
                rowIndex++;
            }
            columnIndex++;
        }
    }

}
