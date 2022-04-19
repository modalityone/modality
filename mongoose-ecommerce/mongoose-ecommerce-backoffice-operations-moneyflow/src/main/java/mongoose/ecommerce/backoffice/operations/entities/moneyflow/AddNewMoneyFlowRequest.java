package mongoose.ecommerce.backoffice.operations.entities.moneyflow;

import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.framework.shared.operation.HasOperationExecutor;
import dev.webfx.platform.shared.async.AsyncFunction;
import javafx.scene.layout.Pane;

public final class AddNewMoneyFlowRequest implements HasOperationCode,
        HasOperationExecutor<AddNewMoneyFlowRequest, Void> {

    private final static String OPERATION_CODE = "AddNewMoneyFlow";

    private final Pane parentContainer;

    public AddNewMoneyFlowRequest(Pane parentContainer) {
        this.parentContainer = parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    public Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public AsyncFunction<AddNewMoneyFlowRequest, Void> getOperationExecutor() {
        return AddNewMoneyFlowExecutor::executeRequest;
    }
}
