package one.modality.ecommerce.document.service.events;

import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.Document;

/**
 * @author Bruno Salmon
 */
public final class AddDocumentEvent extends AbstractDocumentEvent {

    private final Object eventPrimaryKey;
    private final Object personPrimaryKey;

    public AddDocumentEvent(Document document) {
        super(document);
        eventPrimaryKey = Entities.getPrimaryKey(document.getEvent());
        personPrimaryKey = Entities.getPrimaryKey(document.getPerson());
    }

    public AddDocumentEvent(Object documentPrimaryKey, Object eventPrimaryKey, Object personPrimaryKey) {
        super(documentPrimaryKey);
        this.eventPrimaryKey = eventPrimaryKey;
        this.personPrimaryKey = personPrimaryKey;
    }

    public Object getEventPrimaryKey() {
        return eventPrimaryKey;
    }

    public Object getPersonPrimaryKey() {
        return personPrimaryKey;
    }
}
