package one.modality.ecommerce.document.service.events.book;

import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class ApplyFacilityFeeEvent extends AbstractDocumentEvent {

    private final boolean apply;

    public ApplyFacilityFeeEvent(Object documentPrimaryKey, boolean apply) {
        super(documentPrimaryKey);
        this.apply = apply;
    }

    public ApplyFacilityFeeEvent(Document document, boolean apply) {
        super(document);
        this.apply = apply;
    }

    public boolean isApply() {
        return apply;
    }

    @Override
    public void replayEventOnDocument() {
        super.replayEventOnDocument();
        document.setPersonFacilityFee(apply);
    }
}
