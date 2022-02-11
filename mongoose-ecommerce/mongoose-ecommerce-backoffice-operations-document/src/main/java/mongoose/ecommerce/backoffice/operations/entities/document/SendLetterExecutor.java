package mongoose.ecommerce.backoffice.operations.entities.document;

import javafx.scene.layout.Pane;
import mongoose.base.shared.entities.Document;
import dev.webfx.platform.shared.util.async.Future;

final class SendLetterExecutor {

    static Future<Void> executeRequest(SendLetterRequest rq) {
        return execute(rq.getDocument(), rq.getParentContainer());
    }

    private static Future<Void> execute(Document document, Pane parentContainer) {
        Future<Void> future = Future.future();
        return future;
    }
}
