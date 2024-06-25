package one.modality.ecommerce.payment.client;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.webview.pane.LoadOptions;
import dev.webfx.extras.webview.pane.WebViewPane;
import dev.webfx.platform.browser.Browser;
import dev.webfx.platform.conf.ConfigLoader;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.scheduler.Scheduled;
import dev.webfx.platform.scheduler.Scheduler;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.markers.HasPersonalDetails;
import one.modality.ecommerce.payment.InitiatePaymentResult;

import java.util.function.Consumer;

/**
 * @author Bruno Salmon
 */
public class WebPaymentForm {

    private static final boolean DEBUG = true;

    private final InitiatePaymentResult result;
    private final HasPersonalDetails buyerPersonalDetails;
    private final WebViewPane webViewPane = new WebViewPane();
    private boolean inited;
    private Consumer<String> onLoadFailure; // Called when the webview failed to load
    private Consumer<String> onInitFailure; // Called when the payment page failed to initialised (otherwise the card details should appear)
    private Consumer<String> onGatewayFailure; // Called when the gateway failed to create the payment (just after the buyer pressed Pay)
    private Consumer<String> onModalityFailure; // Called when Modality couldn't handle the payment ()
    private Consumer<PaymentStatus> onFinalStatus;
    private Runnable onBuyerCancel;
    private final Button payButton = new Button("Pay");
    private final Button cancelButton = new Button("Cancel");
    private Scheduled initFailureChecker;

    public WebPaymentForm(InitiatePaymentResult result, HasPersonalDetails buyerPersonalDetails) {
        this.result = result;
        this.buyerPersonalDetails = buyerPersonalDetails;
    }

    public WebPaymentForm setOnLoadFailure(Consumer<String> onLoadFailure) {
        this.onLoadFailure = onLoadFailure;
        return this;
    }

    public WebPaymentForm setOnInitFailure(Consumer<String> onInitFailure) {
        this.onInitFailure = onInitFailure;
        return this;
    }

    public WebPaymentForm setOnGatewayFailure(Consumer<String> onGatewayFailure) {
        this.onGatewayFailure = onGatewayFailure;
        return this;
    }

    public WebPaymentForm setOnModalityFailure(Consumer<String> onModalityServerFailure) {
        this.onModalityFailure = onModalityServerFailure;
        return this;
    }

    public WebPaymentForm setOnBuyerCancel(Runnable onBuyerCancel) {
        this.onBuyerCancel = onBuyerCancel;
        return this;
    }

    public WebPaymentForm setOnFinalStatus(Consumer<PaymentStatus> onFinalStatus) {
        this.onFinalStatus = onFinalStatus;
        return this;
    }

    public Button getPayButton() {
        return payButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Region buildPaymentForm() {
        String url = result.getUrl();
        if (result.isRedirect()) {
            try {
                Browser.launchExternalBrowser(url);
            } catch (Exception e) {
                Console.log(e);
            }
            return null;
        }
        webViewPane.setMaxWidth(600);
        webViewPane.setMaxHeight(150);
        //webViewPane.setFitHeight(true); // doesn't work well
        //webViewPane.setRedirectConsole(true); // causes stack overflow
        payButton.setDisable(true);
        LoadOptions loadOptions = new LoadOptions()
                .setOnLoadFailure(this::onLoadFailure)
                .setOnLoadSuccess(() -> { // Note: can be called several times in case of an iFrame reload
                    payButton.setDisable(false);
                    try {
                        if (initFailureChecker != null)  // can happen on iFrame reload
                            initFailureChecker.cancel(); // we cancel the previous checker to prevent outdated init failure
                        webViewPane.setWindowMember("modality_javaPaymentForm", WebPaymentForm.this);
                        webViewPane.callWindow("modality_injectJavaPaymentForm", WebPaymentForm.this);
                        initFailureChecker = Scheduler.scheduleDelay(5000, () -> {
                            if (!inited) {
                                onInitFailure("The payment page didn't respond as expected");
                            }
                        });
                    } catch (Exception ex) {
                        onInitFailure(ex.getMessage());
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
                Console.log("Calling modality_submitGatewayPayment() in payment form");
                webViewPane.callWindow("modality_submitGatewayPayment",
                        buyerPersonalDetails.getFirstName(),
                        buyerPersonalDetails.getLastName(),
                        buyerPersonalDetails.getEmail(),
                        buyerPersonalDetails.getPhone(),
                        buyerPersonalDetails.getStreet(),
                        buyerPersonalDetails.getCityName(),
                        buyerPersonalDetails.getAdmin1Name(),
                        buyerPersonalDetails.getCountry().getIsoAlpha2()
                        );
            } catch (Exception ex) {
                onGatewayFailure(ex.getMessage());
            }
        });
        cancelButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setOnMouseClicked(e -> callRunnableOnUiThreadIfSet(onBuyerCancel));
        return new VBox(webViewPane, new ColumnsPane(payButton, cancelButton));
    }

    private void onLoadFailure(String error) {
        logDebug("onLoadFailure called (error = " + error + ")");
        if (onLoadFailure != null) {
            onLoadFailure.accept(error);
        }
    }

    // Callback methods (called back by the payment gateway script)

    public void onInitSuccess() {
        logDebug("onInitSuccess called");
        inited = true;
    }

    public void onInitFailure(String error) {
        logDebug("onInitFailure called (error = " + error + ")");
        inited = true;
        callConsumerOnUiThreadIfSet(onInitFailure, error);
    }

    public void onGatewayFailure(String error) {
        logDebug("onGatewayFailure called (error = " + error + ")");
        payButton.setDisable(false);
        Console.log(error);
        callConsumerOnUiThreadIfSet(onGatewayFailure, error);
    }

    public void onModalityFailure(String error) {
        logDebug("onModalityFailure called (error = " + error + ")");
        payButton.setDisable(false);
        callConsumerOnUiThreadIfSet(onModalityFailure, error);
    }

    public void onFinalStatus(String status) {
        logDebug("onFinalStatus called (status = " + status + ")");
        payButton.setDisable(false);
        callConsumerOnUiThreadIfSet(onFinalStatus, PaymentStatus.valueOf(status));
    }


    private static String getHttpServerOrigin() {
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

    private void callRunnableOnUiThreadIfSet(Runnable runnable) {
        if (runnable != null) {
            Platform.runLater(runnable);
        }
    }

    private <T> void callConsumerOnUiThreadIfSet(Consumer<T> consumer, T argument) {
        if (consumer != null) {
            Platform.runLater(() -> consumer.accept(argument));
        }
    }

    private static void logDebug(String message) {
        if (DEBUG) {
            Console.log(">>>>>>>>>>>>>> " + message);
        }
    }

}
