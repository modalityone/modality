package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.MoneyTransfer;

/**
 * @author Bruno Salmon
 */
public class AbstractMoneyTransferEvent extends AbstractDocumentEvent {

    protected MoneyTransfer moneyTransfer;
    private Object moneyTransferPrimaryKey;

    protected boolean playedOnMoneyTransfer;

    public AbstractMoneyTransferEvent(Object documentPrimaryKey, Object moneyTransferPrimaryKey) {
        super(documentPrimaryKey);
        this.moneyTransferPrimaryKey = moneyTransferPrimaryKey;
    }

    public AbstractMoneyTransferEvent(MoneyTransfer moneyTransfer) {
        super(moneyTransfer.getDocument());
        this.moneyTransfer = moneyTransfer;
        moneyTransferPrimaryKey = moneyTransfer.getPrimaryKey();
    }

    public MoneyTransfer getMoneyTransfer() {
        if (moneyTransfer == null && entityStore != null) {
            createMoneyTransfer();
        }
        if (moneyTransfer != null && !playedOnMoneyTransfer)
            replayEventOnMoneyTransfer();
        return moneyTransfer;
    }

    public Object getMoneyTransferPrimaryKey() {
        return moneyTransferPrimaryKey;
    }

    public void setMoneyTransferPrimaryKey(Object moneyTransferPrimaryKey) {
        this.moneyTransferPrimaryKey = moneyTransferPrimaryKey;
    }

    protected void createMoneyTransfer() {
        if (isForSubmit())
            moneyTransfer = updateStore.updateEntity(MoneyTransfer.class, getMoneyTransferPrimaryKey());
        else
            moneyTransfer = entityStore.getOrCreateEntity(MoneyTransfer.class, getMoneyTransferPrimaryKey());
    }

    protected void replayEventOnMoneyTransfer() {
        moneyTransfer.setDocument(getDocument());
        playedOnMoneyTransfer = true;
    }
}
