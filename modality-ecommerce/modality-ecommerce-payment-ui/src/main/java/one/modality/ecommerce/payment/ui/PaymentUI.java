package one.modality.ecommerce.payment.ui;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.player.video.impl.ResizableRectangle;
import dev.webfx.platform.browser.Browser;
import dev.webfx.platform.console.Console;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import one.modality.ecommerce.payment.InitiatePaymentResult;

/**
 * @author Bruno Salmon
 */
public class PaymentUI {

    private final InitiatePaymentResult result;

    public PaymentUI(InitiatePaymentResult result) {
        this.result = result;
    }

    public Region buildUI() {
        MonoPane container = new MonoPane();
        container.setBackground(Background.fill(Color.PURPLE));
        container.setMinSize(600, 400);
        // We set its content to a resizable rectangle, so it will be resized
        container.setContent(new ResizableRectangle());
        container.widthProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                // One-time listener => we remove it
                container.widthProperty().removeListener(this);
                // Now that the container has a stabilized size (which will be the size of the video player),
                // we can set its content to the web view
                container.setContent(buildWebView());
            }
        });

        return container;
    }

    private WebView buildWebView() {
        String url = result.getUrl();
        if (result.isRedirect()) {
            try {
                Browser.launchExternalBrowser(url);
            } catch (Exception e) {
                Console.log(e);
            }
            return null;
        }
        WebView webView = new WebView();
        webView.setMinSize(550, 350);
        String htmlContent = result.getHtmlContent();
        WebEngine engine = webView.getEngine();
        if (htmlContent != null) {
            engine.loadContent(htmlContent);
        } else {
            if (url.startsWith("/"))
                //url = WindowLocation.getOrigin() + url; // TODO make this work
                url = "https://10.101.1.34" + url;
            Console.log("Loading " + url);
            engine.load(url);
            webView.setPageFill(Color.PINK);
        }
        engine.getLoadWorker().stateProperty().addListener(
                (obs, oldState, newState) -> {
                    Console.log("PaymentUI state changed from " + oldState + " to " + newState);
                }
        );
        /* Not yet supported by WebFX
        engine.getLoadWorker().exceptionProperty().addListener((obs, oldExc, newExc) -> {
            if (newExc != null) {
                Console.log("PaymentUI WebView exception:", newExc);
            }
        });*/
        engine.setOnError(e -> Console.log("PaymentUI WebView error: " + e.getMessage(), e.getException()));
        return webView;
    }
}
