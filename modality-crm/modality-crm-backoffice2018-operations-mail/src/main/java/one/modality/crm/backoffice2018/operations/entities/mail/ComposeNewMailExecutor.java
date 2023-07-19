package one.modality.crm.backoffice2018.operations.entities.mail;

import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Document;
import dev.webfx.platform.async.Future;

final class ComposeNewMailExecutor {

    static Future<Void> executeRequest(ComposeNewMailRequest rq) {
        return execute(rq.getDocument(), rq.getParentContainer());
    }

    private static Future<Void> execute(Document document, Pane parentContainer) {
        Future<Void> future = Future.succeededFuture();
        return future;
    }
}
