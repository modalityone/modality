package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.extras.panes.RatioPane;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.useragent.UserAgent;
import dev.webfx.stack.orm.entity.Entity;
import javafx.concurrent.Worker;
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

    private final RatioPane container = new RatioPane(16d/9);
    private final WebView webView = new WebView();
    private JSObject window;
    private boolean googleMapLoaded;
    private final List<JSObject> googleMarkers = new ArrayList<>();

    @Override
    public Node buildMapNode() {
        WebEngine webEngine = webView.getEngine();
        webEngine.setOnError(error -> Console.log("WebView error: " + error));
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Worker.State state = webEngine.getLoadWorker().getState();
            if (state == Worker.State.READY) { // happens on first view, but also on subsequent views in the browser when the user navigates back here (as the browser unloads the iFrame each time it's removed from the DOM)
                googleMapLoaded = false;
                googleMarkers.clear(); // Also important to clear googleMarkers on subsequent views, because we need to recreate them all
                String GOOGLE_MAP_JS_API_KEY = SourcesConfig.getSourcesRootConfig().getString("GOOGLE_MAP_JS_API_KEY");
                String script =
                        Resource.getText(Resource.toUrl("DynamicMapView.js", getClass()))
                        .replace("YOUR_API_KEY", GOOGLE_MAP_JS_API_KEY);
                boolean isGluon = UserAgent.isNative();
                if (!isGluon) { // ex: desktop JRE & browser
                    window = (JSObject) webEngine.executeScript("window");
                    window.setMember("java", DynamicMapView.this);
                    webEngine.executeScript(script);
                    container.setContent(webView);
                } else { // ex: mobile app
                    Region region = new Region();
                    region.setMaxSize(1200, 1200);
                    container.setContent(region);
                    UiScheduler.scheduleDelay(800, () -> {
                        webEngine.loadContent("<html>" +
                                "<head>\n" +
                                "        <meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>\n" +
                                "        <meta name='viewport' content='user-scalable=no, initial-scale=1, maximum-scale=1, minimum-scale=1'>\n" +
                                "</head>\n" +
                                "<body style='width: 100%; height: 100vh;'><p>Loading map...</p><script type='text/javascript'>" + script + "</script></body></html>");
                        container.setContent(webView);
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
        }, webEngine.getLoadWorker().stateProperty());
        if (UserAgent.isBrowser())
            container.setContent(webView);
        return container;
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
                webView.getEngine().executeScript(script);
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