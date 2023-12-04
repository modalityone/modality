package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.entity.Entity;
import javafx.concurrent.Worker;
import javafx.scene.Node;
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

    private final WebView webView = new WebView();
    private JSObject window;
    private boolean googleMapLoaded;
    private final List<JSObject> googleMarkers = new ArrayList<>();

    @Override
    public Node buildMapNode() {
        WebEngine webEngine = webView.getEngine();
        webEngine.setOnError(event -> Console.log("Received error event: " + event));
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Worker.State state = webEngine.getLoadWorker().getState();
            if (state == Worker.State.READY) { // happens on first view, but also on subsequent views in the browser when the user navigates back here (as the browser unloads the iFrame each time it's removed from the DOM)
                String GOOGLE_MAP_JS_API_KEY = SourcesConfig.getSourcesRootConfig().getString("GOOGLE_MAP_JS_API_KEY");
                window = (JSObject) webEngine.executeScript("window");
                window.setMember("java", DynamicMapView.this);
                googleMarkers.clear(); // Also important to clear googleMarkers on subsequent views, because we need to recreate them all
                webEngine.executeScript("var googleMap; \n" +
                        "(g=>{var h,a,k,p=\"The Google Maps JavaScript API\",c=\"google\",l=\"importLibrary\",q=\"__ib__\",m=document,b=window;b=b[c]||(b[c]={});var d=b.maps||(b.maps={}),r=new Set,e=new URLSearchParams,u=()=>h||(h=new Promise(async(f,n)=>{await (a=m.createElement(\"script\"));e.set(\"libraries\",[...r]+\"\");for(k in g)e.set(k.replace(/[A-Z]/g,t=>\"_\"+t[0].toLowerCase()),g[k]);e.set(\"callback\",c+\".maps.\"+q);a.src=`https://maps.${c}apis.com/maps/api/js?`+e;d[q]=f;a.onerror=()=>h=n(Error(p+\" could not load.\"));a.nonce=m.querySelector(\"script[nonce]\")?.nonce||\"\";m.head.append(a)}));d[l]?console.warn(p+\" only loads once. Ignoring:\",g):d[l]=(f,...n)=>r.add(f)&&u().then(()=>d[l](f,...n))})({\n" +
                        "    key: \"" + GOOGLE_MAP_JS_API_KEY + "\",\n" +
                        //"    v: \"weekly\",\n" +
                        "});\n" +
                        "\n" +
                        "async function initMap() {\n" +
                        "    const { Map } = await google.maps.importLibrary('maps');\n" +
                        "    const { AdvancedMarkerElement, PinElement } = await google.maps.importLibrary('marker');\n" +
                        "    googleMap = new Map(document.body, {\n" +
                        "        center: { lat: -34.397, lng: 150.644 },\n" +
                        "        zoom: 8,\n" +
                        "        mapTypeControl: false,\n" +
                        "        navigationControlOptions: {style: google.maps.NavigationControlStyle.SMALL},\n" +
                        "        mapTypeId: google.maps.MapTypeId.ROADMAP," +
                        "        mapId: 'DEMO_MAP_ID'\n" +
                        "    });\n" +
                        "    window.AdvancedMarkerElement = AdvancedMarkerElement; window.PinElement = PinElement;\n" +
                        "    var javaCallbackFunction = function() { java.onGoogleMapLoaded(); };\n" +
                        // Google map markers use ResizeObserver which is not supported by default on OpenJFX WebView, so we use a polyfill in that case
                        "    if (!window.ResizeObserver) {\n" +
                        "       var script = document.createElement('script');\n" +
                        "       document.body.appendChild(script);\n" +
                        "       script.onload = javaCallbackFunction; \n" +
                        "       script.src = 'https://unpkg.com/resize-observer-polyfill@1.5.1/dist/ResizeObserver.global.js';\n" +
                        "    } else {\n" +
                        "       javaCallbackFunction();\n" +
                        "    }\n" +
                        "}\n" +
                        "function createMarker(lat, lng, title, javaMarker) {\n" +
                        "    var marker = new AdvancedMarkerElement({map: googleMap, position: {lat: lat, lng: lng}, title: title, content: new PinElement().element});\n" +
                        "    marker.addListener('click', x => { java.onMarkerClicked(javaMarker); } );\n" +
                        "    return marker;\n" +
                        "}\n" +
                        "function createEmptyObject() {\n" +
                        "    return {};\n" +
                        "}\n" +
                        "initMap();");
            }
        }, webEngine.getLoadWorker().stateProperty());
        return webView;
    }

    // Java callbacks called from JavaScript => must be declared in webfx.xml (required for successful GWT compilation)

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
                    JSObject p = (JSObject) webView.getEngine().executeScript("createEmptyObject()");
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
