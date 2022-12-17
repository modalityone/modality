package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.stack.i18n.I18n;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class MonthSelectionPanel extends HBox {

    private static final Color BG_COLOR_SELECTED = Color.web("#0096d6");
    private static final Color BG_COLOR_UNSELECTED = Color.web("#838788");

    private final MonthSelectionPanelListener listener;
    private final Map<LocalDate, VBox> monthBoxes = new HashMap<>();

    private LocalDate selectedMonth;

    public MonthSelectionPanel(MonthSelectionPanelListener listener) {
        this.listener = listener;
        selectedMonth = LocalDate.now();
        HBox body = new HBox();
        //body.setPadding(new Insets(0, 0, 16, 0));
        body.setSpacing(8);
        LocalDate month = LocalDate.now().minusMonths(3);
        LocalDate latestDate = LocalDate.now().plusMonths(12);
        while (!month.isAfter(latestDate)) {
            body.getChildren().add(createMonthBox(month));
            month = month.plusMonths(1);
        }
        showSelection();
        getChildren().add(body);
    }

    private VBox createMonthBox(LocalDate month) {
        Label monthLabel = new Label();
        I18n.bindI18nTextProperty(monthLabel.textProperty(), "[" + month.getMonth().name()+ "] " + (month.getYear() % 1000));
        monthLabel.setTextFill(Color.WHITE);
        VBox monthBox = new VBox(monthLabel);
        monthBox.setAlignment(Pos.CENTER);
        monthBox.setPadding(new Insets(10, 4, 10, 4));
        monthBox.setOnMouseClicked(e -> {
            selectedMonth = month;
            showSelection();
            listener.onMonthSelected(month);
        });
        monthBox.setCursor(Cursor.HAND);
        monthBox.setPrefWidth(130);
        monthBoxes.put(month, monthBox);
        return monthBox;
    }

    public static String buildMonthDisplayText(LocalDate month) {
        return I18n.getI18nText(month.getMonth().name()) + " " + (month.getYear() % 1000);
    }

    private void showSelection() {
        for (Map.Entry<LocalDate, VBox> entry : monthBoxes.entrySet()) {
            if (isSelectedMonth(entry.getKey())) {
                entry.getValue().setBackground(new Background (new BackgroundFill(BG_COLOR_SELECTED, new CornerRadii(5), Insets.EMPTY)));
            } else {
                entry.getValue().setBackground(new Background (new BackgroundFill(BG_COLOR_UNSELECTED, new CornerRadii(5), Insets.EMPTY)));
            }
        }
    }

    private boolean isSelectedMonth(LocalDate month) {
        return selectedMonth != null && month.getMonth().equals(selectedMonth.getMonth()) && month.getYear() == selectedMonth.getYear();
    }

    public LocalDate getSelectedMonth() {
        return selectedMonth != null ? selectedMonth : LocalDate.now();
    }

    public interface MonthSelectionPanelListener {
        void onMonthSelected(LocalDate month);
    }
}
