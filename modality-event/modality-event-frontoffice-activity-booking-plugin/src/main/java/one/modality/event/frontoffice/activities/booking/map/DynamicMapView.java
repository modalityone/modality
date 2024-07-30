package one.modality.event.frontoffice.activities.booking.map;

import dev.webfx.extras.webview.pane.LoadOptions;
import dev.webfx.extras.webview.pane.WebViewPane;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.resource.Resource;
import dev.webfx.stack.orm.entity.Entity;
import javafx.event.Event;
import javafx.scene.Node;
import netscape.javascript.JSObject;
import one.modality.base.shared.entities.Country;
import one.modality.base.shared.entities.Organization;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Salmon
 */
public class DynamicMapView extends MapViewBase {

    private boolean loadGluonWebViewAfterFlip;
    private boolean googleMapLoaded;
    private final List<JSObject> googleMarkers = new ArrayList<>();
    private final WebViewPane webViewPane = new WebViewPane();

    @Override
    protected Node buildMapNode() {
        String googleMapJsApiKey = SourcesConfig.getSourcesRootConfig().childConfigAt("modality.event.frontoffice.activity.booking").getString("googleMapJsApiKey");
        String script = Resource.getText(Resource.toUrl("DynamicMapView.js", getClass()))
                .replace("YOUR_API_KEY", googleMapJsApiKey);
        if (WebViewPane.isBrowser()) { // In the browser, Google Maps can integrate seamlessly without an iFrame
            // Note: this is not only lighter but also necessary for FireFox, because Google Maps JS API bugs when
            // inside a FireFox iFrame (map images are not loaded & displayed).
            script = script.replace("document.body", "document.getElementById('googleMap')");
            // Note: displaying the map in this way will actually remove the default iFrame within <fx-webview>
            // Another benefit of the seamless integration is that the user can zoom with the mouse scroll only (while
            // in an iFrame he needs to hold the command button in addition).
            // Last detail: scroll events are used by Google Maps to zoom in or out the map, so we don't want them to
            // be handled also in JavaFX, otherwise this would scroll the window while zooming.
            webViewPane.setOnScroll(Event::consume);
        }
        // Also we don't redirect the console, otherwise this would create an infinite loop (because the java WebFX
        // Console already relies on the current window console).
        webViewPane.setRedirectConsole(!WebViewPane.isBrowser());
        webViewPane.loadFromScript(script, new LoadOptions()
                .setSeamlessInBrowser(true)
                .setSeamlessContainerId("googleMap")
                .setOnWebWindowReady(() -> {
                    googleMapLoaded = false;
                    googleMarkers.clear(); // Important to clear googleMarkers on subsequent views, because we need to recreate them all
                    loadGluonWebViewAfterFlip = WebViewPane.isGluon();
                    webViewPane.setWindowMember("javaDynamicView", DynamicMapView.this); // indirectly register GWT callbacks
                }).setOnLoadSuccess(() -> {
                    webViewPane.callWindow("injectJavaDynamicView", DynamicMapView.this);
                }), false); // Gluon layout not stabilized at this stage => needs to call onGluonLayoutStabilized() later
        return webViewPane;
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
            webViewPane.onGluonLayoutStabilized();
        }
    }

    // Java callbacks called from JavaScript => must be declared in webfx.xml (required for successful GWT & Gluon compilation)

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
                webViewPane.getWebEngine().executeScript(script);
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
                    JSObject p = (JSObject) webViewPane.getWindow().call("createJSObject");
                    p.setMember("lat", mp.getLatitude());
                    p.setMember("lng", mp.getLongitude());
                    googleMarker.call("setPosition", p);
                } else {
                    JSObject googleMarker = (JSObject) webViewPane.getWindow().call("createMarker", mp.getLatitude(), mp.getLongitude(), marker.getOrganization().getName(), marker);
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
