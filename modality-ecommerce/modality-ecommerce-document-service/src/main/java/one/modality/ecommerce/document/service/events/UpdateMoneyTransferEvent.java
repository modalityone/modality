package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.MoneyTransfer;

/**
 * @author Bruno Salmon
 */
public final class UpdateMoneyTransferEvent extends AbstractMoneyTransferEvent {

    public UpdateMoneyTransferEvent(Object documentPrimaryKey, Object moneyTransferPrimaryKey, int amount, boolean pending, boolean successful) {
        super(documentPrimaryKey, moneyTransferPrimaryKey, amount, pending, successful);
    }

    public UpdateMoneyTransferEvent(MoneyTransfer moneyTransfer) {
        super(moneyTransfer);
    }
}
