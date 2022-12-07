package one.modality.catering.backoffice.activities.kitchen;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class MonthSelectionPanel extends HBox {

    private final MonthSelectionPanelListener listener;
    private final Map<LocalDate, VBox> monthBoxes = new HashMap<>();

    private LocalDate selectedMonth;
    private ScrollPane scrollPane;
    public MonthSelectionPanel(MonthSelectionPanelListener listener) {
        this.listener = listener;
        selectedMonth = LocalDate.now();
        HBox body = new HBox();
        LocalDate month = LocalDate.now().minusYears(2);
        LocalDate latestDate = LocalDate.now().plusYears(2);
        while (!month.isAfter(latestDate)) {
            body.getChildren().add(createMonthBox(month));
            month = month.plusMonths(1);
        }
        showSelection();
        scrollPane = new ScrollPane(body);
        getChildren().add(scrollPane);
    }

    private VBox createMonthBox(LocalDate month) {
        String text = month.getMonth() + " " + month.getYear();
        Label monthLabel = new Label(text);
        VBox monthBox = new VBox(monthLabel);
        monthBox.setPadding(new Insets(0, 4, 0, 4));
        monthBox.setOnMouseClicked(e -> {
            selectedMonth = month;
            showSelection();
            listener.onMonthSelected(month);
        });
        monthBoxes.put(month, monthBox);
        return monthBox;
    }

    private void showSelection() {
        for (Map.Entry<LocalDate, VBox> entry : monthBoxes.entrySet()) {
            if (isSelectedMonth(entry.getKey())) {
                entry.getValue().setBackground(new Background(new BackgroundFill(Color.YELLOW, CornerRadii.EMPTY, Insets.EMPTY)));
            } else {
                entry.getValue().setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        }
    }

    private boolean isSelectedMonth(LocalDate month) {
        return selectedMonth != null && month.getMonth().equals(selectedMonth.getMonth()) && month.getYear() == selectedMonth.getYear();
    }

    public interface MonthSelectionPanelListener {
        void onMonthSelected(LocalDate month);
    }
}
