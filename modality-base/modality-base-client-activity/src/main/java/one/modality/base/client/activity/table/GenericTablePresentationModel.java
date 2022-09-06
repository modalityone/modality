package one.modality.base.client.activity.table;

import javafx.beans.property.*;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasGenericVisualResultProperty;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasGenericVisualSelectionProperty;
import dev.webfx.stack.orm.reactive.dql.statement.conventions.HasLimitProperty;
import one.modality.base.client.presentationmodel.HasSearchTextProperty;
import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.VisualSelection;

/**
 * @author Bruno Salmon
 */
public class GenericTablePresentationModel implements
        HasSearchTextProperty,
        HasLimitProperty,
        HasGenericVisualResultProperty,
        HasGenericVisualSelectionProperty {

    // Display input

    private final StringProperty searchTextProperty = new SimpleStringProperty();
    @Override  public StringProperty searchTextProperty() { return searchTextProperty; }

    private final IntegerProperty limitProperty = new SimpleIntegerProperty(0);
    @Override public IntegerProperty limitProperty() { return limitProperty; }

    private final ObjectProperty<VisualSelection> genericVisualSelectionProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<VisualSelection> genericVisualSelectionProperty() { return genericVisualSelectionProperty; }

    // Display output

    private final ObjectProperty<VisualResult> genericVisualResultProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<VisualResult> genericVisualResultProperty() { return genericVisualResultProperty; }

}
