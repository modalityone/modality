package one.modality.ecommerce.backoffice2018.activities.statistics;

import dev.webfx.stack.orm.reactive.dql.statement.conventions.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.client.activity.eventdependent.EventDependentGenericTablePresentationModel;
import one.modality.base.client.presentationmodel.HasSelectedDocumentLineProperty;
import one.modality.base.client.presentationmodel.HasSelectedDocumentProperty;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.VisualSelection;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasGroupVisualResultProperty;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasGroupVisualSelectionProperty;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasMasterVisualResultProperty;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasMasterVisualSelectionProperty;
import dev.webfx.stack.orm.expression.builder.ReferenceResolver;

/**
 * @author Bruno Salmon
 */
final class StatisticsPresentationModel extends EventDependentGenericTablePresentationModel implements
        HasConditionDqlStatementProperty,
        HasGroupDqlStatementProperty,
        HasColumnsDqlStatementProperty,
        HasGroupVisualResultProperty,
        HasGroupVisualSelectionProperty,
        HasSelectedGroupProperty<DocumentLine>,
        HasSelectedGroupConditionDqlStatementProperty,
        HasSelectedGroupReferenceResolver,
        HasMasterVisualResultProperty,
        HasMasterVisualSelectionProperty,
        HasSelectedMasterProperty<DocumentLine>,
        HasSelectedDocumentLineProperty,
        HasSelectedDocumentProperty {

    private final ObjectProperty<DqlStatement> conditionDqlStatementProperty = new SimpleObjectProperty<>();
    @Override public final ObjectProperty<DqlStatement> conditionDqlStatementProperty() { return conditionDqlStatementProperty; }

    private final ObjectProperty<DqlStatement> groupDqlStatementProperty = new SimpleObjectProperty<>();
    @Override public final ObjectProperty<DqlStatement> groupDqlStatementProperty() { return groupDqlStatementProperty; }

    private final ObjectProperty<DqlStatement> columnsDqlStatementProperty = new SimpleObjectProperty<>();
    @Override public final ObjectProperty<DqlStatement> columnsDqlStatementProperty() { return columnsDqlStatementProperty; }

    private final ObjectProperty<VisualResult> groupVisualResultProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<VisualResult> groupVisualResultProperty() { return groupVisualResultProperty; }

    private final ObjectProperty<VisualSelection> groupVisualSelectionProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<VisualSelection> groupVisualSelectionProperty() { return groupVisualSelectionProperty; }

    private final ObjectProperty<DocumentLine> selectedGroupProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<DocumentLine> selectedGroupProperty() {
        return selectedGroupProperty;
    }

    private final ObjectProperty<DqlStatement> selectedGroupConditionDqlStatementProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<DqlStatement> selectedGroupConditionDqlStatementProperty() { return selectedGroupConditionDqlStatementProperty; }

    private ReferenceResolver selectedGroupReferenceResolver;
    @Override public ReferenceResolver getSelectedGroupReferenceResolver() { return selectedGroupReferenceResolver; }
    @Override public void setSelectedGroupReferenceResolver(ReferenceResolver referenceResolver) { this.selectedGroupReferenceResolver = referenceResolver; }

    private final ObjectProperty<VisualResult> masterVisualResultProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<VisualResult> masterVisualResultProperty() { return masterVisualResultProperty; }

    private final ObjectProperty<VisualSelection> masterVisualSelectionProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<VisualSelection> masterVisualSelectionProperty() { return masterVisualSelectionProperty; }

    @Override public ObjectProperty<DocumentLine> selectedMasterProperty() { return selectedDocumentLineProperty; }

    private final ObjectProperty<DocumentLine> selectedDocumentLineProperty = new SimpleObjectProperty<DocumentLine/*GWT*/>() {
        @Override
        protected void invalidated() {
            DocumentLine dl = get();
            setSelectedDocument(dl == null ? null : dl.getDocument());
        }
    };
    @Override public ObjectProperty<DocumentLine> selectedDocumentLineProperty() { return selectedDocumentLineProperty; }

    private final ObjectProperty<Document> selectedDocumentProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<Document> selectedDocumentProperty() {
        return selectedDocumentProperty;
    }
}
