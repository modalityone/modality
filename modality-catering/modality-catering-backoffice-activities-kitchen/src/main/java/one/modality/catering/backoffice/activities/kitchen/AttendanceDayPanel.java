package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.extras.scalepane.ScalePane;
import dev.webfx.platform.json.Json;
import dev.webfx.stack.ui.fxraiser.FXRaiser;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import one.modality.base.shared.entities.Item;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class AttendanceDayPanel extends GridPane {

    private static final Background BACKGROUND_DEFAULT = new Background (new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(5), Insets.EMPTY));
    private static final Background BACKGROUND_TODAY = new Background (new BackgroundFill(Color.web("#0096d6"), new CornerRadii(5), Insets.EMPTY));
    private static final Color DAY_NUMBER_TEXT_COLOR_DEFAULT = Color.web("#0096d6");
    private static final Color DAY_NUMBER_TEXT_COLOR_TODAY = Color.LIGHTGRAY;
    private static final Color DIETARY_OPTION_TEXT_COLOR_DEFAULT = Color.web("#838788");
    private static final Color DIETARY_OPTION_TEXT_COLOR_TODAY = Color.LIGHTGRAY;
    private static final String DIETARY_OPTION_TOTAL = "Total";
    private static final Color MEAL_COUNT_COLOR = Color.web("#317437");
    private static final Color MEAL_TEXT_COLOR_DEFAULT = Color.web("#909394");
    private static final Color MEAL_TEXT_COLOR_TODAY = Color.LIGHTGRAY;
    private static final Font MEAL_TEXT_FONT = Font.font(new Label().getFont().getFamily(), FontWeight.BOLD, new Label().getFont().getSize());

    private DoubleProperty graphicColumnWidthProperty = new SimpleDoubleProperty(0);
    private DoubleProperty dietaryOptionColumnWidthProperty = new SimpleDoubleProperty(0);

    public AttendanceDayPanel(AttendanceCounts attendanceCounts, LocalDate date, List<Item> displayedMeals, AbbreviationGenerator abbreviationGenerator) {
        List<Item> sortedDisplayedMeals = sortMeals(attendanceCounts, date, displayedMeals);
        setBackground(isToday(date) ? BACKGROUND_TODAY : BACKGROUND_DEFAULT);
        setPadding(new Insets(4));
        addDayOfMonthNumber(date);
        addMealTitles(date, sortedDisplayedMeals, abbreviationGenerator);
        addDietaryOptions(date, attendanceCounts);
        addMealCounts(attendanceCounts, date, sortedDisplayedMeals);
    }

    private static boolean isToday(LocalDate date) {
        LocalDate now = LocalDate.now();
        return date.getDayOfMonth() == now.getDayOfMonth() && date.getMonthValue() == now.getMonthValue() && date.getYear() == now.getYear();
    }

    private List<Item> sortMeals(AttendanceCounts attendanceCounts, LocalDate date, List<Item> meals) {
        return meals.stream()
                .filter(meal -> attendanceCounts.getCount(date, meal.getName(), DIETARY_OPTION_TOTAL) > 0)
                .sorted((meal1, meal2) -> Integer.compare(meal1.getOrd(), meal2.getOrd()))
                .collect(Collectors.toList());
    }

    private void addDayOfMonthNumber(LocalDate date) {
        int dayNumber = date.getDayOfMonth();
        Label dayNumberLabel = new Label(padWithLeadingZero(dayNumber));
        dayNumberLabel.setFont(MEAL_TEXT_FONT);
        dayNumberLabel.setTextFill(isToday(date) ? DAY_NUMBER_TEXT_COLOR_TODAY : DAY_NUMBER_TEXT_COLOR_DEFAULT);
        add(dayNumberLabel, 1, 0);
    }

    private String padWithLeadingZero(int dayNumber) {
        return dayNumber > 9 ? String.valueOf(dayNumber) : "0" + String.valueOf(dayNumber);
    }

    private void addMealTitles(LocalDate date, List<Item> displayedMeals, AbbreviationGenerator abbreviationGenerator) {
        int columnIndex = 2;
        for (Item meal : displayedMeals) {
            String mealTitle = abbreviationGenerator.getAbbreviation(meal.getName());
            Label mealLabel = new Label(mealTitle);
            mealLabel.setFont(MEAL_TEXT_FONT);
            mealLabel.setTextFill(isToday(date) ? MEAL_TEXT_COLOR_TODAY : MEAL_TEXT_COLOR_DEFAULT);
            mealLabel.setAlignment(Pos.CENTER);
            bindMealLabelWidth(mealLabel, displayedMeals);
            add(mealLabel, columnIndex, 0);
            columnIndex++;
        }
    }

    private void bindMealLabelWidth(Label label, List<Item> displayedMeals) {
        label.prefWidthProperty().bind(widthProperty().subtract(graphicColumnWidthProperty).subtract(dietaryOptionColumnWidthProperty).divide(displayedMeals.size()));
    }

    private void addDietaryOptions(LocalDate date, AttendanceCounts attendanceCounts) {
        Color color = isToday(date) ? DIETARY_OPTION_TEXT_COLOR_TODAY : DIETARY_OPTION_TEXT_COLOR_DEFAULT;
        List<String> dietaryOptions = attendanceCounts.getSortedDietaryOptions();
        int rowIndex = 1;
        for (String dietaryOption : dietaryOptions) {
            String svg = attendanceCounts.getSvgForDietaryOption(dietaryOption);
            if (svg != null) {
                Node node = FXRaiser.raiseToNode(Json.parseObject(svg));
                if (node instanceof SVGPath) {
                    ((SVGPath) node).setFill(color);
                }
                ScalePane scalePane = new ScalePane(node);
                scalePane.setPadding(new Insets(0, 4, 0, 0));
                scalePane.widthProperty().addListener((observableValue, oldValue, newValue) -> {
                    if (newValue.doubleValue() > graphicColumnWidthProperty.doubleValue()) {
                        graphicColumnWidthProperty.set(newValue.doubleValue());
                    }
                });
                add(scalePane, 0, rowIndex);
            }

            Label dietaryOptionLabel = new Label(dietaryOption);
            dietaryOptionLabel.setTextFill(color);
            dietaryOptionLabel.setMinWidth(Region.USE_PREF_SIZE);
            dietaryOptionLabel.widthProperty().addListener((observableValue, oldValue, newValue) -> {
                if (newValue.doubleValue() > dietaryOptionColumnWidthProperty.doubleValue()) {
                    dietaryOptionColumnWidthProperty.set(newValue.doubleValue());
                }
            });
            add(dietaryOptionLabel, 1, rowIndex);

            rowIndex++;
        }
    }

    private void addMealCounts(AttendanceCounts attendanceCounts, LocalDate date, List<Item> displayedMeals) {
        List<String> dietaryOptions = attendanceCounts.getSortedDietaryOptions();
        int columnIndex = 2;
        for (Item meal : displayedMeals) {
            int rowIndex = 1;
            for (String dietaryOption : dietaryOptions) {
                int count = attendanceCounts.getCount(date, meal.getName(), dietaryOption);
                Label countLabel = new Label(String.valueOf(count));
                countLabel.setTextFill(MEAL_COUNT_COLOR);
                countLabel.setAlignment(Pos.CENTER);
                bindMealLabelWidth(countLabel, displayedMeals);
                add(countLabel, columnIndex, rowIndex);
                rowIndex++;
            }
            columnIndex++;
        }
    }

}
