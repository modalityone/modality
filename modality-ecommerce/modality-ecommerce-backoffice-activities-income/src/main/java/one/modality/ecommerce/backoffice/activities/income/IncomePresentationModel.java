package one.modality.ecommerce.backoffice.activities.income;

import dev.webfx.extras.visual.VisualResult;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.dql.statement.conventions.HasGroupDqlStatementProperty;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasGroupVisualResultProperty;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import one.modality.base.client.activity.eventdependent.EventDependentGenericTablePresentationModel;

/**
 * @author Bruno Salmon
 */
final class IncomePresentationModel extends EventDependentGenericTablePresentationModel
        implements HasGroupVisualResultProperty, HasGroupDqlStatementProperty {

    private final ObjectProperty<VisualResult> groupVisualResultProperty =
            new SimpleObjectProperty<>();

    @Override
    public ObjectProperty<VisualResult> groupVisualResultProperty() {
        return groupVisualResultProperty;
    }

    private final ObjectProperty<DqlStatement> groupDqlStatementProperty =
            new SimpleObjectProperty<>();

    @Override
    public ObjectProperty<DqlStatement> groupDqlStatementProperty() {
        return groupDqlStatementProperty;
    }
}
