package one.modality.ecommerce.payment.ui;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.webview.pane.LoadOptions;
import dev.webfx.extras.webview.pane.WebViewPane;
import dev.webfx.platform.browser.Browser;
import dev.webfx.platform.conf.ConfigLoader;
import dev.webfx.platform.console.Console;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.ecommerce.payment.InitiatePaymentResult;

import java.util.function.Consumer;

/**
 * @author Bruno Salmon
 */
public class PaymentUI {

    private final InitiatePaymentResult result;
    private final WebViewPane webViewPane = new WebViewPane();
    private Runnable onCancel;
    private Runnable onSuccess;
    private Consumer<String> onFailure;
    private final Button payButton = new Button("Pay");

    public PaymentUI(InitiatePaymentResult result) {
        this.result = result;
    }

    public PaymentUI setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
        return this;
    }

    public PaymentUI setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public PaymentUI setOnFailure(Consumer<String> onFailure) {
        this.onFailure = onFailure;
        return this;
    }

    public Region buildUI() {
        String url = result.getUrl();
        if (result.isRedirect()) {
            try {
                Browser.launchExternalBrowser(url);
            } catch (Exception e) {
                Console.log(e);
            }
            return null;
        }
        webViewPane.setMaxSize(600, 130);
        //webViewPane.setRedirectConsole(true); // causes stack overflow
        payButton.setDisable(true);
        LoadOptions loadOptions = new LoadOptions()
                .setOnLoadSuccess(() -> {
                    payButton.setDisable(false);
                    try {
                        webViewPane.setWindowMember("webfxJavaPaymentCallback", PaymentUI.this);
                        webViewPane.callWindow("webfxInjectJavaPaymentCallback", PaymentUI.this);
                    } catch (Exception ex) {
                        onGatewayFailure(ex.getMessage());
                    }
                });
        String htmlContent = result.getHtmlContent();
        if (htmlContent != null) {
            webViewPane.loadFromHtml(htmlContent, loadOptions, false);
        } else {
            if (url.startsWith("/")) {
                url = getHttpServerOrigin() + url;
            }
            webViewPane.loadFromUrl(url, loadOptions, false);
        }
        payButton.setMaxWidth(Double.MAX_VALUE);
        payButton.setOnMouseClicked(e -> {
            payButton.setDisable(true);
            try {
                Console.log("Calling webfxSubmitGatewayPayment() in payment form");
                webViewPane.callWindow("webfxSubmitGatewayPayment");
            } catch (Exception ex) {
                onGatewayFailure(ex.getMessage());
            }
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setOnMouseClicked(e -> {
            if (onCancel != null) {
                onCancel.run();
            }
        });
        return new VBox(webViewPane, new ColumnsPane(payButton, cancelButton));
    }

    // Callback methods (called back by the payment gateway script)

    public void onGatewaySuccess() {
        payButton.setDisable(false);
        if (onSuccess != null)
            onSuccess.run();
    }

    public void onGatewayFailure(String error) {
        payButton.setDisable(false);
        Console.log(error);
        if (onFailure != null)
            onFailure.accept(error);
    }

    private static String getHttpServerOrigin() {
        // return WindowLocation.getOrigin(); // TODO make this work
        String origin = evaluateOrNull("${{ HTTP_SERVER_ORIGIN }}");
        if (origin == null)
            origin = "https://" + evaluateOrNull("${{ HTTP_SERVER_HOST | BUS_SERVER_HOST | SERVER_HOST }}");
        return origin;
    }

    private static String evaluateOrNull(String expression) {
        String value = ConfigLoader.getRootConfig().get(expression);
        if (value == expression)
            value = null;
        return value;
    }

}
