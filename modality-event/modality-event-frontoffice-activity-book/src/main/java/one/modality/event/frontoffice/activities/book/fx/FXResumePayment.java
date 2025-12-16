package one.modality.event.frontoffice.activities.book.fx;

import dev.webfx.platform.util.collection.Collections;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.MoneyTransfer;
import one.modality.ecommerce.payment.PaymentAllocation;

import java.util.List;
import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public final class FXResumePayment {

    private final static ObjectProperty<MoneyTransfer> moneyTransferProperty = new SimpleObjectProperty<>();
    private static Document[] documents;
    private static PaymentAllocation[] paymentAllocations;

    public static MoneyTransfer getMoneyTransfer() {
        return moneyTransferProperty.get();
    }

    public static ObjectProperty<MoneyTransfer> moneyTransferProperty() {
        return moneyTransferProperty;
    }

    public static void setMoneyTransfer(MoneyTransfer moneyTransfer) {
        moneyTransferProperty.set(moneyTransfer);
    }

    public static void setMoneyTransfers(List<MoneyTransfer> moneyTransfers) {
        documents = moneyTransfers.stream().map(MoneyTransfer::getDocument).filter(Objects::nonNull).toArray(Document[]::new);
        paymentAllocations = moneyTransfers.stream().filter(mt -> mt.getDocumentId() != null).map(mt -> new PaymentAllocation(mt.getDocumentId().getPrimaryKey(), mt.getAmount())).toArray(PaymentAllocation[]::new);
        setMoneyTransfer(Collections.first(moneyTransfers));
    }

    public static int getAmount() {
        return getMoneyTransfer().getAmount();
    }

    public static void setDocuments(Document[] documents) {
        FXResumePayment.documents = documents;
    }

    public static Document[] getDocuments() {
        return documents;
    }

    public static PaymentAllocation[] getPaymentAllocations() {
        return paymentAllocations;
    }

}
