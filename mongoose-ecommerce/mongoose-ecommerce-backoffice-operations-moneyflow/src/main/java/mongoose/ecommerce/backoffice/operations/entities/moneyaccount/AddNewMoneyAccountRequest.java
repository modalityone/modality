package mongoose.ecommerce.backoffice.operations.entities.moneyaccount;

import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.framework.shared.operation.HasOperationExecutor;
import dev.webfx.platform.shared.async.AsyncFunction;
import javafx.scene.layout.Pane;
import mongoose.base.shared.entities.Organization;

public final class AddNewMoneyAccountRequest implements HasOperationCode,
        HasOperationExecutor<AddNewMoneyAccountRequest, Void> {

    private final static String OPERATION_CODE = "AddNewMoneyAccount";

    private final Organization organization;
    private final Pane parentContainer;

    public AddNewMoneyAccountRequest(Organization organization, Pane parentContainer) {
        this.organization = organization;
        this.parentContainer = parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    public Organization getOrganization() {
        return organization;
    }

    public Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public AsyncFunction<AddNewMoneyAccountRequest, Void> getOperationExecutor() {
        return AddNewMoneyAccountExecutor::executeRequest;
    }
}
