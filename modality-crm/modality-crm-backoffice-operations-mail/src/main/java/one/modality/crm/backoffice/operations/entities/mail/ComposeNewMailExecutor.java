package one.modality.crm.backoffice.operations.entities.mail;

import dev.webfx.platform.async.Future;

import javafx.scene.layout.Pane;

import one.modality.base.shared.entities.Document;

final class ComposeNewMailExecutor {

    static Future<Void> executeRequest(ComposeNewMailRequest rq) {
        return execute(rq.getDocument(), rq.getParentContainer());
    }

    private static Future<Void> execute(Document document, Pane parentContainer) {
        Future<Void> future = Future.succeededFuture();
        return future;
    }
}
