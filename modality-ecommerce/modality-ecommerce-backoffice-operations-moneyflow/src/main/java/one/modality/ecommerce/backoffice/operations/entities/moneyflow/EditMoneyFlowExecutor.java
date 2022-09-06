package one.modality.ecommerce.backoffice.operations.entities.moneyflow;

import dev.webfx.stack.orm.entity.controls.entity.sheet.EntityPropertiesSheet;
import dev.webfx.platform.async.Future;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.MoneyFlow;

final class EditMoneyFlowExecutor {

    static Future<Void> executeRequest(EditMoneyFlowRequest rq) {
        return execute(rq.getMoneyFlow(), rq.getParentContainer());
    }

    private static Future<Void> execute(MoneyFlow moneyFlow, Pane parentContainer) {
        Object organizationPk = moneyFlow.getOrganizationId().getPrimaryKey();
        String expressionColumns = "[" +
                "{expression: `fromMoneyAccount`, foreignWhere: 'organization=" + organizationPk + "'}," +
                "{expression: `toMoneyAccount`, foreignWhere: 'organization=" + organizationPk + "'}," +
                "{expression: `method`, foreignWhere: '1=1'}," +
                "`positiveAmounts`," +
                "`negativeAmounts`" +
                "]";
        EntityPropertiesSheet.editEntity(moneyFlow, expressionColumns, parentContainer);
        return Future.succeededFuture();
    }
}