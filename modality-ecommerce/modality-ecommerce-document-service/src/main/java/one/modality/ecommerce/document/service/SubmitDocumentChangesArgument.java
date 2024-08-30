package one.modality.ecommerce.document.service;

import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class SubmitDocumentChangesArgument {

    private final String historyComment;
    private final AbstractDocumentEvent[] documentEvents;

    public SubmitDocumentChangesArgument(String historyComment, AbstractDocumentEvent... documentEvents) {
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
