package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.MoneyTransfer;

/**
 * @author Bruno Salmon
 */
public class AbstractExistingMoneyTransferEvent extends AbstractMoneyTransferEvent {

    private final int amount;
    private final boolean pending;
    private final boolean successful;

    public AbstractExistingMoneyTransferEvent(Object documentPrimaryKey, Object moneyTransferPrimaryKey, int amount, boolean pending, boolean successful) {
        super(documentPrimaryKey, moneyTransferPrimaryKey);
        this.amount = amount;
        this.pending = pending;
        this.successful = successful;
    }

    public AbstractExistingMoneyTransferEvent(MoneyTransfer moneyTransfer) {
        super(moneyTransfer);
        amount = moneyTransfer.getAmount();
        pending = moneyTransfer.isPending();
        successful = moneyTransfer.isSuccessful();
    }

    public MoneyTransfer getMoneyTransfer() {
        if (moneyTransfer == null && entityStore != null) {
            moneyTransfer = super.getMoneyTransfer();
            moneyTransfer.setAmount(getAmount());
            moneyTransfer.setPending(isPending());
            moneyTransfer.setSuccessful(isSuccessful());
        }
        return moneyTransfer;
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

}
