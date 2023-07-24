package one.modality.ecommerce.backoffice.operations.entities.moneytransfer;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;

import javafx.scene.layout.Pane;

import one.modality.base.shared.entities.MoneyTransfer;

public final class EditPaymentRequest
        implements HasOperationCode, HasOperationExecutor<EditPaymentRequest, Void> {

    private static final String OPERATION_CODE = "EditPayment";

    private final MoneyTransfer payment;
    private final Pane parentContainer;

    public EditPaymentRequest(MoneyTransfer payment, Pane parentContainer) {
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
    public AsyncFunction<EditPaymentRequest, Void> getOperationExecutor() {
        return EditPaymentExecutor::executeRequest;
    }
}
