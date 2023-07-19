package one.modality.catering.backoffice2018.operations.entities.allocationrule;

import javafx.scene.layout.Pane;
import one.modality.base.backoffice.operations.entities.generic.SetEntityFieldRequest;
import dev.webfx.stack.orm.entity.Entity;

public final class TriggerAllocationRuleRequest extends SetEntityFieldRequest {

    private final static String OPERATION_CODE = "TriggerAllocationRule";

    public TriggerAllocationRuleRequest(Entity allocationRule, Pane parentContainer) {
        super(allocationRule, "triggerAllocate", "true", null, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
