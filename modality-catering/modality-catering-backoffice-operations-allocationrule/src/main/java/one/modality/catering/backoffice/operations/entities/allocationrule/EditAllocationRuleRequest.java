package one.modality.catering.backoffice.operations.entities.allocationrule;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;

public final class EditAllocationRuleRequest
    implements HasOperationCode, HasOperationExecutor<EditAllocationRuleRequest, Void> {

  private static final String OPERATION_CODE = "EditAllocationRule";

  private final Entity allocationRule;
  private final Pane parentContainer;

  public EditAllocationRuleRequest(Entity allocationRule, Pane parentContainer) {
    this.allocationRule = allocationRule;
    this.parentContainer = parentContainer;
  }

  Entity getAllocationRule() {
    return allocationRule;
  }

  Pane getParentContainer() {
    return parentContainer;
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }

  @Override
  public AsyncFunction<EditAllocationRuleRequest, Void> getOperationExecutor() {
    return EditAllocationRuleExecutor::executeRequest;
  }
}
