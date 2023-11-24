package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.extras.panes.ScaleMode;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.platform.conf.Config;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.stack.orm.entity.Entity;
import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
final class MapView {

    // Downloaded map image is always 600px x 600px, then eventually scaled down
    private static final double MAP_WIDTH = 600;
    private static final double MAP_HEIGHT = 600;

    private static final String ZOOM_PLUS_SVG_PATH = "M 13,8 H 3 M 8,13 V 3";
    private static final String ZOOM_MINUS_SVG_PATH = "M 13,8 H 3";
    private final int zoomMin, zoomMax;
    private MapPoint mapCenterPoint;

    private final ObjectProperty<Entity> entityProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            updateImage();
        }
    };
    private final IntegerProperty zoomProperty = new SimpleIntegerProperty() {
        @Override
        protected void invalidated() {
            updateImage();
            updateMarkers();
        }
    };
    private final ImageView mapImageView = new ImageView();
    private final Button zoomInButton = createZoomButton(ZOOM_PLUS_SVG_PATH);
    private final Button zoomOutButton = createZoomButton(ZOOM_MINUS_SVG_PATH);
    private final ObservableList<MapMarker> markers = FXCollections.observableArrayList();
    private ScalePane scalePane;

    public MapView(int zoomMin, int zoomMax, int zoomInitial) {
        this.zoomMin = zoomMin;
        this.zoomMax = zoomMax;
        zoomProperty.set(zoomInitial);
        markers.addListener((InvalidationListener) observable -> updateMarkers());
    }

    public void setMapCenterPoint(double mapCenterLatitude, double mapCenterLongitude) {
        setMapCenterPoint(new MapPoint(mapCenterLatitude, mapCenterLongitude));
    }

    public void setMapCenterPoint(MapPoint mapCenterPoint) {
        this.mapCenterPoint = mapCenterPoint;
    }

    public Entity getEntity() {
        return entityProperty.get();
    }

    public ObjectProperty<Entity> entityProperty() {
        return entityProperty;
    }

    public void setEntity(Entity entity) {
        this.entityProperty.set(entity);
    }

    public ObservableList<MapMarker> getMarkers() {
        return markers;
    }

    Node buildMapNode() {
        mapImageView.setFitWidth(MAP_WIDTH);
        mapImageView.setFitHeight(MAP_HEIGHT);

        zoomInButton.setOnAction(e -> incrementZoom( +1));
        zoomOutButton.setOnAction(e -> incrementZoom(-1));
        VBox zoomBar = new VBox(5, zoomInButton, zoomOutButton);
        // We set the max size to the pref size, otherwise it will cover the whole image map and prevent user
        // interaction with markers.
        zoomBar.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        zoomBar.setPadding(new Insets(5));
        StackPane.setAlignment(zoomBar, Pos.BOTTOM_RIGHT);

        scalePane = new ScalePane(ScaleMode.FIT_WIDTH, mapImageView);
        scalePane.setCanGrow(false); // We only eventually scale down the image, never scale up (otherwise will be pixelated)
        StackPane stackPane = new StackPane(scalePane, zoomBar);
        stackPane.setMaxSize(MAP_WIDTH, MAP_HEIGHT);

        return stackPane;
    }

    private void updateImage() {
        Entity entity = entityProperty.get();
        if (entity == null)
            return;
        Config webConfig = SourcesConfig.getSourcesRootConfig().childConfigAt("webfx.stack.com.client.websocket");
        String serverHost = webConfig.getString("serverHost");
        boolean serverSSL = webConfig.getBoolean("serverSSL");
        String mapUrl = (serverSSL ? "https://" : "http://") + serverHost + "/map/" + entity.getDomainClass().getName().toLowerCase() + "/" + entity.getPrimaryKey() + "?zoom=" + zoomProperty.get();
        Image image = new Image(mapUrl, true);
        image.progressProperty().addListener(observable -> {
            if (image.getProgress() >= 1)
                mapImageView.setImage(image);
        });
    }

    private void updateMarkers() {
        if (scalePane == null || mapCenterPoint == null)
            return;
        StackPane stackPane = new StackPane(mapImageView);
        double zoom = zoomProperty.doubleValue();
        double scaleFactor = Math.pow(2, zoom);
        for (MapMarker marker : markers) {
            // Computing the marker coordinates from the map image center
            double x = (marker.getMapPoint().getX() - mapCenterPoint.getX()) * scaleFactor;
            double y = (marker.getMapPoint().getY() - mapCenterPoint.getY()) * scaleFactor;
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
            scalePane.setContent(mapImageView);
        else {
            stackPane.setMinSize(MAP_WIDTH, MAP_HEIGHT);
            stackPane.setPrefSize(MAP_WIDTH, MAP_HEIGHT);
            stackPane.setMaxSize(MAP_WIDTH, MAP_HEIGHT);
            mapImageView.setScaleX(1);
            mapImageView.setScaleY(1);
            scalePane.setContent(stackPane);
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
