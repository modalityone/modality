package one.modality.ecommerce.document.service.events.registration.moneytransfer;

import one.modality.base.shared.entities.MoneyTransfer;
import one.modality.ecommerce.document.service.events.AbstractMoneyTransferEvent;

/**
 * @author Bruno Salmon
 */
public final class RemoveMoneyTransferEvent extends AbstractMoneyTransferEvent {

    public RemoveMoneyTransferEvent(Object documentPrimaryKey, Object moneyTransferPrimaryKey) {
        super(documentPrimaryKey, moneyTransferPrimaryKey);
    }

    public RemoveMoneyTransferEvent(MoneyTransfer moneyTransfer) {
        super(moneyTransfer);
    }
}
