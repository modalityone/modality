package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.extras.scalepane.ScalePane;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.ui.fxraiser.FXRaiser;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.util.LinkedHashMap;
import java.util.Map;

public class DietaryOptionKeyPanel extends VBox {

    private static final Color TITLE_TEXT_COLOR = Color.web("#0096d6");
    private final VBox labelsVBox = new VBox(2);

    public DietaryOptionKeyPanel() {
        Label titleLabel = new Label();
        I18n.bindI18nTextProperty(titleLabel.textProperty(), "Index");
        titleLabel.setTextFill(TITLE_TEXT_COLOR);
        getChildren().setAll(titleLabel, labelsVBox);
    }

    public void populate(LinkedHashMap<String, String> dietaryOptionSvgs) {
        Platform.runLater(() -> labelsVBox.getChildren().clear());
        addRows(dietaryOptionSvgs);
        setAlignment(Pos.CENTER);
        setFillWidth(false);
    }

    private void addRows(LinkedHashMap<String, String> dietaryOptionSvgs) {
        for (Map.Entry<String, String> entry : dietaryOptionSvgs.entrySet()) {
            String graphic = entry.getValue();
            Node node = FXRaiser.raiseToNode(graphic);
            if (node instanceof SVGPath) {
                ((SVGPath) node).setFill(Color.BLACK);
            }
            ScalePane scalePane = new ScalePane(node);

            Label dietaryOptionLabel = new Label(entry.getKey());
            dietaryOptionLabel.setGraphic(scalePane);
            dietaryOptionLabel.setGraphicTextGap(4);

            Platform.runLater(() -> labelsVBox.getChildren().add(dietaryOptionLabel));
        }
    }
}
