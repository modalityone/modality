package mongoose.ecommerce.backoffice.operations.entities.moneyaccount;

import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.framework.shared.operation.HasOperationExecutor;
import dev.webfx.platform.shared.util.async.AsyncFunction;
import javafx.scene.layout.Pane;

public final class AddNewMoneyAccountRequest implements HasOperationCode,
        HasOperationExecutor<AddNewMoneyAccountRequest, Void> {

    private final static String OPERATION_CODE = "AddNewMoneyAccount";

    private final Pane parentContainer;

    public AddNewMoneyAccountRequest(Pane parentContainer) {
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
    public AsyncFunction<AddNewMoneyAccountRequest, Void> getOperationExecutor() {
        return AddNewMoneyAccountExecutor::executeRequest;
    }
}
