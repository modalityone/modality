package one.modality.ecommerce.backoffice.operations.entities.moneyaccount;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;

import javafx.scene.layout.Pane;

import one.modality.base.shared.entities.Organization;

public final class AddNewMoneyAccountRequest
        implements HasOperationCode, HasOperationExecutor<AddNewMoneyAccountRequest, Void> {

    private static final String OPERATION_CODE = "AddNewMoneyAccount";

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
