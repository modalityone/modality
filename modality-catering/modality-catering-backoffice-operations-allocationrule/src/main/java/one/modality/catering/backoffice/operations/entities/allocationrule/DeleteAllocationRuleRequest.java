package one.modality.catering.backoffice.operations.entities.allocationrule;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;

public final class DeleteAllocationRuleRequest
    implements HasOperationCode, HasOperationExecutor<DeleteAllocationRuleRequest, Void> {

  private static final String OPERATION_CODE = "DeleteAllocationRule";

  private final Entity documentLine;
  private final Pane parentContainer;

  public DeleteAllocationRuleRequest(Entity documentLine, Pane parentContainer) {
    this.documentLine = documentLine;
    this.parentContainer = parentContainer;
  }

  Entity getDocumentLine() {
    return documentLine;
  }

  Pane getParentContainer() {
    return parentContainer;
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }

  @Override
  public AsyncFunction<DeleteAllocationRuleRequest, Void> getOperationExecutor() {
    return DeleteAllocationRuleExecutor::executeRequest;
  }
}
