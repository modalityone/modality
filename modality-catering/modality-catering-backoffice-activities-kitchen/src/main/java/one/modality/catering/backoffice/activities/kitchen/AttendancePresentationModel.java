package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.dql.statement.conventions.HasGroupDqlStatementProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.client.activity.eventdependent.EventDependentGenericTablePresentationModel;

import dev.webfx.stack.orm.reactive.dql.statement.conventions.HasConditionDqlStatementProperty;

public class AttendancePresentationModel extends EventDependentGenericTablePresentationModel implements
        HasConditionDqlStatementProperty,
        HasGroupDqlStatementProperty {

    private final ObjectProperty<DqlStatement> conditionDqlStatementProperty = new SimpleObjectProperty<>();
    @Override public final ObjectProperty<DqlStatement> conditionDqlStatementProperty() { return conditionDqlStatementProperty; }

    private final ObjectProperty<DqlStatement> groupDqlStatementProperty = new SimpleObjectProperty<>();
    @Override public final ObjectProperty<DqlStatement> groupDqlStatementProperty() { return groupDqlStatementProperty; }
}
