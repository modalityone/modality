package one.modality.event.backoffice.activities.medias;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import one.modality.base.client.presentationmodel.HasSearchTextProperty;

/**
 * Presentation model for the Media Dashboard view.
 * Holds the search text property for filtering consumption data.
 *
 * @author Claude Code
 */
final class MediaDashboardPresentationModel implements HasSearchTextProperty {

    /**
     * Filter types for consumption data.
     */
    enum ConsumptionTypeFilter {
        ALL,
        LIVE,
        RECORDING,
        BOTH
    }

    private final StringProperty searchTextProperty = new SimpleStringProperty();
    private final ObjectProperty<ConsumptionTypeFilter> typeFilterProperty = new SimpleObjectProperty<>(ConsumptionTypeFilter.ALL);

    @Override
    public StringProperty searchTextProperty() {
        return searchTextProperty;
    }

    public String getSearchText() {
        return searchTextProperty.getValue();
    }

    public void setSearchText(String searchText) {
        searchTextProperty.setValue(searchText);
    }

    public ObjectProperty<ConsumptionTypeFilter> typeFilterProperty() {
        return typeFilterProperty;
    }

    public ConsumptionTypeFilter getTypeFilter() {
        return typeFilterProperty.getValue();
    }

    public void setTypeFilter(ConsumptionTypeFilter typeFilter) {
        typeFilterProperty.setValue(typeFilter);
    }
}
