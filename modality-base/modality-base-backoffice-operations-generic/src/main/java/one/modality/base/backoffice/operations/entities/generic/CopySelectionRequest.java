package one.modality.base.backoffice.operations.entities.generic;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.reactive.entities.entities_to_grid.EntityColumn;
import java.util.Collection;

public final class CopySelectionRequest extends CopyRequest {

  private static final String OPERATION_CODE = "CopySelection";

  public CopySelectionRequest(Collection<? extends Entity> entities, EntityColumn... columns) {
    super(entities, columns);
  }

  public Object getOperationCode() {
    return OPERATION_CODE;
  }
}
