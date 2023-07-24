package one.modality.ecommerce.backoffice.activities.statements;

import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.VisualSelection;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.expression.builder.ReferenceResolver;
import dev.webfx.stack.orm.reactive.dql.statement.conventions.*;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasGroupVisualResultProperty;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasGroupVisualSelectionProperty;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasMasterVisualResultProperty;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasMasterVisualSelectionProperty;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasSlaveVisualResultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.client.activity.eventdependent.EventDependentGenericTablePresentationModel;
import one.modality.base.client.presentationmodel.HasSelectedPaymentProperty;
import one.modality.base.shared.entities.MoneyTransfer;

/**
 * @author Bruno Salmon
 */
final class StatementsPresentationModel extends EventDependentGenericTablePresentationModel
    implements HasConditionDqlStatementProperty,
        HasGroupDqlStatementProperty,
        HasGroupVisualResultProperty,
        HasGroupVisualSelectionProperty,
        HasSelectedGroupProperty<MoneyTransfer>,
        HasSelectedGroupConditionDqlStatementProperty,
        HasSelectedGroupReferenceResolver,
        HasMasterVisualResultProperty,
        HasMasterVisualSelectionProperty,
        HasSelectedMasterProperty<MoneyTransfer>,
        HasSelectedPaymentProperty,
        HasSlaveVisualResultProperty,
        HasSlaveVisibilityCondition<MoneyTransfer> {

  private final ObjectProperty<Entity> selectedMoneyAccountProperty = new SimpleObjectProperty<>();

  public ObjectProperty<Entity> selectedMoneyAccountProperty() {
    return selectedMoneyAccountProperty;
  }

  private final BooleanProperty flatPaymentsProperty = new SimpleBooleanProperty(true);

  public BooleanProperty flatPaymentsProperty() {
    return flatPaymentsProperty;
  }

  private final BooleanProperty flatBatchesProperty = new SimpleBooleanProperty(false);

  public BooleanProperty flatBatchesProperty() {
    return flatBatchesProperty;
  }

  private final ObjectProperty<DqlStatement> conditionDqlStatementProperty =
      new SimpleObjectProperty<>();

  @Override
  public ObjectProperty<DqlStatement> conditionDqlStatementProperty() {
    return conditionDqlStatementProperty;
  }

  private final ObjectProperty<DqlStatement> groupDqlStatementProperty =
      new SimpleObjectProperty<>();

  @Override
  public ObjectProperty<DqlStatement> groupDqlStatementProperty() {
    return groupDqlStatementProperty;
  }

  private final ObjectProperty<VisualResult> groupVisualResultProperty =
      new SimpleObjectProperty<>();

  @Override
  public ObjectProperty<VisualResult> groupVisualResultProperty() {
    return groupVisualResultProperty;
  }

  private final ObjectProperty<VisualSelection> groupVisualSelectionProperty =
      new SimpleObjectProperty<>();

  @Override
  public ObjectProperty<VisualSelection> groupVisualSelectionProperty() {
    return groupVisualSelectionProperty;
  }

  private final ObjectProperty<MoneyTransfer> selectedGroupProperty = new SimpleObjectProperty<>();

  @Override
  public ObjectProperty<MoneyTransfer> selectedGroupProperty() {
    return selectedGroupProperty;
  }

  private final ObjectProperty<DqlStatement> selectedGroupConditionDqlStatementProperty =
      new SimpleObjectProperty<>();

  @Override
  public ObjectProperty<DqlStatement> selectedGroupConditionDqlStatementProperty() {
    return selectedGroupConditionDqlStatementProperty;
  }

  private ReferenceResolver selectedGroupReferenceResolver;

  @Override
  public ReferenceResolver getSelectedGroupReferenceResolver() {
    return selectedGroupReferenceResolver;
  }

  @Override
  public void setSelectedGroupReferenceResolver(ReferenceResolver referenceResolver) {
    this.selectedGroupReferenceResolver = referenceResolver;
  }

  private final ObjectProperty<VisualResult> masterVisualResultProperty =
      new SimpleObjectProperty<>();

  @Override
  public ObjectProperty<VisualResult> masterVisualResultProperty() {
    return masterVisualResultProperty;
  }

  private final ObjectProperty<VisualSelection> masterVisualSelectionProperty =
      new SimpleObjectProperty<>();

  @Override
  public ObjectProperty<VisualSelection> masterVisualSelectionProperty() {
    return masterVisualSelectionProperty;
  }

  private final ObjectProperty<MoneyTransfer> selectedMasterProperty = new SimpleObjectProperty<>();

  @Override
  public ObjectProperty<MoneyTransfer> selectedMasterProperty() {
    return selectedMasterProperty;
  }

  @Override
  public ObjectProperty<MoneyTransfer> selectedPaymentProperty() {
    return selectedMasterProperty();
  }

  private final ObjectProperty<VisualResult> slaveVisualResultProperty =
      new SimpleObjectProperty<>();

  @Override
  public ObjectProperty<VisualResult> slaveVisualResultProperty() {
    return slaveVisualResultProperty;
  }

  @Override
  public boolean isSlaveVisible(MoneyTransfer selectedPayment) {
    return selectedPayment.getDocument() == null;
  }
}
