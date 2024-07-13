package one.modality.ecommerce.payment.client;

import dev.webfx.extras.panes.FlexPane;
import dev.webfx.extras.webview.pane.LoadOptions;
import dev.webfx.extras.webview.pane.WebViewPane;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.browser.Browser;
import dev.webfx.platform.conf.ConfigLoader;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.scheduler.Scheduled;
import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Numbers;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import one.modality.base.shared.entities.markers.HasPersonalDetails;
import one.modality.ecommerce.payment.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
public class WebPaymentForm {

    private static final boolean DEBUG = true;

    private final InitiatePaymentResult result;
    private final HasPersonalDetails buyerPersonalDetails;
    private final WebViewPane webViewPane = new WebViewPane();
    private final BooleanProperty userInteractionAllowedProperty = new SimpleBooleanProperty(false);
    private Scheduled initFailureChecker;
    private boolean inited;
    private Consumer<String> onLoadFailure; // Called when the webview failed to load
    private Consumer<String> onInitFailure; // Called when the payment page failed to initialised (otherwise the card details should appear)
    private Consumer<String> onVerificationFailure; // Called when the gateway failed to create the payment (just after the buyer pressed Pay)
    private Consumer<String> onPaymentFailure; // Called when Modality couldn't complete the payment
    private Consumer<PaymentStatus> onPaymentCompletion;

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

    public WebPaymentForm setOnVerificationFailure(Consumer<String> onVerificationFailure) {
        this.onVerificationFailure = onVerificationFailure;
        return this;
    }

    public WebPaymentForm setOnPaymentFailure(Consumer<String> onModalityServerFailure) {
        this.onPaymentFailure = onModalityServerFailure;
        return this;
    }

    public WebPaymentForm setOnPaymentCompletion(Consumer<PaymentStatus> onPaymentCompletion) {
        this.onPaymentCompletion = onPaymentCompletion;
        return this;
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
        webViewPane.setFitHeight(true); // Note: works with browser seamless mode and OpenJFX WebView, but not well with browser iFrame (constantly increasing)
        webViewPane.setMaxHeight(800); // Setting a maximum in case we are in browser iFrame (which we avoid for now)
        webViewPane.setFitHeightExtra(result.isSeamless() ? 5 : 10);
        //webViewPane.setRedirectConsole(true); // causes stack overflow
        setUserInteractionAllowed(false);
        LoadOptions loadOptions = new LoadOptions()
                .setOnLoadFailure(this::onLoadFailure)
                .setOnLoadSuccess(() -> { // Note: can be called several times in case of an iFrame reload
                    try {
                        if (initFailureChecker != null)  // can happen on iFrame reload
                            initFailureChecker.cancel(); // we cancel the previous checker to prevent outdated init failure
                        webViewPane.setWindowMember("modality_javaPaymentForm", WebPaymentForm.this);
                        webViewPane.callWindow("modality_injectJavaPaymentForm", WebPaymentForm.this);
                        initFailureChecker = Scheduler.scheduleDelay(5000, () -> {
                            if (!inited) {
                                onGatewayInitFailure("The payment page didn't respond as expected");
                            }
                        });
                    } catch (Exception ex) {
                        onGatewayInitFailure(ex.getMessage());
                    }
                });
        String htmlContent = result.getHtmlContent();
        if (htmlContent != null) {
            if (result.isSeamless()) {
                loadOptions
                        .setSeamlessInBrowser(true)
                        .setSeamlessContainerId("modality-payment-form-container");
                webViewPane.loadFromScript(htmlContent, loadOptions, false);
            } else {
                webViewPane.loadFromHtml(htmlContent, loadOptions, false);
            }
        } else {
            if (url.startsWith("/")) {
                url = getHttpServerOrigin() + url;
            }
            webViewPane.loadFromUrl(url, loadOptions, false);
        }
        webViewPane.getStyleClass().add("payment-form");
        return webViewPane;
    }

    public String getGatewayName() {
        return result.getGatewayName();
    }

    public boolean isLive() {
        return result.isLive();
    }

    public boolean isSandbox() {
        return !isLive();
    }

    public Node createSandboxBar() {
        SandboxCard[] sandboxCards = result.getSandboxCards();
        if (sandboxCards == null)
            return new Text("No sandbox cards available");
        Button numbersButton = copyButton("Numbers");
        Button expirationDateButton = copyButton("Expiration Date");
        Button cvvButton = copyButton("CVV");
        Button zipButton = copyButton("ZIP");
        Button cardButton = new Button("Select a sandbox card to copy");
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().setAll(Arrays.stream(sandboxCards)
                .map(card -> {
                    MenuItem menuItem = new MenuItem();
                    menuItem.setText(card.getName());
                    menuItem.setOnAction(e -> {
                        cardButton.setText(card.getName());
                        numbersButton.setText(card.getNumbers());
                        String expirationDate = card.getExpirationDate();
                        if (expirationDate == null) {
                            expirationDate = Numbers.twoDigits(LocalDate.now().getMonth().getValue()) + "/" + ((LocalDate.now().getYear() + 1) % 100);
                        }
                        expirationDateButton.setText(expirationDate);
                        cvvButton.setText(card.getCvv());
                        zipButton.setText(card.getZip());
                    });
                    return menuItem;
                }).collect(Collectors.toList()));
        cardButton.setOnAction(e -> {
            contextMenu.setMinWidth(cardButton.getWidth());
            contextMenu.setStyle("-fx-min-width: " + cardButton.getWidth() + "px");
            Point2D buttonPosition = cardButton.localToScreen(0, 0);
            contextMenu.show(cardButton, buttonPosition.getX(), buttonPosition.getY());
        });
        cardButton.setMaxWidth(Double.MAX_VALUE);
        return new VBox(10, cardButton,
                new FlexPane(10, 10, cardButton, numbersButton, expirationDateButton, cvvButton, zipButton));
    }

    private Button copyButton(String name) {
        Button button = new Button();
        button.setMaxWidth(Double.MAX_VALUE);
        button.setText(name);
        button.setOnAction(e -> {
            ClipboardContent content = new ClipboardContent();
            content.put(DataFormat.PLAIN_TEXT, button.getText());
            Clipboard.getSystemClipboard().setContent(content);
        });
        return button;
    }

    public void pay() {
        if (!inited || !userInteractionAllowedProperty.get())
            throw new IllegalStateException("pay() must be called after the payment form has been initialized and when the user is allowed to interact");
        setUserInteractionAllowed(false);
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
            onGatewayBuyerVerificationFailure(ex.getMessage());
        }
    }

    public Future<CancelPaymentResult> cancelPayment() {
        logDebug("cancelPayment called");
        return PaymentService.cancelPayment(new CancelPaymentArgument(result.getPaymentPrimaryKey()));
    }

    public ReadOnlyBooleanProperty userInteractionAllowedProperty() {
        return userInteractionAllowedProperty;
    }

    public boolean isUserInteractionAllowed() {
        return userInteractionAllowedProperty.get();
    }

    private void setUserInteractionAllowed(boolean allowed) {
        userInteractionAllowedProperty.set(allowed);
    }

    private void setUserInteractionAllowedInUiThread(boolean allowed) {
        UiScheduler.runInUiThread(() -> setUserInteractionAllowed(allowed));
    }

    private void onLoadFailure(String error) {
        logDebug("onLoadFailure called (error = " + error + ")");
        if (onLoadFailure != null) {
            onLoadFailure.accept(error);
        }
    }

    // Callback methods (called back by the payment gateway script)

    public void onGatewayInitSuccess() {
        logDebug("onGatewayInitSuccess called");
        inited = true;
        setUserInteractionAllowedInUiThread(true);
    }

    public void onGatewayInitFailure(String error) {
        logDebug("onGatewayInitFailure called (error = " + error + ")");
        inited = true;
        //setUserInteractionAllowed(true);
        callConsumerOnUiThreadIfSet(onInitFailure, error);
    }

    public void onGatewayCardVerificationFailure(String error) {
        logDebug("onGatewayCardVerificationFailure called (error = " + error + ")");
        setUserInteractionAllowedInUiThread(true);
    }

    public void onGatewayBuyerVerificationFailure(String error) {
        logDebug("onGatewayBuyerVerificationFailure called (error = " + error + ")");
        setUserInteractionAllowedInUiThread(true);
        callConsumerOnUiThreadIfSet(onVerificationFailure, error);
    }

    public void onGatewayPaymentVerificationSuccess(String gatewayCompletePaymentPayload) {
        logDebug("onGatewayPaymentVerificationSuccess called (gatewayCompletePaymentPayload = " + gatewayCompletePaymentPayload + ")");
        PaymentService.completePayment(new CompletePaymentArgument(result.getPaymentPrimaryKey(), result.isLive(), result.getGatewayName(), gatewayCompletePaymentPayload))
                .onFailure(e -> onModalityCompletePaymentFailure(e.getMessage()))
                .onSuccess(r -> onModalityCompletePaymentSuccess(r.getPaymentStatus()));
    }

    public void onModalityCompletePaymentFailure(String error) {
        logDebug("onModalityCompletePaymentFailure called (error = " + error + ")");
        setUserInteractionAllowedInUiThread(true);
        callConsumerOnUiThreadIfSet(onPaymentFailure, error);
    }


    public void onModalityCompletePaymentSuccess(PaymentStatus status) {
        logDebug("onModalityCompletePaymentSuccess called (status = " + status + ")");
        //setUserInteractionAllowedInUiThread(true);
        callConsumerOnUiThreadIfSet(onPaymentCompletion, status);
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
