package one.modality.ecommerce.document.service;

import one.modality.ecommerce.document.service.events.DocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class SubmitDocumentChangesArgument {

    private final DocumentEvent[] documentEvents;

    public SubmitDocumentChangesArgument(DocumentEvent[] documentEvents) {
        this.documentEvents = documentEvents;
    }
}
