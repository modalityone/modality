package one.modality.ecommerce.document.service;

import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class SubmitDocumentChangesArgument {

    private final AbstractDocumentEvent[] documentEvents;
    private final String historyComment;

    public SubmitDocumentChangesArgument(AbstractDocumentEvent[] documentEvents, String historyComment) {
        this.documentEvents = documentEvents;
        this.historyComment = historyComment;
    }

    public AbstractDocumentEvent[] getDocumentEvents() {
        return documentEvents;
    }

    public String getHistoryComment() {
        return historyComment;
    }
}
