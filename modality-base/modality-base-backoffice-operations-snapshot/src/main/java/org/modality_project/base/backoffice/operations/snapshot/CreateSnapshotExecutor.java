package org.modality_project.base.backoffice.operations.snapshot;

import dev.webfx.stack.orm.reactive.entities.entities_to_grid.EntityColumn;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.async.Future;

import java.util.Collection;

final class CreateSnapshotExecutor {

    static Future<Void> executeRequest(CreateSnapshotRequest rq) {
        return execute(rq.getEntities());
    }

    private static <E extends Entity> Future<Void> execute(Collection<E> entities, EntityColumn<E>... columns) {
        // TODO
        return Future.succeededFuture();
    }
}
