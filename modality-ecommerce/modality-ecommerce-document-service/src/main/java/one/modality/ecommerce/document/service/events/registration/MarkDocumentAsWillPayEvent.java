package one.modality.ecommerce.document.service.events.registration;

import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public class MarkDocumentAsWillPayEvent extends AbstractDocumentEvent {

    private final boolean willPay;

    public MarkDocumentAsWillPayEvent(Object documentPrimaryKey, boolean willPay) {
        super(documentPrimaryKey);
        this.willPay = willPay;
    }

    public MarkDocumentAsWillPayEvent(Document document, boolean willPay) {
        super(document);
        this.willPay = willPay;
    }

    public boolean isWillPay() {
        return willPay;
    }

    @Override
    public void replayEventOnDocument() {
        super.replayEventOnDocument();
        document.setWillPay(willPay);
    }
}
