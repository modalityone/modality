package mongoose.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.framework.client.ui.controls.entity.sheet.EntityPropertiesSheet;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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
        setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        fromMoneyAccountListGrid = new MoneyAccountFromListGrid(moneyAccountPanes, moneyFlowArrowViews);
        toMoneyAccountListGrid = new MoneyAccountToListGrid(moneyAccountPanes, moneyFlowArrowViews);
        VBox gridVbox = new VBox(
                buildHeadingLabel("Money flows from"),
                fromMoneyAccountListGrid,
                buildHeadingLabel("Money flows to"),
                toMoneyAccountListGrid);
        gridScrollPane = new ScrollPane(gridVbox);
        VBox.setVgrow(gridScrollPane, Priority.ALWAYS);
        showStartingMsg();
    }

    private void showStartingMsg() {
        Label label = new Label("Please perform one of the following actions\n" +
                "1. Select an existing money account to edit.\n" +
                "2. Click the \"+\" button to create a new money account.\n" +
                "3. Create a new money flow by dragging from one money account to another.");
        label.setWrapText(true);
        getChildren().add(label);
    }

    public void edit(MoneyAccount moneyAccount) {
        getChildren().clear();

        Pane propertiesSheetPane = new Pane();
        propertiesSheetPane.prefWidthProperty().bind(widthProperty());
        propertiesSheetPane.setPrefHeight(300);
        EntityPropertiesSheet.editEntity(moneyAccount, "name,closed,currency,event,gatewayCompany,type", propertiesSheetPane);
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
