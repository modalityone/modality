package mongoose.ecommerce.backoffice.operations.entities.moneyflow;

import dev.webfx.framework.client.ui.controls.entity.sheet.EntityPropertiesSheet;
import dev.webfx.platform.shared.util.async.Future;
import javafx.scene.layout.Pane;
import mongoose.base.shared.entities.MoneyFlow;

final class EditMoneyFlowExecutor {

    static Future<Void> executeRequest(EditMoneyFlowRequest rq) {
        return execute(rq.getMoneyFlow(), rq.getParentContainer());
    }

    private static Future<Void> execute(MoneyFlow moneyFlow, Pane parentContainer) {
        EntityPropertiesSheet.editEntity(moneyFlow, "fromMoneyAccount,toMoneyAccount,method,positiveAmounts,negativeAmounts", parentContainer);
        return Future.succeededFuture();
    }
}