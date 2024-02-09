package one.modality.event.frontoffice.activities.booking.map;

import dev.webfx.stack.orm.entity.Entity;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
public interface MapView {

    // Downloaded map image is always 600px x 600px, then eventually scaled down
    double MAP_WIDTH = 600;
    double MAP_HEIGHT = 600;

    default void setMapCenter(double mapCenterLatitude, double mapCenterLongitude) {
        setMapCenter(new MapPoint(mapCenterLatitude, mapCenterLongitude));
    }

    void setMapCenter(MapPoint mapCenter);

    default Entity getPlaceEntity() {
        return placeEntityProperty().get();
    }

    ObjectProperty<Entity> placeEntityProperty();

    default void setPlaceEntity(Entity entity) {
        placeEntityProperty().set(entity);
    }

    ObservableList<MapMarker> getMarkers();

    Node getMapNode();

    default void onBeforeFlip() { }

    default void onAfterFlip() { }

}
