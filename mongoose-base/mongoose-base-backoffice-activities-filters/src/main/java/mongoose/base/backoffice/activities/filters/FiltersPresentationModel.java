package mongoose.base.backoffice.activities.filters;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import mongoose.base.client.activity.eventdependent.EventDependentGenericTablePresentationModel;
import mongoose.base.client.presentationmodel.HasSelectedDocumentProperty;
import mongoose.base.shared.entities.Document;
import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.VisualSelection;
import dev.webfx.framework.shared.orm.dql.DqlStatement;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_visual.conventions.HasGroupVisualResultProperty;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_visual.conventions.HasGroupVisualSelectionProperty;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_visual.conventions.HasMasterVisualResultProperty;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_visual.conventions.HasMasterVisualSelectionProperty;
import dev.webfx.framework.client.orm.reactive.dql.statement.conventions.*;
import dev.webfx.framework.shared.orm.expression.builder.ReferenceResolver;

final class FiltersPresentationModel extends EventDependentGenericTablePresentationModel implements
        HasConditionDqlStatementProperty,
        HasGroupDqlStatementProperty,
        HasColumnsDqlStatementProperty,
        HasGroupVisualResultProperty,
        HasGroupVisualSelectionProperty,
        HasSelectedGroupProperty<Document>,
        HasSelectedGroupConditionDqlStatementProperty,
        HasSelectedGroupReferenceResolver,
        HasMasterVisualResultProperty,
        HasMasterVisualSelectionProperty,
        HasSelectedMasterProperty<Document>,
        HasSelectedDocumentProperty {

    private final ObjectProperty<DqlStatement> conditionDqlStatementProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<DqlStatement> conditionDqlStatementProperty() { return conditionDqlStatementProperty; }

    private final ObjectProperty<DqlStatement> groupDqlStatementProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<DqlStatement> groupDqlStatementProperty() { return groupDqlStatementProperty; }

    private final ObjectProperty<DqlStatement> columnsDqlStatementProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<DqlStatement> columnsDqlStatementProperty() { return columnsDqlStatementProperty; }

    private final ObjectProperty<VisualResult> groupVisualResultProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<VisualResult> groupVisualResultProperty() { return groupVisualResultProperty; }

    private final ObjectProperty<VisualSelection> groupVisualSelectionProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<VisualSelection> groupVisualSelectionProperty() { return groupVisualSelectionProperty; }

    private final ObjectProperty<Document> selectedGroupProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<Document> selectedGroupProperty() { return selectedGroupProperty; }

    private final ObjectProperty<DqlStatement> selectedGroupConditionDqlStatementProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<DqlStatement> selectedGroupConditionDqlStatementProperty() { return selectedGroupConditionDqlStatementProperty; }

    private ReferenceResolver selectedGroupReferenceResolver;
    @Override public ReferenceResolver getSelectedGroupReferenceResolver() { return selectedGroupReferenceResolver; }
    @Override public void setSelectedGroupReferenceResolver(ReferenceResolver referenceResolver) { this.selectedGroupReferenceResolver = referenceResolver; }

    private final ObjectProperty<VisualResult> masterVisualResultProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<VisualResult> masterVisualResultProperty() { return masterVisualResultProperty; }

    private final ObjectProperty<VisualSelection> masterVisualSelectionProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<VisualSelection> masterVisualSelectionProperty() { return masterVisualSelectionProperty; }

    private final ObjectProperty<Document> selectedMasterProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<Document> selectedMasterProperty() { return selectedMasterProperty; }

    @Override public ObjectProperty<Document> selectedDocumentProperty() { return selectedMasterProperty(); }

}
