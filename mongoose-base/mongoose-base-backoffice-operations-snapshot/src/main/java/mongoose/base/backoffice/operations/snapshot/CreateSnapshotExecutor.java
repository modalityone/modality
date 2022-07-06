package mongoose.base.backoffice.operations.snapshot;

import dev.webfx.framework.client.orm.reactive.mapping.entities_to_grid.EntityColumn;
import dev.webfx.framework.client.orm.reactive.mapping.entities_to_grid.EntityColumnFactory;
import dev.webfx.framework.shared.orm.domainmodel.formatter.ValueFormatter;
import dev.webfx.framework.shared.orm.entity.Entity;
import dev.webfx.framework.shared.orm.expression.Expression;
import dev.webfx.platform.shared.async.Future;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class CreateSnapshotExecutor {

    static Future<Void> executeRequest(CreateSnapshotRequest rq) {
        return execute(rq.getEntities());
    }

    private static <E extends Entity> Future<Void> execute(Collection<E> entities, EntityColumn<E>... columns) {
        // TODO
        return Future.succeededFuture();
    }
}
