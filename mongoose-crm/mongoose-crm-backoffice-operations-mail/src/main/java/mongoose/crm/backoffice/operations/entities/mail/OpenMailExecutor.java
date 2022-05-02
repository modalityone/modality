package mongoose.crm.backoffice.operations.entities.mail;

import javafx.scene.layout.Pane;
import mongoose.base.shared.entities.Mail;
import dev.webfx.platform.shared.async.Future;

final class OpenMailExecutor {

    static Future<Void> executeRequest(OpenMailRequest rq) {
        return execute(rq.getMail(), rq.getParentContainer());
    }

    private static Future<Void> execute(Mail mail, Pane parentContainer) {
        Future<Void> future = Future.succeededFuture();
        return future;
    }
}
