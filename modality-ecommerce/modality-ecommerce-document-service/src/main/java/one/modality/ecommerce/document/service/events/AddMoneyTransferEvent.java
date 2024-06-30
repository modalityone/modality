package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.MoneyTransfer;

/**
 * @author Bruno Salmon
 */
public final class AddMoneyTransferEvent extends AbstractMoneyTransferEvent {

    public AddMoneyTransferEvent(Object documentPrimaryKey, Object moneyTransferPrimaryKey, int amount, boolean pending, boolean successful) {
        super(documentPrimaryKey, moneyTransferPrimaryKey, amount, pending, successful);
    }

    public AddMoneyTransferEvent(MoneyTransfer moneyTransfer) {
        super(moneyTransfer);
    }
}
