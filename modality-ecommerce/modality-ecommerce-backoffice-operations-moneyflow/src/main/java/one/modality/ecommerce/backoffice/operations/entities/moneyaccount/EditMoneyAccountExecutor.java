package one.modality.ecommerce.backoffice.operations.entities.moneyaccount;

import dev.webfx.stack.orm.entity.controls.entity.sheet.EntityPropertiesSheet;
import dev.webfx.platform.async.Future;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.MoneyAccount;

final class EditMoneyAccountExecutor {

    static Future<Void> executeRequest(EditMoneyAccountRequest rq) {
        return execute(rq.getMoneyAccount(), rq.getParentContainer());
    }

    private static Future<Void> execute(MoneyAccount moneyAccount, Pane parentContainer) {
        EntityPropertiesSheet.editEntity(moneyAccount, "name,closed,currency,event,gatewayCompany,type", parentContainer);
        return Future.succeededFuture();
    }
}