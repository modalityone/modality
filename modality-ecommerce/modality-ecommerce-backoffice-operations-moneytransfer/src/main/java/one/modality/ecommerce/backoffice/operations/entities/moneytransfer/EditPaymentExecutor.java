package one.modality.ecommerce.backoffice.operations.entities.moneytransfer;

import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.MoneyTransfer;
import dev.webfx.stack.orm.entity.controls.entity.sheet.EntityPropertiesSheet;
import dev.webfx.platform.async.Future;

final class EditPaymentExecutor {

    static Future<Void> executeRequest(EditPaymentRequest rq) {
        return execute(rq.getPayment(), rq.getParentContainer());
    }

    private static Future<Void> execute(MoneyTransfer payment, Pane parentContainer) {
        EntityPropertiesSheet.editEntity(payment, // language=JSON5
            """
            [
                'date',
                'method',
                'parentMethod',
                'amount',
                'readOnly(currency)',
                'readOnly(fromMoneyAccount)',
                'readOnly(parentFromMoneyAccount)',
                { expression: 'toMoneyAccount',
                  foreignAlias: 'ma',
                  foreignWhere: 'exists(select MoneyTransfer mt where id="paymentPk" and ma.type.internal=mt.(!payment or refund=document.expenditure) and ma.type.customer=mt.(payment and !document.expenditure and refund) and ma.type.supplier=mt.(payment and document.expenditure and !refund) and exists(select MethodSupport ms where ms.moneyAccountType=ma.type and ms.method=mt.method) and exists(select MoneyFlow mf where mf.fromMoneyAccount=mt.fromMoneyAccount and mf.toMoneyAccount=ma and (mf.method=null or mf.method=mt.method)))'
                },
                'parentToMoneyAccount',
                'readOnly(status)',
                'transactionRef',
                'comment',
                'read',
                'pending',
                'successful',
                'readOnly(verifier)'
            ]""".replace("\"paymentPk\"", payment.getPrimaryKey().toString()), parentContainer);
        return Future.succeededFuture();
    }
}
