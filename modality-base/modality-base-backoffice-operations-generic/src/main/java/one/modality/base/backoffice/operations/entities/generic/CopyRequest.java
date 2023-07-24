package one.modality.base.backoffice.operations.entities.generic;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.reactive.entities.entities_to_grid.EntityColumn;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import java.util.Collection;

abstract class CopyRequest implements HasOperationCode, HasOperationExecutor<CopyRequest, Void> {

  private final Collection<? extends Entity> entities;
  private final EntityColumn[] columns;

  CopyRequest(Collection<? extends Entity> entities, EntityColumn... columns) {
    this.entities = entities;
    this.columns = columns;
  }

  Collection<? extends Entity> getEntities() {
    return entities;
  }

  EntityColumn[] getColumns() {
    return columns;
  }

  @Override
  public AsyncFunction<CopyRequest, Void> getOperationExecutor() {
    return CopyExecutor::executeRequest;
  }
}
