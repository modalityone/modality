package one.modality.event.frontoffice.activities.booking.map;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.useragent.UserAgent;
import dev.webfx.stack.orm.entity.Entity;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import one.modality.base.shared.entities.Country;
import one.modality.base.shared.entities.Organization;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Salmon
 */
public class DynamicMapView extends MapViewBase {

    private static final boolean IS_BROWSER = UserAgent.isBrowser();
    private static final boolean IS_GLUON = UserAgent.isNative();
    private final MonoPane webViewContainer = new MonoPane();
    private final WebView webView = new WebView();
    private final WebEngine webEngine = webView.getEngine();
    private JSObject window;
    private boolean loadGluonWebViewAfterFlip;
    private boolean googleMapLoaded;
    private final List<JSObject> googleMarkers = new ArrayList<>();

    @Override
    protected Node buildMapNode() {
        webEngine.setOnError(error -> Console.log("WebView error: " + error));
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Worker.State state = webEngine.getLoadWorker().getState();
            if (state == Worker.State.READY) { // happens on first view, but also on subsequent views in the browser when the user navigates back here (as the browser unloads the iFrame each time it's removed from the DOM)
                googleMapLoaded = false;
                googleMarkers.clear(); // Also important to clear googleMarkers on subsequent views, because we need to recreate them all
                if (!IS_GLUON) { // ex: desktop JRE & browser
                    window = (JSObject) webEngine.executeScript("window");
                    window.setMember("java", DynamicMapView.this);
                    webEngine.executeScript(getLoadScript());
                    webViewContainer.setContent(webView);
                } else { // ex: mobile app
                    Region region = new Region();
                    region.setMaxSize(1200, 1200);
                    webViewContainer.setContent(region);
                    loadGluonWebViewAfterFlip = true;
                }
            }
        }, webEngine.getLoadWorker().stateProperty());
        if (UserAgent.isBrowser()) {
            webViewContainer.setContent(webView);
            // Also, consuming touch events on the web view to prevent the page scrolling while the user actually just
            // wants to interact with the map (ex: move the map using 1 finger, or scroll the map using 2 fingers).
            webViewContainer.setOnTouchMoved(Event::consume);
        }
        return webViewContainer;
    }

    private String getLoadScript() {
        String GOOGLE_MAP_JS_API_KEY = SourcesConfig.getSourcesRootConfig().getString("GOOGLE_MAP_JS_API_KEY");
        String script = Resource.getText(Resource.toUrl("DynamicMapView.js", getClass()))
                .replace("YOUR_API_KEY", GOOGLE_MAP_JS_API_KEY);
        if (IS_BROWSER) { // In the browser, Google Maps can integrate seamlessly without an iFrame
            // Note: this is not only lighter but also necessary for FireFox, because Google Maps JS API bugs when
            // inside a FireFox iFrame (map images are not loaded & displayed).
            // So, we give the webView an id (will be mapped to the id of the HTML tag <fx-webview> which contains the iFrame)
            webView.setId("googleMap");
            // And instead of displaying the map in the iFrame body, we will display it directly inside <fx-webview>
            script = script.replace("document.body", "document.getElementById('googleMap')");
            // Note: displaying the map in this way will actually remove the default iFrame within <fx-webview>
            // Also we don't redirect the console, otherwise this would create an infinite loop (because the java WebFX
            // Console already relies on the current window console).
            script = script.replace("redirectConsole = true", "redirectConsole = false");
            // Another benefit of the seamless integration is that the user can zoom with the mouse scroll only (while
            // in an iFrame he needs to hold the command button in addition).
            // Last detail: scroll events are used by Google Maps to zoom in or out the map, so we don't want them to
            // be handled also in JavaFX, otherwise this would scroll the window while zooming.
            webView.setOnScroll(Event::consume);
        }
        return script;
    }

    @Override
    public void onBeforeFlip() {
        /*if (googleMapLoaded) {
            WritableImage snapshot = webView.snapshot(null, null);
            webViewContainer.setBackground(new Background(new BackgroundImage(snapshot, null, null, null, null)));
        }*/
    }

    @Override
    public void onAfterFlip() {
        if (loadGluonWebViewAfterFlip) {
            loadGluonWebViewAfterFlip = false;
            Platform.runLater(() -> {
                webEngine.loadContent("<html>" +
                        "<head>\n" +
                        "        <meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>\n" +
                        "        <meta name='viewport' content='user-scalable=no, initial-scale=1, maximum-scale=1, minimum-scale=1'>\n" +
                        "</head>\n" +
                        "<body style='width: 100%; height: 100vh;'><script type='text/javascript'>" + getLoadScript() + "</script></body></html>");
                webViewContainer.setContent(webView);
                UiScheduler.schedulePeriodic(100, scheduled -> {
                    window = (JSObject) webEngine.executeScript("window");
                    if (window != null) {
                        window.call("injectJava", DynamicMapView.this);
                        scheduled.cancel();
                    }
                });
            });
        }
    }

    // Java callbacks called from JavaScript => must be declared in webfx.xml (required for successful GWT & Gluon compilation)

    public void consoleLog(String message) {
        Console.log("[WebView console.log] " + message);
    }

    public void consoleWarn(String message) {
        Console.log("[WebView console.warn] " + message);
    }

    public void consoleError(String message) {
        Console.log("[WebView console.error] " + message);
    }

    public void onGoogleMapLoaded() { // Java callback called from JavaScript when Google map is loaded
        Console.log("Google map loaded");
        googleMapLoaded = true;
        updateMapPosition();
        updateMarkers();
    }

    public void onMarkerClicked(MapMarker marker) { // Java callback called from JavaScript when clicking on a Google marker
        Runnable onAction = marker.getOnAction();
        if (onAction != null)
            onAction.run();
    }

    @Override
    protected void updateMapPosition() {
        if (googleMapLoaded) {
            String script = null;
            Entity placeEntity = getPlaceEntity();
            if (placeEntity instanceof Country) {
                Country c = (Country) placeEntity;
                script = "googleMap.fitBounds(new google.maps.LatLngBounds(new google.maps.LatLng(" + c.getSouth() + ", " + c.getWest() + "), new google.maps.LatLng(" + c.getNorth() + ", " + c.getEast() + ")))";
            } else if (placeEntity instanceof Organization) {
                Organization o = (Organization) placeEntity;
                script = "googleMap.setCenter({lat: " + o.getLatitude() + ", lng: " + o.getLongitude() + "})";
            }
            if (script != null)
                webEngine.executeScript(script);
        }
    }

    @Override
    protected void updateMarkers() {
        if (googleMapLoaded) {
            int i = 0;
            for (MapMarker marker : markers) {
                MapPoint mp = marker.getMapPoint();
                if (i < googleMarkers.size()) {
                    JSObject googleMarker = googleMarkers.get(i);
                    JSObject p = (JSObject) window.call("createJSObject");
                    p.setMember("lat", mp.getLatitude());
                    p.setMember("lng", mp.getLongitude());
                    googleMarker.call("setPosition", p);
                } else {
                    JSObject googleMarker = (JSObject) window.call("createMarker", mp.getLatitude(), mp.getLongitude(), marker.getOrganization().getName(), marker);
                    googleMarkers.add(googleMarker);
                }
                i++;
            }
            while (googleMarkers.size() > i) {
                JSObject googleMarker = googleMarkers.get(i);
                googleMarker.call("setMap", (Object) null);
                googleMarkers.remove(i);
            }
        }
    }

}
