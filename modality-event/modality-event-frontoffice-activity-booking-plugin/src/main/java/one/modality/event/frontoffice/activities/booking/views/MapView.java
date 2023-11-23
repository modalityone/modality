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
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;

/**
 * @author Bruno Salmon
 */
final class MapView {

    private static final String ZOOM_PLUS_SVG_PATH = "M 13,8 H 3 M 8,13 V 3";
    private static final String ZOOM_MINUS_SVG_PATH = "M 13,8 H 3";
    private final int zoomMin, zoomMax;

    private double mapCenterLatitude, mapCenterLongitude;

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

    public void setMapCenter(double mapCenterLatitude, double mapCenterLongitude) {
        this.mapCenterLatitude = mapCenterLatitude;
        this.mapCenterLongitude = mapCenterLongitude;
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

    Node buildMapNode(boolean showCenterMarker) {
        //mapImageView.setPreserveRatio(true);
        mapImageView.setFitWidth(600);
        mapImageView.setFitHeight(600);

        zoomInButton.setOnAction(e -> incrementZoom( +1));
        zoomOutButton.setOnAction(e -> incrementZoom(-1));
        VBox zoomBar = new VBox(5, zoomInButton, zoomOutButton);
        zoomBar.setAlignment(Pos.BOTTOM_RIGHT);
        zoomBar.setPadding(new Insets(5));

        scalePane = new ScalePane(ScaleMode.FIT_WIDTH, mapImageView);
        scalePane.setCanGrow(false);
        StackPane stackPane = new StackPane(scalePane, zoomBar);
        stackPane.setMaxSize(600, 600);

        if (showCenterMarker) {
            Node centerMarker = MapMarker.createMarkerNode();
            // We want the bottom tip of the marker to be in the center of the map. The SVG is 28px high, so we need to move
            // it up by 8px (otherwise this will be the point at y = 14px in SVG that will be in the center).
            centerMarker.setTranslateY(-14);
            stackPane.getChildren().add(centerMarker);
        }
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
        if (scalePane == null)
            return;
        StackPane stackPane = new StackPane(mapImageView);
        for (MapMarker marker : markers) {
            double mapCenterLatitudeRad = Math.toRadians(mapCenterLatitude);
            double mapCenterLongitudeRad = Math.toRadians(mapCenterLongitude);
            double latitudeRad = Math.toRadians(marker.getLatitude());
            double longitudeRad = Math.toRadians(marker.getLongitude());
            double zoom = zoomProperty.doubleValue();
            double x = 256 * Math.pow(2, zoom) * ((longitudeRad - mapCenterLongitudeRad) / (2 * Math.PI));
            double y = 256 * Math.pow(2, zoom) * (- (Math.log(Math.tan((Math.PI / 4) + latitudeRad / 2)) - Math.log(Math.tan((Math.PI / 4) + mapCenterLatitudeRad / 2))) / (2 * Math.PI));
            if (x >= -300 && x <= 300 && y >= -300 && y <= 300) {
                Node markerNode = marker.getNode();
                markerNode.setManaged(false);
                markerNode.relocate(300 + x,300 + y);
                stackPane.getChildren().add(markerNode);
            }
        }
        if (stackPane.getChildren().size() == 1)
            scalePane.setContent(mapImageView);
        else {
            stackPane.setMinSize(600, 600);
            stackPane.setPrefSize(600, 600);
            stackPane.setMaxSize(600, 600);
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
        b.setMinSize(24, 24); // For the web version, otherwise the minus button is not square (because minus SVG is not square)
        return b;
    }

}
