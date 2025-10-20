package one.modality.ecommerce.backoffice.operations.entities.moneytransfer;

import dev.webfx.platform.async.Future;
import one.modality.base.client.util.dialog.ModalityDialog;
import one.modality.base.shared.domainmodel.formatters.PriceFormatter;
import one.modality.base.shared.entities.MoneyTransfer;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.registration.moneytransfer.RemoveMoneyTransferEvent;

final class DeletePaymentExecutor {

    static Future<Void> executeRequest(DeletePaymentRequest rq) {
        return rq.getPayment().<MoneyTransfer>onExpressionLoaded("amount,document") // RemoveMoneyTransferEvent requires access to document
                .compose(moneyTransfer ->
                    ModalityDialog.showConfirmationDialogForAsyncOperation(
                            "Are you sure you want to delete this payment?"
                            , rq.getParentContainer(),
                            () -> DocumentService.submitDocumentChanges(
                                    new SubmitDocumentChangesArgument(
                                            "Deleted payment " + PriceFormatter.formatWithoutCurrency(moneyTransfer.getAmount()),
                                            new RemoveMoneyTransferEvent(moneyTransfer))
                            )
                    )
                );
    }

}