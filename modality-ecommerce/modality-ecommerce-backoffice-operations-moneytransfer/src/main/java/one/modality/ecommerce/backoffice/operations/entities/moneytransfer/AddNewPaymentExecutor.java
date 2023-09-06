package one.modality.ecommerce.backoffice.operations.entities.moneytransfer;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.ui.dialog.DialogCallback;
import dev.webfx.stack.ui.dialog.DialogUtil;
import dev.webfx.extras.util.layout.LayoutUtil;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.payment.custom.CustomPaymentService;
import one.modality.ecommerce.payment.custom.InitiateCustomPaymentArgument;

final class AddNewPaymentExecutor {

    //private static final boolean DELEGATED = false;

    static Future<Void> executeRequest(AddNewPaymentRequest rq) {
        return execute(rq.getDocument(), rq.getParentContainer());
    }

    private static Future<Void> execute(Document document, Pane parentContainer) {
        // Note: quickly implemented for the 19/12/2022 demo
        WebView webView = new WebView();
        StackPane stackPane = new StackPane(webView);
        LayoutUtil.setMinSize(stackPane, 500);
        LayoutUtil.setPrefSize(stackPane, 500);
        LayoutUtil.setMaxSizeToInfinite(stackPane);
        BorderPane borderPane = new BorderPane(stackPane);
        String description = document.getEvent().getName() + " - Ref " + document.getRef();
        int amount = document.getPriceNet() - document.getPriceDeposit();
        String currency = "GBP"; // hardcoded for now...
        /*if (DELEGATED) // Doesn't work in the browser iFrame
            DelegatedPaymentService.initiateDelegatedPayment(new InitiateDelegatedPaymentArgument(description, amount, currency))
                    .onFailure(e -> Platform.runLater(() -> borderPane.setCenter(new Label(e.getMessage()))))
                    .onSuccess(r -> Platform.runLater(() -> {
                        String paymentUrl = r.getDelegatedPaymentUrl();
                        webView.getEngine().load(paymentUrl);
                    }));
        else*/
            CustomPaymentService.initiateCustomPayment(new InitiateCustomPaymentArgument(amount, currency, description, 1, null, null, null))
                    .onFailure(e -> Platform.runLater(() -> borderPane.setCenter(new Label(e.getMessage()))))
                    .onSuccess(r -> Platform.runLater(() -> {
                        String htmlContent = r.getHtmlContent();
                        webView.getEngine().loadContent(htmlContent);
                    }));
        Button cancelButton = new Button("Cancel");
        LayoutUtil.setMaxWidthToInfinite(cancelButton);
        borderPane.setBottom(cancelButton);
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(borderPane, parentContainer);
        cancelButton.setOnAction(e -> dialogCallback.closeDialog());
        return Future.succeededFuture();
    }
}