package mongoose.ecommerce.backoffice.operations.entities.moneytransfer;

import javafx.scene.layout.Pane;
import mongoose.base.shared.entities.Document;
import dev.webfx.platform.shared.util.async.Future;

final class AddNewTransferExecutor {

    static Future<Void> executeRequest(AddNewTransferRequest rq) {
        return execute(rq.getDocument(), rq.getParentContainer());
    }

    private static Future<Void> execute(Document documentLine, Pane parentContainer) {
        Future<Void> future = Future.future();
        return future;
    }
}