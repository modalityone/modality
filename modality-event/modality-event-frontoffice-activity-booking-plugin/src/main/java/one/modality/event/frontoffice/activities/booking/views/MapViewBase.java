package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.stack.orm.entity.Entity;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * @author Bruno Salmon
 */
public abstract class MapViewBase implements MapView {

    protected MapPoint mapCenterPoint;

    private final ObjectProperty<Entity> placeEntityProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            updateMapPosition();
        }
    };

    protected final ObservableList<MapMarker> markers = FXCollections.observableArrayList();

    public MapViewBase() {
        markers.addListener((InvalidationListener) observable -> updateMarkers());
    }

    public void setMapCenter(MapPoint mapCenter) {
        this.mapCenterPoint = mapCenter;
    }

    public ObjectProperty<Entity> placeEntityProperty() {
        return placeEntityProperty;
    }

    public ObservableList<MapMarker> getMarkers() {
        return markers;
    }

    protected abstract void updateMapPosition();

    protected abstract void updateMarkers();

}
