package one.modality.catering.backoffice.operations.entities.allocationrule;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;

import javafx.scene.layout.Pane;

import one.modality.base.shared.entities.Event;

public final class AddNewAllocationRuleRequest
        implements HasOperationCode, HasOperationExecutor<AddNewAllocationRuleRequest, Void> {

    private static final String OPERATION_CODE = "AddNewAllocationRule";

    private final Event event;
    private final Pane parentContainer;

    public AddNewAllocationRuleRequest(Event event, Pane parentContainer) {
        this.event = event;
        this.parentContainer = parentContainer;
    }

    Event getEvent() {
        return event;
    }

    Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<AddNewAllocationRuleRequest, Void> getOperationExecutor() {
        return AddNewAllocationRuleExecutor::executeRequest;
    }
}
