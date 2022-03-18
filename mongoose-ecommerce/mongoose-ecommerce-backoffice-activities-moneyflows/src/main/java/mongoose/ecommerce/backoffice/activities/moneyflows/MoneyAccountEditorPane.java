package mongoose.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.framework.client.ui.controls.entity.sheet.EntityPropertiesSheet;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import mongoose.base.shared.entities.MoneyAccount;

/**
 * @author Dan Newman
 */
public class MoneyAccountEditorPane extends VBox {

    private static final Font HEADING_FONT = Font.font("Verdana", FontWeight.BOLD, 14);

    private final MoneyAccountFromListGrid fromMoneyAccountListGrid;
    private final MoneyAccountToListGrid toMoneyAccountListGrid;

    public MoneyAccountEditorPane(ObservableList<MoneyAccountPane> moneyAccountPanes, ObservableList<MoneyFlowArrowView> moneyFlowArrowViews) {
        setPrefWidth(400);
        setStyle("-fx-background-color: lightgray;");
        fromMoneyAccountListGrid = new MoneyAccountFromListGrid(moneyAccountPanes, moneyFlowArrowViews);
        toMoneyAccountListGrid = new MoneyAccountToListGrid(moneyAccountPanes, moneyFlowArrowViews);
    }

    public void edit(MoneyAccount moneyAccount) {
        getChildren().clear();

        Pane propertiesSheetPane = new Pane();
        propertiesSheetPane.prefWidthProperty().bind(widthProperty());
        propertiesSheetPane.setPrefHeight(300);
        EntityPropertiesSheet.editEntity(moneyAccount, "name,closed,currency,event,gatewayCompany,organization,type", propertiesSheetPane);
        getChildren().add(propertiesSheetPane);

        getChildren().add(buildHeadingLabel("Money flows from"));
        fromMoneyAccountListGrid.excludedMoneyAccount().set(moneyAccount);
        getChildren().add(fromMoneyAccountListGrid);

        getChildren().add(buildHeadingLabel("Money flows to"));
        toMoneyAccountListGrid.excludedMoneyAccount().set(moneyAccount);
        getChildren().add(toMoneyAccountListGrid);
    }

    private Label buildHeadingLabel(String text) {
        Label label = new Label(text);
        label.setFont(HEADING_FONT);
        return label;
    }

}
