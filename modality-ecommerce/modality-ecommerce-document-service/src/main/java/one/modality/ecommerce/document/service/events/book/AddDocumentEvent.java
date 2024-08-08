package one.modality.ecommerce.document.service.events.book;

import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Person;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class AddDocumentEvent extends AbstractDocumentEvent {

    private final Object eventPrimaryKey;
    // Booking with an account
    private Object personPrimaryKey;
    // Booking as a guest
    private String firstName;
    private String lastName;
    private String email;

    public AddDocumentEvent(Document document) {
        super(document);
        eventPrimaryKey = Entities.getPrimaryKey(document.getEvent());
        personPrimaryKey = Entities.getPrimaryKey(document.getPerson());
        firstName = document.getFirstName();
        lastName = document.getLastName();
        email = document.getEmail();
    }

    public AddDocumentEvent(Object documentPrimaryKey, Object eventPrimaryKey, Object personPrimaryKey, String firstName, String lastName, String email) {
        super(documentPrimaryKey);
        this.eventPrimaryKey = eventPrimaryKey;
        this.personPrimaryKey = personPrimaryKey;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public Document getDocument() {
        if (document == null && entityStore != null) {
            document = entityStore.createEntity(Document.class, getDocumentPrimaryKey());
            document.setEvent(entityStore.getEntity(Event.class, eventPrimaryKey));
            if (personPrimaryKey != null) {
                document.setPerson(entityStore.getOrCreateEntity(Person.class, personPrimaryKey));
            }
            document.setFirstName(firstName);
            document.setLastName(lastName);
            document.setEmail(email);
        }
        return super.getDocument();
    }
}
