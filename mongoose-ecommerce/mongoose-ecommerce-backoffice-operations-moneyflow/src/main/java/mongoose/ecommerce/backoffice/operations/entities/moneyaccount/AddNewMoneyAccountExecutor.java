package mongoose.ecommerce.backoffice.operations.entities.moneyaccount;

import dev.webfx.platform.shared.util.async.Future;
import javafx.scene.layout.Pane;
import mongoose.base.shared.entities.MoneyAccount;

final class AddNewMoneyAccountExecutor {

    static Future<Void> executeRequest(AddNewMoneyAccountRequest rq) {
        return execute(rq.getParentContainer());
    }

    private static Future<Void> execute(Pane parentContainer) {
        Future<Void> future = Future.future();
        return future;
    }
}