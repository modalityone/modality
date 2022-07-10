package org.modality_project.base.backoffice.operations.snapshot;

import dev.webfx.stack.framework.client.orm.reactive.mapping.entities_to_grid.EntityColumn;
import dev.webfx.stack.framework.shared.orm.entity.Entity;
import dev.webfx.stack.platform.async.Future;

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
