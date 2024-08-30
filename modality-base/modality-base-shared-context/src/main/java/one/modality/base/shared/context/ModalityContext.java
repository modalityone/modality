package one.modality.base.shared.context;

/**
 * @author Bruno Salmon
 */
public class ModalityContext {

    private Object organizationId;
    private Object eventId;
    private Object documentId;
    private Object magicLinkId;

    public ModalityContext(Object organizationId, Object eventId, Object documentId, Object magicLinkId) {
        this.organizationId = organizationId;
        this.eventId = eventId;
        this.documentId = documentId;
        this.magicLinkId = magicLinkId;
    }

    public Object getOrganizationId() {
        return organizationId;
    }

    public Object getEventId() {
        return eventId;
    }

    public Object getDocumentId() {
        return documentId;
    }

    public Object getMagicLinkId() {
        return magicLinkId;
    }

    // Maybe it would be better to have a ModalityContextBuilder and make this class immutable

    public void setOrganizationId(Object organizationId) {
        this.organizationId = organizationId;
    }

    public void setEventId(Object eventId) {
        this.eventId = eventId;
    }

    public void setDocumentId(Object documentId) {
        this.documentId = documentId;
    }

    public void setMagicLinkId(Object magicLinkId) {
        this.magicLinkId = magicLinkId;
    }
}
