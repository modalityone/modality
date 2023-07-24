package one.modality.catering.backoffice.operations.entities.allocationrule;

import dev.webfx.stack.orm.entity.Entity;
import javafx.scene.layout.Pane;
import one.modality.base.backoffice.operations.entities.generic.SetEntityFieldRequest;

public final class TriggerAllocationRuleRequest extends SetEntityFieldRequest {

  private static final String OPERATION_CODE = "TriggerAllocationRule";

  public TriggerAllocationRuleRequest(Entity allocationRule, Pane parentContainer) {
    super(allocationRule, "triggerAllocate", "true", null, parentContainer);
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }
}
