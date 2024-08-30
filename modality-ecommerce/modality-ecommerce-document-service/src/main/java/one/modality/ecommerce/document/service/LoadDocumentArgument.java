package one.modality.ecommerce.document.service;

/**
 * @author Bruno Salmon
 */
public final class LoadDocumentArgument {

    // Primary key of the document to load
    private final Object documentPrimaryKey;

    // Alternatively, primary key of the person and event. In that case, the document service will load the latest
    // document associated to that person for this event (or null if none is found).
    private final Object personPrimaryKey;
    private final Object eventPrimaryKey;

    // Optional history loading. If not null, it will load the document up to that history. If null, it will load the
    // latest version of the document.
    private final Object historyPrimaryKey;

    public LoadDocumentArgument(Object documentPrimaryKey) {
        this(documentPrimaryKey, null, null, null);
    }

    public LoadDocumentArgument(Object personPrimaryKey, Object eventPrimaryKey) {
        this(null, personPrimaryKey, eventPrimaryKey, null);
    }

    public LoadDocumentArgument(Object documentPrimaryKey, Object personPrimaryKey, Object eventPrimaryKey, Object historyPrimaryKey) {
        this.documentPrimaryKey = documentPrimaryKey;
        this.personPrimaryKey = personPrimaryKey;
        this.eventPrimaryKey = eventPrimaryKey;
        this.historyPrimaryKey = historyPrimaryKey;
    }

    public Object getDocumentPrimaryKey() {
        return documentPrimaryKey;
    }

    public Object getPersonPrimaryKey() {
        return personPrimaryKey;
    }

    public Object getEventPrimaryKey() {
        return eventPrimaryKey;
    }

    public Object getHistoryPrimaryKey() {
        return historyPrimaryKey;
    }
}
