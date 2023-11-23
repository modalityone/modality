package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.extras.panes.ScaleMode;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.platform.conf.Config;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.stack.orm.entity.Entity;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;

/**
 * @author Bruno Salmon
 */
final class MapView {

    private static final String MARKER_SVG_PATH = "m 8.0408158,27.932173 c 0.2040781,-3.908096 2.3550612,-10.256967 5.4754162,-14.250776 1.699971,-2.177513 2.48363,-4.2978848 2.48363,-5.5856178 A 8.0488411,8.0488411 0 0 0 8.0000002,2.1599999e-7 v 0 A 8.0488411,8.0488411 0 0 0 1.378808e-4,8.0957792 c 0,1.287733 0.7816191992,3.4081048 2.4836307192,5.5856178 3.1203545,3.99585 5.2754194,10.34268 5.475416,14.250776 z";
    private static final String ZOOM_PLUS_SVG_PATH = "M 13,8 H 3 M 8,13 V 3";
    private static final String ZOOM_MINUS_SVG_PATH = "M 13,8 H 3";
    private static final int ZOOM_MIN = 5, ZOOM_MAX = 18, ZOOM_INITIAL = 12;

    private final ObjectProperty<Entity> entityProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            updateImage();
        }
    };
    private final IntegerProperty zoomProperty = new SimpleIntegerProperty(ZOOM_INITIAL) {
        @Override
        protected void invalidated() {
            updateImage();
        }
    };
    private final ImageView mapImageView = new ImageView();
    private final Button zoomInButton = createZoomButton(ZOOM_PLUS_SVG_PATH);
    private final Button zoomOutButton = createZoomButton(ZOOM_MINUS_SVG_PATH);

    public Entity getEntity() {
        return entityProperty.get();
    }

    public ObjectProperty<Entity> entityProperty() {
        return entityProperty;
    }

    public void setEntity(Entity entity) {
        this.entityProperty.set(entity);
    }

    Node buildMapNode() {
        //mapImageView.setPreserveRatio(true);
        mapImageView.setFitWidth(600);
        mapImageView.setFitHeight(600);
        SVGPath markerSvgPath = new SVGPath();
        markerSvgPath.setContent(MARKER_SVG_PATH);
        markerSvgPath.setFill(Color.web("#EA4335"));
        markerSvgPath.setStroke(Color.web("#DA352D"));
        // We want the bottom tip of the marker to be in the center of the map. The SVG is 28px high, so we need to move
        // it up by 8px (otherwise this will be the point at y = 14px in SVG that will be in the center).
        markerSvgPath.setTranslateY(-14);

        // We add a little white circle on top of the red marker
        Circle markerCircle = new Circle(3.5, Color.WHITE);
        markerCircle.setTranslateY(-20); // Moving it up to the right position

        zoomInButton.setOnAction(e -> incrementZoom( +1));
        zoomOutButton.setOnAction(e -> incrementZoom(-1));
        VBox zoomBar = new VBox(5, zoomInButton, zoomOutButton);
        zoomBar.setAlignment(Pos.BOTTOM_RIGHT);
        zoomBar.setPadding(new Insets(5));

        return new StackPane(new ScalePane(ScaleMode.FIT_WIDTH, mapImageView), zoomBar, markerSvgPath, markerCircle);
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

    private void incrementZoom(int deltaZoom) {
        int zoomValue = zoomProperty.get() + deltaZoom;
        zoomValue = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, zoomValue));
        zoomProperty.set(zoomValue);
        zoomInButton.setDisable(zoomValue == ZOOM_MAX);
        zoomOutButton.setDisable(zoomValue == ZOOM_MIN);
    }

    private static Button createZoomButton(String path) {
        Button b = new Button();
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(path);
        svgPath.setStroke(Color.BLACK);
        svgPath.setStrokeLineCap(StrokeLineCap.ROUND);
        b.setGraphic(svgPath);
        b.setMinSize(24, 24); // For the web version, otherwise the minus button is not square (because minus SVG is not square)
        return b;
    }

}
