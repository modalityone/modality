package one.modality.ecommerce.document.service.events.gateway;

import one.modality.base.shared.entities.MoneyTransfer;
import one.modality.ecommerce.document.service.events.AbstractExistingMoneyTransferEvent;

/**
 * @author Bruno Salmon
 */
public final class UpdateMoneyTransferEvent extends AbstractExistingMoneyTransferEvent {

    public UpdateMoneyTransferEvent(Object documentPrimaryKey, Object moneyTransferPrimaryKey, int amount, boolean pending, boolean successful) {
        super(documentPrimaryKey, moneyTransferPrimaryKey, amount, pending, successful);
    }

    public UpdateMoneyTransferEvent(MoneyTransfer moneyTransfer) {
        super(moneyTransfer);
    }
}
