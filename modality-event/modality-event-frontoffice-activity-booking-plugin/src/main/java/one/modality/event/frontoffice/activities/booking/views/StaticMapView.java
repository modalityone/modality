package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.extras.panes.ClipPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.platform.conf.Config;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.stack.orm.entity.Entity;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;

/**
 * @author Bruno Salmon
 */
final class StaticMapView extends MapViewBase {

    private static final String ZOOM_PLUS_SVG_PATH = "M 13,8 H 3 M 8,13 V 3";
    private static final String ZOOM_MINUS_SVG_PATH = "M 13,8 H 3";
    private static final Insets ZOOM_BAR_INSETS = new Insets(5);

    private final int zoomMin, zoomMax;

    private final IntegerProperty zoomProperty = new SimpleIntegerProperty() {
        @Override
        protected void invalidated() {
            updateMapPosition();
            updateMarkers();
        }
    };
    private final ImageView mapImageView = new ImageView();
    private final Button zoomInButton = createZoomButton(ZOOM_PLUS_SVG_PATH);
    private final Button zoomOutButton = createZoomButton(ZOOM_MINUS_SVG_PATH);
    private MonoPane mapContainer;

    public StaticMapView(int zoomInitial) {
        this(0, 20, zoomInitial);
    }

    public StaticMapView(int zoomMin, int zoomMax, int zoomInitial) {
        this.zoomMin = zoomMin;
        this.zoomMax = zoomMax;
        zoomProperty.set(zoomInitial);
    }

    @Override
    protected Node buildMapNode() {
        mapImageView.setFitWidth(MAP_WIDTH);
        mapImageView.setFitHeight(MAP_HEIGHT);

        zoomInButton.setOnAction(e -> incrementZoom( +1));
        zoomOutButton.setOnAction(e -> incrementZoom(-1));
        VBox zoomBar = new VBox(5, zoomInButton, zoomOutButton);
        // We set the max size to the pref size, otherwise it will cover the whole image map and prevent user
        // interaction with markers.
        zoomBar.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        mapContainer = new MonoPane(mapImageView);

        return new ClipPane(mapContainer, zoomBar) {
            @Override
            protected void layoutChildren() {
                double width = getWidth(), height = getHeight();
                layoutInArea(mapContainer, 0, 0, width, height, 0, HPos.CENTER, VPos.CENTER);
                layoutInArea(zoomBar, 0, 0, width, height, 0, ZOOM_BAR_INSETS, HPos.RIGHT, VPos.BOTTOM);
                resizeClip();
            }
        };
    }

    @Override
    protected void updateMapPosition() {
        Entity placeEntity = getPlaceEntity();
        if (placeEntity == null)
            return;
        Config webConfig = SourcesConfig.getSourcesRootConfig().childConfigAt("webfx.stack.com.client.websocket");
        String serverHost = webConfig.getString("serverHost");
        boolean serverSSL = webConfig.getBoolean("serverSSL");
        String mapUrl = (serverSSL ? "https://" : "http://") + serverHost + "/map/" + placeEntity.getDomainClass().getName().toLowerCase() + "/" + placeEntity.getPrimaryKey() + "?zoom=" + zoomProperty.get();
        Image image = new Image(mapUrl, true);
        image.progressProperty().addListener(observable -> {
            if (image.getProgress() >= 1)
                mapImageView.setImage(image);
        });
    }

    @Override
    protected void updateMarkers() {
        if (mapContainer == null || mapCenter == null)
            return;
        StackPane stackPane = new StackPane(mapImageView);
        double zoom = zoomProperty.doubleValue();
        double scaleFactor = Math.pow(2, zoom);
        for (MapMarker marker : markers) {
            // Computing the marker coordinates from the map image center
            double x = (marker.getMapPoint().getX() - mapCenter.getX()) * scaleFactor;
            double y = (marker.getMapPoint().getY() - mapCenter.getY()) * scaleFactor;
            // Translating the marker coordinates to be from the map image left top corner
            x += MAP_WIDTH / 2;
            y += MAP_HEIGHT / 2;
            // Checking that the marker point is visible (within the map image)
            if (x >= 0 && x <= MAP_WIDTH && y >= 0 && y <= MAP_HEIGHT) {
                Node markerNode = marker.getNode();
                markerNode.relocate(x, y);
                markerNode.setManaged(false);
                markerNode.setCursor(Cursor.HAND);
                stackPane.getChildren().add(markerNode);
            }
        }
        if (stackPane.getChildren().size() == 1)
            mapContainer.setContent(mapImageView);
        else {
            stackPane.setMinSize(MAP_WIDTH, MAP_HEIGHT);
            stackPane.setPrefSize(MAP_WIDTH, MAP_HEIGHT);
            stackPane.setMaxSize(MAP_WIDTH, MAP_HEIGHT);
            mapImageView.setScaleX(1);
            mapImageView.setScaleY(1);
            mapContainer.setContent(stackPane);
        }
    }

    private void incrementZoom(int deltaZoom) {
        int zoomValue = zoomProperty.get() + deltaZoom;
        zoomValue = Math.max(zoomMin, Math.min(zoomMax, zoomValue));
        zoomProperty.set(zoomValue);
        /*zoomInButton.setDisable(zoomValue == ZOOM_MAX);
        zoomOutButton.setDisable(zoomValue == ZOOM_MIN);*/
    }

    private static Button createZoomButton(String path) {
        Button b = new Button();
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(path);
        svgPath.setStroke(Color.BLACK);
        svgPath.setStrokeWidth(1.5);
        svgPath.setStrokeLineCap(StrokeLineCap.ROUND);
        b.setGraphic(svgPath);
        b.setCursor(Cursor.HAND);
        b.setMinSize(24, 24); // For the web version, otherwise the minus button is not square (because minus SVG is not square)
        return b;
    }

}
