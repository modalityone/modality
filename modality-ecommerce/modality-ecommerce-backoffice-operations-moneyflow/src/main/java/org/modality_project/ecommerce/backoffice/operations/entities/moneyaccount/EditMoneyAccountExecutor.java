package org.modality_project.ecommerce.backoffice.operations.entities.moneyaccount;

import dev.webfx.stack.framework.client.ui.controls.entity.sheet.EntityPropertiesSheet;
import dev.webfx.stack.platform.async.Future;
import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.MoneyAccount;

final class EditMoneyAccountExecutor {

    static Future<Void> executeRequest(EditMoneyAccountRequest rq) {
        return execute(rq.getMoneyAccount(), rq.getParentContainer());
    }

    private static Future<Void> execute(MoneyAccount moneyAccount, Pane parentContainer) {
        EntityPropertiesSheet.editEntity(moneyAccount, "name,closed,currency,event,gatewayCompany,type", parentContainer);
        return Future.succeededFuture();
    }
}