package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.extras.scalepane.ScalePane;
import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.shape.ShapeTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.stack.ui.fxraiser.FXRaiser;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.FontWeight;

import one.modality.base.shared.entities.Item;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AttendanceDayPanel extends GridPane {

    private static final String DIETARY_OPTION_TOTAL = "Total";
    private static final FontDef MEAL_COUNT_FONT = FontDef.font(FontWeight.NORMAL, 14);
    private static final FontDef MEAL_TOTAL_COUNT_FONT = FontDef.font(FontWeight.BOLD, 14);
    private static final FontDef MEAL_TEXT_FONT = FontDef.font(FontWeight.BOLD, 15);

    public AttendanceDayPanel(
            AttendanceCounts attendanceCounts,
            LocalDate date,
            List<Item> displayedMeals,
            AbbreviationGenerator abbreviationGenerator) {
        List<Item> sortedDisplayedMeals = sortMeals(attendanceCounts, date, displayedMeals);
        setPadding(new Insets(4));
        addDayOfMonthNumber(date);
        addMealTitles(date, sortedDisplayedMeals, abbreviationGenerator);
        addDietaryOptions(date, attendanceCounts);
        addMealCounts(attendanceCounts, date, sortedDisplayedMeals);
        setVgap(2); // To avoid SVGs to touch each others
    }

    private List<Item> sortMeals(
            AttendanceCounts attendanceCounts, LocalDate date, List<Item> meals) {
        return meals.stream()
                .filter(
                        meal ->
                                attendanceCounts.getCount(
                                                date, meal.getName(), DIETARY_OPTION_TOTAL)
                                        > 0)
                .sorted(Comparator.comparingInt(Item::getOrd))
                .collect(Collectors.toList());
    }

    private void addDayOfMonthNumber(LocalDate date) {
        int dayNumber = date.getDayOfMonth();
        Label dayNumberLabel = new Label(padWithLeadingZero(dayNumber));
        TextTheme.createPrimaryTextFacet(dayNumberLabel).requestedFont(MEAL_TEXT_FONT).style();
        GridPane.setHalignment(dayNumberLabel, HPos.CENTER);
        GridPane.setColumnSpan(dayNumberLabel, 2);
        add(dayNumberLabel, 0, 0);
    }

    private String padWithLeadingZero(int dayNumber) {
        return dayNumber > 9 ? String.valueOf(dayNumber) : "0" + dayNumber;
    }

    private void addMealTitles(
            LocalDate date,
            List<Item> displayedMeals,
            AbbreviationGenerator abbreviationGenerator) {
        int columnIndex = 2;
        ColumnConstraints percentConstraints = new ColumnConstraints();
        percentConstraints.setPercentWidth(100d / (displayedMeals.size() + 2));
        getColumnConstraints().clear();
        ColumnConstraints hGrowConstraints = new ColumnConstraints();
        hGrowConstraints.setHgrow(Priority.ALWAYS);
        getColumnConstraints().add(hGrowConstraints);
        getColumnConstraints().add(hGrowConstraints);
        for (Item meal : displayedMeals) {
            String mealTitle = abbreviationGenerator.getAbbreviation(meal.getName());
            Label mealLabel = new Label(mealTitle);
            TextTheme.createSecondaryTextFacet(mealLabel).requestedFont(MEAL_TEXT_FONT).style();
            GridPane.setHalignment(mealLabel, HPos.CENTER);
            add(mealLabel, columnIndex, 0);
            getColumnConstraints().add(percentConstraints);
            columnIndex++;
        }
    }

    private void addDietaryOptions(LocalDate date, AttendanceCounts attendanceCounts) {
        List<String> dietaryOptions = attendanceCounts.getSortedDietaryOptions();
        int rowIndex = 1;
        for (String dietaryOption : dietaryOptions) {
            String svg = attendanceCounts.getSvgForDietaryOption(dietaryOption);
            if (svg != null) {
                Node node = FXRaiser.raiseToNode(svg);
                ShapeTheme.createSecondaryShapeFacet(node).style();
                ScalePane scalePane = new ScalePane(node);
                scalePane.setPadding(new Insets(0, 4, 0, 0));
                add(scalePane, 0, rowIndex);
            }

            if (!"Total".equals(dietaryOption)) {
                Label dietaryOptionLabel = new Label(dietaryOption);
                TextTheme.createSecondaryTextFacet(dietaryOptionLabel).style();
                boolean unspecifiedDiet = "?".equals(dietaryOption);
                if (unspecifiedDiet) {
                    GridPane.setHalignment(dietaryOptionLabel, HPos.CENTER);
                    add(dietaryOptionLabel, 0, rowIndex);
                } else {
                    dietaryOptionLabel.setPadding(new Insets(0, 0, 0, 5));
                    add(dietaryOptionLabel, 1, rowIndex);
                }
            }

            rowIndex++;
        }
    }

    private void addMealCounts(
            AttendanceCounts attendanceCounts, LocalDate date, List<Item> displayedMeals) {
        List<String> dietaryOptions = attendanceCounts.getSortedDietaryOptions();
        int columnIndex = 2;
        for (Item meal : displayedMeals) {
            int rowIndex = 1;
            for (String dietaryOption : dietaryOptions) {
                boolean isTotal = "Total".equals(dietaryOption);
                int count = attendanceCounts.getCount(date, meal.getName(), dietaryOption);
                Label countLabel = new Label(String.valueOf(count));
                TextTheme.createDefaultTextFacet(countLabel)
                        .requestedFont(isTotal ? MEAL_TOTAL_COUNT_FONT : MEAL_COUNT_FONT)
                        .style();
                GridPane.setHalignment(countLabel, HPos.CENTER);
                add(countLabel, columnIndex, rowIndex);
                rowIndex++;
            }
            columnIndex++;
        }
    }
}
