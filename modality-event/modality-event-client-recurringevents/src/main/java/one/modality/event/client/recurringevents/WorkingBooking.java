package one.modality.event.client.recurringevents;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.util.Arrays;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.shared.entities.*;
import one.modality.ecommerce.document.service.*;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;
import one.modality.ecommerce.document.service.events.book.*;
import one.modality.ecommerce.document.service.util.DocumentEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
public class WorkingBooking {

    private final PolicyAggregate policyAggregate;
    private final DocumentAggregate initialDocumentAggregate; // null for new bookings
    private final ObservableList<AbstractDocumentEvent> documentChanges = FXCollections.observableArrayList();
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
        if (initialDocumentAggregate != null) { // Case of existing booking
            initialDocumentAggregate.setPolicyAggregate(policyAggregate);
            documentPrimaryKey = initialDocumentAggregate.getDocument().getPrimaryKey();
        } else // Case of new booking
            cancelChanges(); // sounds a bit weired, but this will actually initialize the document
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

    public Document getDocument() {
        return document;
    }

    public Object getDocumentPrimaryKey() {
        return documentPrimaryKey;
    }

    public Event getEvent() {
        return policyAggregate.getEvent();
    }

    public void bookScheduledItems(List<ScheduledItem> scheduledItems, boolean addOnly) {
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
            integrateNewDocumentEvent(new AddDocumentLineEvent(documentLine), false);
            existingAttendances = null;
        }
        Attendance[] attendances = scheduledItems.stream().map(scheduledItem -> {
            // Checking that the scheduledItem is not already in the existing attendances
            if (Collections.findFirst(existingAttendances, a -> Objects.equals(a.getScheduledItem(), scheduledItem)) != null)
                return null;
            // Second check, but using the date
            if (Collections.findFirst(existingAttendances, a -> Objects.equals(a.getDate(), scheduledItem.getDate())) != null)
                return null;
            Attendance attendance = getEntityStore().createEntity(Attendance.class);
            attendance.setDocumentLine(documentLine);
            attendance.setDate(scheduledItem.getDate());
            attendance.setScheduledItem(scheduledItem);
            return attendance;
        }).filter(Objects::nonNull).toArray(Attendance[]::new);
        if (attendances.length > 0)
            integrateNewDocumentEvent(new AddAttendancesEvent(attendances), false);
        if (!addOnly) {
            // We remove all existing attendances not referencing the passed scheduledItems
            List<Attendance> attendancesToRemove = Collections.filter(existingAttendances, a ->
                Collections.findFirst(scheduledItems, si -> a.getScheduledItem() == si) == null);
            removeAttendances(attendancesToRemove);
        }
        lastestDocumentAggregate = null;
    }

    public void removeAttendance(Attendance attendance) {
        removeAttendances(java.util.Collections.singletonList(attendance));
    }

    public void removeAttendances(List<Attendance> attendance) {
        if (!attendance.isEmpty()) {
            integrateNewDocumentEvent(new RemoveAttendancesEvent(attendance.toArray(new Attendance[0])), false);
            lastestDocumentAggregate = null;
        }
    }

    public void cancelBooking() {
        if (document == null || document.isNew()) {
            cancelChanges();
        } else {
            integrateNewDocumentEvent(new CancelDocumentEvent(document, true), true);
            lastestDocumentAggregate = null;
        }
    }

    public void uncancelBooking() {
        integrateNewDocumentEvent(new CancelDocumentEvent(document, false), true);
        lastestDocumentAggregate = null;
    }

    public void applyFacilityFeeRate() {
        applyFacilityFeeRate(true);
    }

    public void removeFacilityFeeRate() {
        applyFacilityFeeRate(false);
    }

    public void applyFacilityFeeRate(boolean apply) {
        integrateNewDocumentEvent(new ApplyFacilityFeeDocumentEvent(document, apply), true);
    }

    private void integrateNewDocumentEvent(AbstractDocumentEvent e, boolean applyImmediatelyToDocument) {
        if (applyImmediatelyToDocument)
            e.replayEventOnDocument();
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
                document.setPerson(FXPersonToBook.getPersonToBook());
                integrateNewDocumentEvent(new AddDocumentEvent(document), false);
            } else { // Case of new booking once submitted
                document = getEntityStore().createEntity(Document.class, documentPrimaryKey);
            }
        }
    }

    public Future<SubmitDocumentChangesResult> submitChanges(String historyComment) {
        // In case the booking is not linked to the booker account (because the user was not logged-in at the start of
        // the booking process), we set it now (the front-office probably forced the user to login before submit).
        if (document.isNew()) {
            documentChanges.forEach(e -> {
                if (e instanceof AddDocumentEvent) {
                    AddDocumentEvent ade = (AddDocumentEvent) e;
                    if (ade.getPersonPrimaryKey() == null)
                        ade.setPersonPrimaryKey(Entities.getPrimaryKey(FXPersonToBook.getPersonToBook()));
                    ade.setFirstName(document.getFirstName());
                    ade.setLastName(document.getLastName());
                    ade.setEmail(document.getEmail());
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
        return getInitialDocumentAggregate() == null;
    }

    public ObservableList<AbstractDocumentEvent> getDocumentChanges() {
        return documentChanges;
    }

    public List<Attendance> getAttendanceAdded() {
        List<Attendance> list = new ArrayList<>();
        for (AbstractDocumentEvent currentEvent : documentChanges) {
            if (currentEvent instanceof AddAttendancesEvent)
                list.addAll(Arrays.asList(((AddAttendancesEvent) currentEvent).getAttendances()));
        }
        return list;
    }

    public List<Attendance> getAttendanceRemoved() {
        List<Attendance> list = new ArrayList<>();
        for (AbstractDocumentEvent currentEvent : documentChanges) {
            if (currentEvent instanceof RemoveAttendancesEvent)
                list.addAll(Arrays.asList(((RemoveAttendancesEvent) currentEvent).getAttendances()));
        }
        return list;
    }

    public List<ScheduledItem> getScheduledItemsAlreadyBooked() {
        DocumentAggregate initialDocumentAggregate = getInitialDocumentAggregate();
        if (initialDocumentAggregate == null) {
            return Collections.emptyList();
        }
        return initialDocumentAggregate.getAttendancesStream()
            .map(Attendance::getScheduledItem)
            .collect(Collectors.toList());
    }

    // Shorthand methods to PolicyAggregate

    public List<ScheduledItem> getScheduledItemsOnEvent() {
        return getPolicyAggregate().getScheduledItems();
    }

    public int getDailyRatePrice() {
        return getPolicyAggregate().getDailyRatePrice();
    }

    public int getWholeEventPrice() {
        WorkingBooking workingBooking = createWholeEventWorkingBooking(getPolicyAggregate());
        PriceCalculator priceCalculator = new PriceCalculator(workingBooking.getLastestDocumentAggregate());
        return priceCalculator.calculateTotalPrice();
    }

    public int getWholeEventNoDiscountPrice() {
        WorkingBooking workingBooking = createWholeEventWorkingBooking(getPolicyAggregate());
        PriceCalculator priceCalculator = new PriceCalculator(workingBooking.getLastestDocumentAggregate());
        return priceCalculator.calculateNoDiscountTotalPrice();
    }

    public static WorkingBooking createWholeEventWorkingBooking(PolicyAggregate policyAggregate) {
        WorkingBooking workingBooking = new WorkingBooking(policyAggregate, null);
        workingBooking.bookScheduledItems(policyAggregate.getTeachingScheduledItems(), false);
        return workingBooking;
    }

}
