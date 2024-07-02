package one.modality.ecommerce.document.service.events;

import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Person;

/**
 * @author Bruno Salmon
 */
public final class AddDocumentEvent extends AbstractDocumentEvent {

    private final Object eventPrimaryKey;
    private Object personPrimaryKey;

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

    public void setPersonPrimaryKey(Object personPrimaryKey) {
        this.personPrimaryKey = personPrimaryKey;
    }

    @Override
    public Document getDocument() {
        if (document == null && entityStore != null) {
            document = entityStore.createEntity(Document.class, getDocumentPrimaryKey());
            document.setEvent(entityStore.getEntity(Event.class, eventPrimaryKey));
            if (personPrimaryKey != null) {
                document.setPerson(entityStore.getOrCreateEntity(Person.class, personPrimaryKey));
            }
        }
        return super.getDocument();
    }
}
