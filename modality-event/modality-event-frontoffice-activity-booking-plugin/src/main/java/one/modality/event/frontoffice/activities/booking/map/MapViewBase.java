package one.modality.event.frontoffice.activities.booking.map;

import dev.webfx.stack.orm.entity.Entity;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
public abstract class MapViewBase implements MapView {

    protected MapPoint mapCenter;
    private Node mapNode;

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
        this.mapCenter = mapCenter;
    }

    public ObjectProperty<Entity> placeEntityProperty() {
        return placeEntityProperty;
    }

    public ObservableList<MapMarker> getMarkers() {
        return markers;
    }

    protected abstract void updateMapPosition();

    protected abstract void updateMarkers();

    @Override
    public Node getMapNode() {
        if (mapNode == null)
            mapNode = buildMapNode();
        return mapNode;
    }

    protected abstract Node buildMapNode();
}
