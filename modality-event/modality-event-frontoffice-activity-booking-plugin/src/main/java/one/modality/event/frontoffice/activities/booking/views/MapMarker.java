package one.modality.event.frontoffice.activities.booking.views;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

/**
 * @author Bruno Salmon
 */
final class MapMarker {

    private static final String MARKER_SVG_PATH = "m 8.0408158,27.932173 c 0.2040781,-3.908096 2.3550612,-10.256967 5.4754162,-14.250776 1.699971,-2.177513 2.48363,-4.2978848 2.48363,-5.5856178 A 8.0488411,8.0488411 0 0 0 8.0000002,2.1599999e-7 v 0 A 8.0488411,8.0488411 0 0 0 1.378808e-4,8.0957792 c 0,1.287733 0.7816191992,3.4081048 2.4836307192,5.5856178 3.1203545,3.99585 5.2754194,10.34268 5.475416,14.250776 z";
    private final MapPoint mapPoint;
    private Node node;
    private SVGPath markerSvgPath;

    public MapMarker(double latitude, double longitude) {
        this(new MapPoint(latitude, longitude));
    }

    public MapMarker(MapPoint mapPoint) {
        this.mapPoint = mapPoint;
    }

    public MapPoint getMapPoint() {
        return mapPoint;
    }

    public Node getNode() {
        if (node == null) {
            node = createMarkerNode();
        }
        return node;
    }

    public void setColours(Paint fill, Paint stroke) {
        markerSvgPath.setFill(fill);
        markerSvgPath.setStroke(stroke);
    }

    private Node createMarkerNode() {
        markerSvgPath = new SVGPath();
        markerSvgPath.setContent(MARKER_SVG_PATH);
        setColours(Color.web("#EA4335"), Color.web("#DA352D"));
        // We add a little white circle on top of the red marker
        Circle markerCircle = new Circle(3.5, Color.WHITE);
        markerCircle.setTranslateY(-6); // Moving it up to the right position
        StackPane stackPane = new StackPane(markerSvgPath, markerCircle);
        stackPane.setMaxSize(16, 28);
        // We want the bottom tip of the marker to be at y = 0, so when we relocate the marker to (x,y), this is
        // actually its bottom tip that will be displayed at (x, y). The SVG is 28px high, so we need to move it up by
        // 14px for this to happen.
        stackPane.setTranslateY(-14);
        return stackPane;
    }
}
