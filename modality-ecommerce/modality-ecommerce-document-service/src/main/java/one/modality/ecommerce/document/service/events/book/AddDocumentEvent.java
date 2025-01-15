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
    private Integer ref;

    public AddDocumentEvent(Document document) {
        super(document);
        eventPrimaryKey = Entities.getPrimaryKey(document.getEvent());
        personPrimaryKey = Entities.getPrimaryKey(document.getPerson());
        firstName = document.getFirstName();
        lastName = document.getLastName();
        email = document.getEmail();
        ref = document.getRef();
    }

    public AddDocumentEvent(Object documentPrimaryKey, Object eventPrimaryKey, Object personPrimaryKey, String firstName, String lastName, String email, Integer ref) {
        super(documentPrimaryKey);
        this.eventPrimaryKey = eventPrimaryKey;
        this.personPrimaryKey = personPrimaryKey;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.ref = ref;
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

    public Integer getRef() {
        return ref;
    }

    public void setRef(Integer ref) {
        this.ref = ref;
    }

    @Override
    protected void createDocument() {
        if (isForSubmit()) {
            document = updateStore.insertEntity(Document.class, getDocumentPrimaryKey());
            document.setFieldValue("activity", 12); // GP Class TODO: remove activity from DB
        } else {
            super.createDocument();
        }
    }

    @Override
    public void replayEventOnDocument() {
        super.replayEventOnDocument();
        document.setEvent(isForSubmit() ? getEventPrimaryKey() : entityStore.getOrCreateEntity(Event.class, getEventPrimaryKey()));
        Object personPrimaryKey = getPersonPrimaryKey();
        if (personPrimaryKey != null) {
            document.setPerson(isForSubmit() ? personPrimaryKey : entityStore.getOrCreateEntity(Person.class, personPrimaryKey));
        }
        document.setFirstName(firstName);
        document.setLastName(lastName);
        document.setEmail(email);
        if (ref != null)
            document.setRef(ref);
    }
}
