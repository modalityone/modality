package one.modality.event.frontoffice.activities.book.fx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.shared.entities.MoneyTransfer;

/**
 * @author Bruno Salmon
 */
public final class FXResumePaymentMoneyTransfer {

    private final static ObjectProperty<MoneyTransfer> resumePaymentMoneyTransferProperty = new SimpleObjectProperty<>();

    public static MoneyTransfer getResumePaymentMoneyTransfer() {
        return resumePaymentMoneyTransferProperty.get();
    }

    public static ObjectProperty<MoneyTransfer> resumePaymentMoneyTransferProperty() {
        return resumePaymentMoneyTransferProperty;
    }

    public static void setResumePaymentMoneyTransfer(MoneyTransfer guestToBook) {
        resumePaymentMoneyTransferProperty.set(guestToBook);
    }

}
