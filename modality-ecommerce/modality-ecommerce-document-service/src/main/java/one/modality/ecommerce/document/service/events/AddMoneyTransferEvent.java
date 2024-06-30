package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.MoneyTransfer;

/**
 * @author Bruno Salmon
 */
public class AddMoneyTransferEvent extends AbstractDocumentEvent {

    private MoneyTransfer moneyTransfer;
    private Object moneyTransferPrimaryKey;
    private final int amount;
    private final boolean pending;
    private final boolean successful;

    public AddMoneyTransferEvent(Object documentPrimaryKey, Object moneyTransferPrimaryKey, int amount, boolean pending, boolean successful) {
        super(documentPrimaryKey);
        this.moneyTransferPrimaryKey = moneyTransferPrimaryKey;
        this.amount = amount;
        this.pending = pending;
        this.successful = successful;
    }

    public AddMoneyTransferEvent(MoneyTransfer moneyTransfer) {
        super(moneyTransfer.getDocument());
        this.moneyTransfer = moneyTransfer;
        moneyTransferPrimaryKey = moneyTransfer.getPrimaryKey();
        amount = moneyTransfer.getAmount();
        pending = moneyTransfer.isPending();
        successful = moneyTransfer.isSuccessful();
    }

    public MoneyTransfer getMoneyTransfer() {
        return moneyTransfer;
    }

    public Object getMoneyTransferPrimaryKey() {
        return moneyTransferPrimaryKey;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isPending() {
        return pending;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setMoneyTransferPrimaryKey(Object moneyTransferPrimaryKey) {
        this.moneyTransferPrimaryKey = moneyTransferPrimaryKey;
    }
}
