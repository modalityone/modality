package one.modality.ecommerce.backoffice.operations.entities.moneytransfer;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.MoneyTransfer;

public final class DeletePaymentRequest
    implements HasOperationCode, HasOperationExecutor<DeletePaymentRequest, Void> {

  private static final String OPERATION_CODE = "DeletePayment";

  private final MoneyTransfer payment;
  private final Pane parentContainer;

  public DeletePaymentRequest(MoneyTransfer payment, Pane parentContainer) {
    this.payment = payment;
    this.parentContainer = parentContainer;
  }

  MoneyTransfer getPayment() {
    return payment;
  }

  Pane getParentContainer() {
    return parentContainer;
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }

  @Override
  public AsyncFunction<DeletePaymentRequest, Void> getOperationExecutor() {
    return DeletePaymentExecutor::executeRequest;
  }
}
