package mongoose.ecommerce.backoffice.operations.entities.moneyaccount;

import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.framework.shared.operation.HasOperationExecutor;
import dev.webfx.platform.shared.util.async.AsyncFunction;
import javafx.scene.layout.Pane;
import mongoose.base.shared.entities.MoneyAccount;
import mongoose.base.shared.entities.Organization;

public final class EditMoneyAccountRequest implements HasOperationCode,
        HasOperationExecutor<EditMoneyAccountRequest, Void> {

    private final static String OPERATION_CODE = "EditMoneyAccount";

    private final MoneyAccount moneyAccount;
    private final Pane parentContainer;

    public EditMoneyAccountRequest(MoneyAccount moneyAccount, Pane parentContainer) {
        this.moneyAccount = moneyAccount;
        this.parentContainer = parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    public MoneyAccount getMoneyAccount() { return moneyAccount; }

    public Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public AsyncFunction<EditMoneyAccountRequest, Void> getOperationExecutor() {
        return EditMoneyAccountExecutor::executeRequest;
    }
}
