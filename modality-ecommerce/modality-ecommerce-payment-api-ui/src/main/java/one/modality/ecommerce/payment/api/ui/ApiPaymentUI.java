package one.modality.ecommerce.payment.api.ui;

import dev.webfx.platform.console.Console;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.ecommerce.payment.api.ApiPaymentService;
import one.modality.ecommerce.payment.api.MakeApiPaymentArgument;

/**
 * @author Bruno Salmon
 */
public class ApiPaymentUI {

    private final static int CC_NUMBER_LENGTH = 16;
    private final static int CC_EXPIRY_LENGTH = 4;
    private Button payBtn;
    private Button cancelPayBtn;

    public Node buildNode() {
        Label amountLbl = new Label("Amount:");
        Label ccNumberLbl = new Label("Credit card number:");
        Label expiryLbl = new Label("Expiry:");

        TextField amountTF = new TextField("100.00");
        //amountTF.setEditable(false);
        TextField ccNumberTF = new TextField();
        ccNumberTF.setPromptText("xxxx-xxxx-xxxx-xxxx");
        ccNumberTF.textProperty().addListener((observable, oldValue, newValue) -> {
            // Restrict input to 16 chars
            if (newValue.length() > CC_NUMBER_LENGTH) {
                ccNumberTF.setText(oldValue);
            }
        });

        TextField expiryTF = new TextField();
        expiryTF.setPromptText("mmyy");
        expiryTF.textProperty().addListener((observable, oldValue, newValue) -> {
            // Restrict input to 4 chars
            if (newValue.length() > CC_EXPIRY_LENGTH) {
                expiryTF.setText(oldValue);
            }
        });

        payBtn = new Button("Buy");
        cancelPayBtn = new Button("Cancel payment");
        cancelPayBtn.setDisable(true);


        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        //grid.setPadding(new Insets(0, 10, 0, 10));
        grid.add(amountLbl, 0, 0);
        grid.add(amountTF, 1, 0);

        grid.add(ccNumberLbl, 0, 1);
        grid.add(ccNumberTF, 1, 1);

        grid.add(expiryLbl, 0, 2);
        grid.add(expiryTF, 1, 2);

        HBox buttonHBX = new HBox();
        buttonHBX.setPadding(new Insets(5));
        buttonHBX.setSpacing(5);
        buttonHBX.getChildren().addAll(payBtn, cancelPayBtn);
        grid.add(buttonHBX, 1, 3);

        payBtn.setOnAction(e -> {
            payBtn.setDisable(true);
            cancelPayBtn.setDisable(true);
            ApiPaymentService.makeApiPayment(new MakeApiPaymentArgument((int) (100 * Double.parseDouble(amountTF.getText())), null, ccNumberTF.getText(), expiryTF.getText()))
                    .onSuccess(r -> Console.log(r.isSuccess() ? "Payment OK!" : "Payment KO!"))
                    .onFailure(r -> Console.log("Payment Error!"))
                    .onComplete(r -> Platform.runLater(() -> {
                        payBtn.setDisable(false);
                        cancelPayBtn.setDisable(false);
                    }));
        });
        grid.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        grid.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, null, BorderStroke.THIN)));
        grid.setPadding(new Insets(10));
        return grid;
    }

}
