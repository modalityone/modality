package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.extras.scalepane.ScalePane;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.ui.fxraiser.FXRaiser;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Map;

public class DietaryOptionKeyPanel extends VBox {

    private final HBox labelsBox = new HBox(10);
    Label titleLabel = new Label();

    public DietaryOptionKeyPanel() {
        I18n.bindI18nTextProperty(titleLabel.textProperty(), "Index:");
        TextTheme.createPrimaryTextFacet(titleLabel).style();
        labelsBox.setAlignment(Pos.CENTER);
        getChildren().setAll(labelsBox);
    }

    public void populate(Map<String, String> dietaryOptionSvgs) {
        Platform.runLater(() -> labelsBox.getChildren().setAll(titleLabel));
        addRows(dietaryOptionSvgs);
        setAlignment(Pos.CENTER);
        setFillWidth(false);
    }

    private void addRows(Map<String, String> dietaryOptionSvgs) {
        for (Map.Entry<String, String> entry : dietaryOptionSvgs.entrySet()) {
            String graphic = entry.getValue();
            Node node = FXRaiser.raiseToNode(graphic);
            ScalePane scalePane = new ScalePane(node);

            Label dietaryOptionLabel = new Label(entry.getKey());
            dietaryOptionLabel.setGraphic(scalePane);
            dietaryOptionLabel.setGraphicTextGap(4);

            TextTheme.createDefaultTextFacet(dietaryOptionLabel).style();

            Platform.runLater(() -> labelsBox.getChildren().add(dietaryOptionLabel));
        }
    }
}
