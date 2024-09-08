package one.modality.ecommerce.document.service.events.book;

import one.modality.base.shared.entities.MoneyTransfer;
import one.modality.ecommerce.document.service.events.AbstractExistingMoneyTransferEvent;

/**
 * @author Bruno Salmon
 */
public final class AddMoneyTransferEvent extends AbstractExistingMoneyTransferEvent {

    public AddMoneyTransferEvent(Object documentPrimaryKey, Object moneyTransferPrimaryKey, int amount, boolean pending, boolean successful) {
        super(documentPrimaryKey, moneyTransferPrimaryKey, amount, pending, successful);
    }

    public AddMoneyTransferEvent(MoneyTransfer moneyTransfer) {
        super(moneyTransfer);
    }

    @Override
    protected void createMoneyTransfer() {
        if (isForSubmit()) {
            moneyTransfer = updateStore.insertEntity(MoneyTransfer.class, getMoneyTransferPrimaryKey());
        } else {
            super.createMoneyTransfer();
        }
    }
}
