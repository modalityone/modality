package one.modality.ecommerce.document.service;

import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class SubmitDocumentChangesArgument {

    private final AbstractDocumentEvent[] documentEvents;

    public SubmitDocumentChangesArgument(AbstractDocumentEvent[] documentEvents) {
        this.documentEvents = documentEvents;
    }

    public AbstractDocumentEvent[] getDocumentEvents() {
        return documentEvents;
    }
}
