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

    public LoadDocumentArgument(Object documentPrimaryKey) {
        this(documentPrimaryKey, null, null);
    }

    public LoadDocumentArgument(Object personPrimaryKey, Object eventPrimaryKey) {
        this(null, personPrimaryKey, eventPrimaryKey);
    }

    public LoadDocumentArgument(Object documentPrimaryKey, Object personPrimaryKey, Object eventPrimaryKey) {
        this.documentPrimaryKey = documentPrimaryKey;
        this.personPrimaryKey = personPrimaryKey;
        this.eventPrimaryKey = eventPrimaryKey;
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
}
