package one.modality.catering.backoffice2018.operations.entities.allocationrule;

import javafx.scene.layout.Pane;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.platform.async.AsyncFunction;

public final class EditAllocationRuleRequest implements HasOperationCode,
        HasOperationExecutor<EditAllocationRuleRequest, Void> {

    private final static String OPERATION_CODE = "EditAllocationRule";

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
