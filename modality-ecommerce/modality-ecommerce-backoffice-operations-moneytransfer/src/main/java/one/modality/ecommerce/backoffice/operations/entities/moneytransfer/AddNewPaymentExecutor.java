package one.modality.ecommerce.backoffice.operations.entities.moneytransfer;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.ui.dialog.DialogCallback;
import dev.webfx.stack.ui.dialog.DialogUtil;
import dev.webfx.extras.util.layout.Layouts;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.payment.PaymentService;
import one.modality.ecommerce.payment.InitiatePaymentArgument;

final class AddNewPaymentExecutor {

    //private static final boolean REDIRECT = false;

    static Future<Void> executeRequest(AddNewPaymentRequest rq) {
        return execute(rq.getDocument(), rq.getParentContainer());
    }

    private static Future<Void> execute(Document document, Pane parentContainer) {
        // Note: quickly implemented for the 19/12/2022 demo
        WebView webView = new WebView();
        StackPane stackPane = new StackPane(webView);
        Layouts.setMinSize(stackPane, 500);
        Layouts.setPrefSize(stackPane, 500);
        Layouts.setMaxSizeToInfinite(stackPane);
        BorderPane borderPane = new BorderPane(stackPane);
        int amount = document.getPriceNet() - document.getPriceDeposit();
        PaymentService.initiatePayment(new InitiatePaymentArgument(amount, document.getPrimaryKey(), false, false))
                .onFailure(e -> Platform.runLater(() -> borderPane.setCenter(new Label(e.getMessage()))))
                .onSuccess(r -> Platform.runLater(() -> {
                    String htmlContent = r.getHtmlContent();
                    webView.getEngine().loadContent(htmlContent);
                }));
        Button cancelButton = new Button("Cancel");
        Layouts.setMaxWidthToInfinite(cancelButton);
        borderPane.setBottom(cancelButton);
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(borderPane, parentContainer);
        cancelButton.setOnAction(e -> dialogCallback.closeDialog());
        return Future.succeededFuture();
    }
}