package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.extras.scalepane.ScalePane;
import dev.webfx.platform.json.Json;
import dev.webfx.stack.ui.fxraiser.FXRaiser;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import one.modality.base.shared.entities.Organization;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class DietaryOptionKeyPanel extends VBox {

    private static final Font NO_DATA_MSG_FONT = Font.font(new Label().getFont().getFamily(), FontWeight.BOLD, 28);
    private static final Color NO_DATA_MSG_TEXT_COLOR = Color.web("#0096d6");
    private static final Color TITLE_TEXT_COLOR = Color.web("#0096d6");

    public void populate(LinkedHashMap<String, String> dietaryOptionSvgs) {
        Platform.runLater(() -> getChildren().clear());
        addTitle();
        addRows(dietaryOptionSvgs);
    }

    private void addTitle() {
        Label titleLabel = new Label("Index");
        titleLabel.setTextFill(TITLE_TEXT_COLOR);
        Platform.runLater(() -> getChildren().add(titleLabel));
    }

    private void addRows(LinkedHashMap<String, String> dietaryOptionSvgs) {
        for (Map.Entry<String, String> entry : dietaryOptionSvgs.entrySet()) {
            String graphic = entry.getValue();
            Node node = FXRaiser.raiseToNode(Json.parseObject(graphic));
            if (node instanceof SVGPath) {
                ((SVGPath) node).setFill(Color.BLACK);
            }
            ScalePane scalePane = new ScalePane(node);

            Label dietaryOptionLabel = new Label(entry.getKey());

            HBox row = new HBox(scalePane, dietaryOptionLabel);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(4, 0, 0, 0));
            row.setSpacing(4);

            Platform.runLater(() -> getChildren().add(row));
        }
    }

    public void showNoDataMsg(Organization organization, LocalDate month) {
        String monthString = MonthSelectionPanel.buildMonthDisplayText(month);
        String msg = "No meal data for " + monthString + " for " + organization.getName();
        Label noDataLabel = new Label(msg);
        noDataLabel.setTextFill(NO_DATA_MSG_TEXT_COLOR);
        noDataLabel.setFont(NO_DATA_MSG_FONT);
        noDataLabel.setWrapText(true);
        Platform.runLater(() -> getChildren().setAll(noDataLabel));
    }
}
