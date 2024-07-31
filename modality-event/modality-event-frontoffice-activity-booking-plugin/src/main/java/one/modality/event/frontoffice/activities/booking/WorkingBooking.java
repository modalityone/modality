package one.modality.event.frontoffice.activities.booking;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.util.Arrays;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.EntityStore;
import one.modality.base.shared.entities.*;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;
import one.modality.ecommerce.document.service.*;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;
import one.modality.ecommerce.document.service.events.book.*;
import one.modality.ecommerce.document.service.util.DocumentEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public class WorkingBooking {

    private final PolicyAggregate policyAggregate;
    private final DocumentAggregate initialDocumentAggregate; // null for new bookings
    private final List<AbstractDocumentEvent> documentChanges = new ArrayList<>();
    private Object documentPrimaryKey; // null for new booking
    private Document document;
    private DocumentAggregate lastestDocumentAggregate;
    // EntityStore used to hold the entities associated to this working booking (ex: Document, DocumentLine, etc...).
    // Note that it's not an update store, because the booking submit uses DocumentService instead, which keeps a record
    // of all individual changes made over the time. This entity store reflects only the latest version of the booking.
    private EntityStore entityStore;

    public WorkingBooking(PolicyAggregate policyAggregate, DocumentAggregate initialDocumentAggregate) {
        this.policyAggregate = policyAggregate;
        this.initialDocumentAggregate = initialDocumentAggregate;
        if (initialDocumentAggregate != null) {
            initialDocumentAggregate.setPolicyAggregate(policyAggregate);
            documentPrimaryKey = initialDocumentAggregate.getDocument().getPrimaryKey();
        }
    }

    public PolicyAggregate getPolicyAggregate() {
        return policyAggregate;
    }

    public DocumentAggregate getInitialDocumentAggregate() {
        return initialDocumentAggregate;
    }

    public DocumentAggregate getLastestDocumentAggregate() {
        if (lastestDocumentAggregate == null) {
            lastestDocumentAggregate = new DocumentAggregate(initialDocumentAggregate, documentChanges);
            lastestDocumentAggregate.setPolicyAggregate(policyAggregate);
        }
        return lastestDocumentAggregate;
    }

    public Object getDocumentPrimaryKey() {
        return documentPrimaryKey;
    }

    public Event getEvent() {
        return policyAggregate.getEvent();
    }

    public void bookScheduledItems(List<ScheduledItem> scheduledItems) {
        if (scheduledItems.isEmpty())
            return;
        // first draft version assuming it's a new booking and new line
        ScheduledItem scheduledItemSample = scheduledItems.get(0);
        Site site = scheduledItemSample.getSite();
        Item item = scheduledItemSample.getItem();
        DocumentLine existingDocumentLine = getLastestDocumentAggregate().getFirstSiteItemDocumentLine(site, item);
        DocumentLine documentLine;
        List<Attendance> existingAttendances;
        if (existingDocumentLine != null) {
            documentLine = existingDocumentLine;
            existingAttendances = getLastestDocumentAggregate().getLineAttendances(existingDocumentLine);
        } else {
            documentLine = getEntityStore().createEntity(DocumentLine.class);
            documentLine.setDocument(document);
            documentLine.setSite(site);
            documentLine.setItem(item);
            integrateNewDocumentEvent(new AddDocumentLineEvent(documentLine));
            existingAttendances = null;
        }
        Attendance[] attendances = scheduledItems.stream().map(scheduledItem -> {
            if (Collections.findFirst(existingAttendances, a -> Objects.equals(a.getDate(), scheduledItem.getDate())) != null)
                return null;
            Attendance attendance = getEntityStore().createEntity(Attendance.class);
            attendance.setDocumentLine(documentLine);
            attendance.setDate(scheduledItem.getDate());
            attendance.setScheduledItem(scheduledItem);
            return attendance;
        }).filter(Objects::nonNull).toArray(Attendance[]::new);
        if (attendances.length > 0)
            integrateNewDocumentEvent(new AddAttendancesEvent(attendances));
        lastestDocumentAggregate = null;
    }

    public void removeAttendance(Attendance attendance) {
        removeAttendances(java.util.Collections.singletonList(attendance));
    }

    public void removeAttendances(List<Attendance> attendance) {
        integrateNewDocumentEvent(new RemoveAttendancesEvent(attendance.toArray(new Attendance[0])));
        lastestDocumentAggregate = null;
    }

    public void cancelBooking() {
        if (document == null || document.isNew()) {
            cancelChanges();
        } else {
            integrateNewDocumentEvent(new CancelDocumentEvent(document, true));
            lastestDocumentAggregate = null;
        }
    }

    private void integrateNewDocumentEvent(AbstractDocumentEvent e) {
        DocumentEvents.integrateNewDocumentEvent(e, documentChanges);
    }

    public boolean hasChanges() {
        return !documentChanges.isEmpty();
    }

    public void cancelChanges() {
        documentChanges.clear();
        entityStore = null;
        lastestDocumentAggregate = null;
        if (initialDocumentAggregate != null) {
            document = initialDocumentAggregate.getDocument();
            documentPrimaryKey = document.getPrimaryKey();
        } else {
            if (documentPrimaryKey == null) { // Case of new booking not yet submitted
                document = getEntityStore().createEntity(Document.class);
                document.setEvent(getEvent());
                document.setPerson(FXUserPerson.getUserPerson());
                integrateNewDocumentEvent(new AddDocumentEvent(document));
            } else { // Case of new booking once submitted
                document = getEntityStore().createEntity(Document.class, documentPrimaryKey);
            }
        }
    }

    public Future<SubmitDocumentChangesResult> submitChanges(String historyComment) {
        // In case the booking is not linked to the booker account (because the user was not logged-in at the start of
        // the booking process), we set it now (the front-office probably forced the user to login before submit).
        Person userPerson = FXUserPerson.getUserPerson();
        if (document.getPerson() == null && userPerson != null) {
            document.setPerson(userPerson);
            documentChanges.forEach(e -> {
                if (e instanceof AddDocumentEvent) {
                    AddDocumentEvent ade = (AddDocumentEvent) e;
                    if (ade.getPersonPrimaryKey() == null)
                        ade.setPersonPrimaryKey(userPerson.getPrimaryKey());
                }
            });
        }

        return DocumentService.submitDocumentChanges(
                new SubmitDocumentChangesArgument(
                        historyComment, documentChanges.toArray(new AbstractDocumentEvent[0])
                )).map(result -> {
                    documentPrimaryKey = result.getDocumentPrimaryKey();
                    cancelChanges(); // Because successfully submitted
                    return result;
        });
    }

    private EntityStore getEntityStore() {
        if (entityStore == null)
            entityStore = EntityStore.createAbove(policyAggregate.getEntityStore());
        return entityStore;
    }

    public boolean isNewBooking() {
        return this.getInitialDocumentAggregate()==null;
    }

    public List<AbstractDocumentEvent> getDocumentChanges() {
        return documentChanges;
    }

    public Attendance[] getAttendanceAdded() {
        List<Attendance> list = new ArrayList<>();
        for (AbstractDocumentEvent currentEvent : documentChanges) {
            if(currentEvent instanceof AddAttendancesEvent) list.addAll(Arrays.asList(((AddAttendancesEvent) currentEvent).getAttendances()));
        }
        return list.toArray(new Attendance[0]);
    }
    public Attendance[] getAttendanceRemoved() {
        List<Attendance> list = new ArrayList<>();
        for (AbstractDocumentEvent currentEvent : documentChanges) {
            if(currentEvent instanceof RemoveAttendancesEvent) list.addAll(Arrays.asList(((RemoveAttendancesEvent) currentEvent).getAttendances()));
        }
        return list.toArray(new Attendance[0]);
    }
}
