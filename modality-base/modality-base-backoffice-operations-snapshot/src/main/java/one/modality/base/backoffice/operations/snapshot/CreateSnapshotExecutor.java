package one.modality.base.backoffice.operations.snapshot;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.reactive.entities.entities_to_grid.EntityColumn;
import java.util.Collection;

final class CreateSnapshotExecutor {

  static Future<Void> executeRequest(CreateSnapshotRequest rq) {
    return execute(rq.getEntities());
  }

  private static <E extends Entity> Future<Void> execute(
      Collection<E> entities, EntityColumn<E>... columns) {
    // TODO
    return Future.succeededFuture();
  }
}
