package mongoose.ecommerce.backoffice.operations.entities.moneytransfer;

import javafx.scene.layout.Pane;
import mongoose.base.shared.entities.Document;
import dev.webfx.platform.shared.util.async.Future;

final class AddNewPaymentExecutor {

    static Future<Void> executeRequest(AddNewPaymentRequest rq) {
        return execute(rq.getDocument(), rq.getParentContainer());
    }

    private static Future<Void> execute(Document documentLine, Pane parentContainer) {
        // Not yet implemented
        return Future.succeededFuture();
    }
}