package mongoose.ecommerce.backoffice.operations.entities.moneyaccount;

import dev.webfx.framework.client.ui.controls.entity.sheet.EntityPropertiesSheet;
import dev.webfx.platform.shared.async.Future;
import javafx.scene.layout.Pane;
import mongoose.base.shared.entities.MoneyAccount;

final class EditMoneyAccountExecutor {

    static Future<Void> executeRequest(EditMoneyAccountRequest rq) {
        return execute(rq.getMoneyAccount(), rq.getParentContainer());
    }

    private static Future<Void> execute(MoneyAccount moneyAccount, Pane parentContainer) {
        EntityPropertiesSheet.editEntity(moneyAccount, "name,closed,currency,event,gatewayCompany,type", parentContainer);
        return Future.succeededFuture();
    }
}