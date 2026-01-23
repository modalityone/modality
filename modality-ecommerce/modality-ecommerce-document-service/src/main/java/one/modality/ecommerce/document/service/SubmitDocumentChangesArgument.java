package one.modality.ecommerce.document.service;

import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public record SubmitDocumentChangesArgument(
    String historyComment,
    AbstractDocumentEvent[] documentEvents,
    boolean queueCapable
) {

    // Alternative convenient constructor for simple changes (1 change in most cases but possibly several) that avoids
    // array creation. In addition, queueCapable is false by default in this case because such simple changes are made
    // in other context than the booking form, so they don't support queueing.
    public SubmitDocumentChangesArgument(
        String historyComment,
        AbstractDocumentEvent... documentEvents
    ) {
        this(historyComment, documentEvents, false);
    }
}
