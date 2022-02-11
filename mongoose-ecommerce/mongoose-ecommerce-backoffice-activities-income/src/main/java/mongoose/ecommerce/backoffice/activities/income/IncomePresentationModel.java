package mongoose.ecommerce.backoffice.activities.income;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import mongoose.base.client.activity.eventdependent.EventDependentGenericTablePresentationModel;
import dev.webfx.framework.client.orm.reactive.dql.statement.conventions.HasGroupDqlStatementProperty;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_visual.conventions.HasGroupVisualResultProperty;
import dev.webfx.extras.visual.VisualResult;
import dev.webfx.framework.shared.orm.dql.DqlStatement;

/**
 * @author Bruno Salmon
 */
final class IncomePresentationModel extends EventDependentGenericTablePresentationModel implements
        HasGroupVisualResultProperty,
        HasGroupDqlStatementProperty {

    private final ObjectProperty<VisualResult> groupVisualResultProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<VisualResult> groupVisualResultProperty() { return groupVisualResultProperty; }

    private final ObjectProperty<DqlStatement> groupDqlStatementProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<DqlStatement> groupDqlStatementProperty() { return groupDqlStatementProperty; }

}
