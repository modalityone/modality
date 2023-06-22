package one.modality.all.frontoffice.application;

import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.ui.action.Action;
import dev.webfx.stack.ui.action.ActionBinder;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Labeled;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import one.modality.base.client.application.ModalityClientFrameContainerActivity;
import one.modality.base.client.application.RoutingActions;

import java.util.List;

public class ModalityFrontOfficeFrameContainerActivity extends ModalityClientFrameContainerActivity {
    private static <T extends Labeled> T bindI18N(T node, String key) {
        return I18nControls.bindI18nProperties(node, key);
    }
    private Button createBottomButton() {
        Button button = new Button(); // bindI18N(new Button(), key);

        button.setPrefHeight(100);
        button.setMaxWidth(1000);
        button.setMinWidth(0);
        button.setAlignment(Pos.CENTER);
        button.setContentDisplay(ContentDisplay.TOP);

        return button;
    }
    @Override
    protected Region createContainerFooter() {
        GridPane buttonsGridPane = new GridPane();

        List<Action> actions = (List<Action>) RoutingActions.filterRoutingActions(this, this, "RouteToHome", "RouteToBooking", "RouteToAlerts", "RouteToAccount");

        for (int i = 0; i < actions.size(); i++) {
            Action action = actions.get(i);
            Button button = createBottomButton();

            ActionBinder.bindButtonToAction(button, action);

            buttonsGridPane.add(button, i, 0);

            int unitSize = (int) (100/actions.size());

            if (i == 0) {
                unitSize = 100 - unitSize*(actions.size()-1);
            }

            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(unitSize);
            buttonsGridPane.getColumnConstraints().add(col);
        }

        return buttonsGridPane;
    }
}
