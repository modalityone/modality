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

    // Alternative factory method for simple changes (1 change in most cases but possibly several) that avoids
    // array creation. In addition, queueCapable is false by default in this case because such simple changes are made
    // in contexts that don't support queueing (ex: client dialog or server call), as opposed to the context of a
    // booking form which can show the progress of the bookings queue.

    // Note: providing a second constructor instead of a factory method causes a GWT crash

    public static SubmitDocumentChangesArgument of(String historyComment, AbstractDocumentEvent... documentEvents) {
        return new SubmitDocumentChangesArgument(historyComment, documentEvents, false);
    }
}
