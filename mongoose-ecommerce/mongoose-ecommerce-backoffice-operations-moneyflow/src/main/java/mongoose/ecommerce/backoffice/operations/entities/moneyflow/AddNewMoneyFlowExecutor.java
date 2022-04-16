package mongoose.ecommerce.backoffice.operations.entities.moneyflow;

import dev.webfx.platform.shared.util.async.Future;
import javafx.scene.layout.Pane;
import mongoose.base.shared.entities.MoneyFlow;

final class AddNewMoneyFlowExecutor {

    static Future<Void> executeRequest(AddNewMoneyFlowRequest rq) {
        return execute(rq.getParentContainer());
    }

    private static Future<Void> execute(Pane parentContainer) {
        Future<Void> future = Future.future();
        return future;
    }
}