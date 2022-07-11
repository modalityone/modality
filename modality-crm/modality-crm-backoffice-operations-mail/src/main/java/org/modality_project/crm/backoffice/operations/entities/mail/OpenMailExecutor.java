package org.modality_project.crm.backoffice.operations.entities.mail;

import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.Mail;
import dev.webfx.stack.async.Future;

final class OpenMailExecutor {

    static Future<Void> executeRequest(OpenMailRequest rq) {
        return execute(rq.getMail(), rq.getParentContainer());
    }

    private static Future<Void> execute(Mail mail, Pane parentContainer) {
        Future<Void> future = Future.succeededFuture();
        return future;
    }
}
