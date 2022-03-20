package mongoose.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.framework.client.ui.controls.entity.sheet.EntityPropertiesSheet;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
    private final ScrollPane gridScrollPane;

    public MoneyAccountEditorPane(ObservableList<MoneyAccountPane> moneyAccountPanes, ObservableList<MoneyFlowArrowView> moneyFlowArrowViews) {
        setPrefWidth(400);
        setStyle("-fx-background-color: lightgray;");
        fromMoneyAccountListGrid = new MoneyAccountFromListGrid(moneyAccountPanes, moneyFlowArrowViews);
        toMoneyAccountListGrid = new MoneyAccountToListGrid(moneyAccountPanes, moneyFlowArrowViews);
        VBox gridVbox = new VBox(
                buildHeadingLabel("Money flows from"),
                fromMoneyAccountListGrid,
                buildHeadingLabel("Money flows to"),
                toMoneyAccountListGrid);
        gridScrollPane = new ScrollPane(gridVbox);
        showStartingMsg();
    }

    private void showStartingMsg() {
        Label label = new Label("Please select an existing money account to edit or click the \"+\" button to create a new money account.");
        label.setWrapText(true);
        getChildren().add(label);
    }

    public void edit(MoneyAccount moneyAccount) {
        getChildren().clear();

        Pane propertiesSheetPane = new Pane();
        propertiesSheetPane.prefWidthProperty().bind(widthProperty());
        propertiesSheetPane.setPrefHeight(300);
        EntityPropertiesSheet.editEntity(moneyAccount, "name,closed,currency,event,gatewayCompany,organization,type", propertiesSheetPane);
        getChildren().add(propertiesSheetPane);

        fromMoneyAccountListGrid.excludedMoneyAccount().set(moneyAccount);
        toMoneyAccountListGrid.excludedMoneyAccount().set(moneyAccount);
        getChildren().add(gridScrollPane);
    }

    public void cancelEdit() {
        getChildren().clear();
    }

    private Label buildHeadingLabel(String text) {
        Label label = new Label(text);
        label.setFont(HEADING_FONT);
        return label;
    }

}
