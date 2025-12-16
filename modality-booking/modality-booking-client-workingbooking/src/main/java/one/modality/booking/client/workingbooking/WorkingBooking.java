package one.modality.booking.client.workingbooking;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.util.Strings;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.util.DocumentLines;
import one.modality.base.shared.entities.util.ScheduledItems;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.ecommerce.document.service.*;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;
import one.modality.ecommerce.document.service.events.AbstractDocumentLineEvent;
import one.modality.ecommerce.document.service.events.book.*;
import one.modality.ecommerce.document.service.events.registration.documentline.PriceDocumentLineEvent;
import one.modality.ecommerce.document.service.util.DocumentEvents;
import one.modality.ecommerce.shared.pricecalculator.PriceCalculator;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Bruno Salmon
 */
public final class WorkingBooking {

    private final PolicyAggregate policyAggregate;
    private DocumentAggregate initialDocumentAggregate; // null for new bookings
    private final ObservableList<AbstractDocumentEvent> documentChanges = FXCollections.observableArrayList();
    // Making notEmptyBinding a class field to prevent GC in OpenJFX
    private final BooleanBinding notEmptyBinding = ObservableLists.isNotEmpty(documentChanges);
    // Note: hasChangesProperty is deferred so that it is always triggered after the changes (when there are several)
    private final ObservableValue<Boolean> hasChangesProperty = FXProperties.deferredProperty(notEmptyBinding);
    private Object documentPrimaryKey; // null for new booking
    private Document document;
    private DocumentAggregate lastestDocumentAggregate;
    // EntityStore used to hold the entities associated with this working booking (ex: Document, DocumentLine, etc...).
    // Note that it's not an update store because the booking submission uses DocumentService instead, which keeps a
    // record of all individual changes made over time. This entity store reflects only the latest version of the booking.
    private EntityStore entityStore;

    // Convenient flag to mark working bookings created from a user payment request.
    private final boolean paymentRequestedByUser;
    // Flag to mark that member was explicitly selected (e.g., via ExistingBookingSection)
    // When true, member selection page should be skipped
    private boolean memberExplicitlySelected;
    // Then a booking form can actually distinguish 3 cases from the working booking state:
    // 1) workingBooking.isNewBooking() == true => non-existing, new, initial booking (after pressing a Book Now button)
    // 2) isPaymentRequestedByUser() == true => existing, saved booking the user requested to pay from Orders page
    // 3) otherwise => existing booking that the user requested to modify from Orders page

    public WorkingBooking(PolicyAggregate policyAggregate, DocumentAggregate initialDocumentAggregate) {
        this(policyAggregate, initialDocumentAggregate, null);
    }

    public WorkingBooking(PolicyAggregate policyAggregate, DocumentAggregate initialDocumentAggregate, Object paymentRequestedByUserDocumentId) {
        this.policyAggregate = policyAggregate;
        this.initialDocumentAggregate = initialDocumentAggregate;
        if (initialDocumentAggregate != null) // Case of existing booking
            initialDocumentAggregate.setPolicyAggregate(policyAggregate);
        cancelChanges(); // sounds a bit weired, but this will actually initialize the document
        paymentRequestedByUser = Entities.samePrimaryKey(documentPrimaryKey, paymentRequestedByUserDocumentId);
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

    public boolean isPaymentRequestedByUser() {
        return paymentRequestedByUser;
    }

    public boolean isMemberExplicitlySelected() {
        return memberExplicitlySelected;
    }

    public void setMemberExplicitlySelected(boolean memberExplicitlySelected) {
        this.memberExplicitlySelected = memberExplicitlySelected;
    }

    public void bookScheduledItems(List<ScheduledItem> scheduledItems, boolean addOnly) {
        if (scheduledItems.isEmpty())
            return;
        // A first draft version assuming all scheduled items are referring to the same site and items
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
        Attendance[] newAttendances = scheduledItems.stream().map(scheduledItem -> {
            // Checking that the scheduledItem is not already in the existing attendances
            if (Collections.findFirst(existingAttendances, a -> Objects.equals(a.getScheduledItem(), scheduledItem)) != null)
                return null;
            // Second check, but using the date
            if (Collections.findFirst(existingAttendances, a -> Objects.equals(a.getDate(), scheduledItem.getDate())) != null)
                return null;
            // Note: attendances are not directly submitted to the database but incorporated in document events, so we
            // don't use insertEntity() but createEntity() to create them in memory.
            Attendance newAttendance = getEntityStore().createEntity(Attendance.class);
            // Note: documentLine may come from another store (ex: initial document aggregate), and in that case, the
            // following assignment will copy it in this store, but not its related entities, such as document.
            newAttendance.setDocumentLine(documentLine); // may copy documentLine if from another store, but not document
            // However, it's really necessary that newAttendance.getDocumentLine().getDocument() doesn't return null
            // when passing it to new AttendancesEvent() because all document events must know which document they are
            // referring to. So we ensure this by the following assignment:
            newAttendance.getDocumentLine().setDocument(document); // may copy the document to the store as well
            newAttendance.setDate(scheduledItem.getDate());
            newAttendance.setScheduledItem(scheduledItem);
            return newAttendance;
        }).filter(Objects::nonNull).toArray(Attendance[]::new);
        if (newAttendances.length > 0)
            integrateNewDocumentEvent(new AddAttendancesEvent(newAttendances), false);
        if (!addOnly) {
            // We remove all existing attendances not referencing the passed scheduledItems
            List<Attendance> attendancesToRemove = Collections.filter(existingAttendances, a ->
                Collections.findFirst(scheduledItems, si -> Entities.sameId(a.getScheduledItem(), si)) == null);
            removeAttendances(attendancesToRemove);
        }
        lastestDocumentAggregate = null;
    }

    public void unbookScheduledItems(List<ScheduledItem> scheduledItems) {
        removeAttendances(Collections.filter(getAttendancesAdded(false), a -> scheduledItems.contains(a.getScheduledItem())));
    }

    public boolean areScheduledItemsBooked(List<ScheduledItem> scheduledItems) {
        if (scheduledItems.isEmpty())
            return false;
        // A first draft version assuming all scheduled items are referring to the same site and items
        ScheduledItem scheduledItemSample = scheduledItems.get(0);
        Site site = scheduledItemSample.getSite();
        Item item = scheduledItemSample.getItem();
        DocumentLine existingDocumentLine = getLastestDocumentAggregate().getFirstSiteItemDocumentLine(site, item);
        return existingDocumentLine != null;
    }

    public void bookWholeEvent() {
        bookScheduledItems(ScheduledItems.filterNotCancelled(policyAggregate.filterTeachingScheduledItems()), false);
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

    public void applyFacilityFeeRate(boolean apply) {
        integrateNewDocumentEvent(new ApplyFacilityFeeEvent(document, apply), true);
    }

    public void addRequest(String request) {
        integrateNewDocumentEvent(new AddRequestEvent(document, request), true);
    }

    private void integrateNewDocumentEvent(AbstractDocumentEvent e, boolean applyImmediatelyToDocument) {
        if (applyImmediatelyToDocument) {
            e.replayEventOnDocument();
            if (e instanceof AbstractDocumentLineEvent dle)
                dle.replayEventOnDocumentLine();
        }
        DocumentEvents.integrateNewDocumentEvent(e, documentChanges, initialDocumentAggregate == null ? null : initialDocumentAggregate.getNewDocumentEvents());
    }

    public boolean hasNoChanges() {
        return documentChanges.isEmpty();
    }

    public boolean hasChanges() {
        return !hasNoChanges();
    }

    public ObservableValue<Boolean> hasChangesProperty() {
        return hasChangesProperty;
    }

    public boolean hasDocumentLineChanges() {
        return Collections.findFirst(documentChanges, e -> e instanceof AbstractDocumentLineEvent) != null;
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

    /**
     * Prepares this WorkingBooking for a completely new booking, discarding any
     * previously submitted booking data. This should be called when the user wants
     * to make a new booking (e.g., for another person) after completing a previous one.
     *
     * Unlike cancelChanges() which reverts to the last submitted state, this method
     * truly starts fresh with a new Document entity.
     */
    public void startNewBooking() {
        initialDocumentAggregate = null;
        documentPrimaryKey = null;
        cancelChanges(); // Will create a new Document because initialDocumentAggregate is null
    }

    public Future<SubmitDocumentChangesResult> submitChanges(String historyComment) {
        // In case the booking is not linked to the booker account - because the user was not logged in at the start of
        // the booking process - we set it now. (the front-office probably forced the user to log in before submit).
        if (document.isNew()) {
            documentChanges.forEach(e -> {
                if (e instanceof AddDocumentEvent ade) {
                    ade.setPersonLang(Strings.toString(I18n.getLanguage()));
                    Person personToBook = FXPersonToBook.getPersonToBook();
                    if (personToBook != null)
                        ade.setPersonPrimaryKey(Entities.getPrimaryKey(personToBook));
                    // If booked as a guest
                    ade.setFirstName(document.getFirstName());
                    ade.setLastName(document.getLastName());
                    ade.setEmail(document.getEmail());
                }
            });
        }

        // We submit the booking changes
        return DocumentService.submitDocumentChanges(
            new SubmitDocumentChangesArgument(historyComment, documentChanges.toArray(new AbstractDocumentEvent[0]))
        ).compose(result -> {
            // The submitting was successful at this point, and we reload the latest version of the booking TODO: make this as on option in SubmitDocumentChangesArgument
            return DocumentService.loadDocument(new LoadDocumentArgument(result.getDocumentPrimaryKey()))
                .map(documentAggregate -> {
                    // We set the polityAggregate (was already loaded), and this also rebuilds internal entities (document, changes)
                    documentAggregate.setPolicyAggregate(policyAggregate);
                    // We reset this working booking from this up-to-date document aggregate
                    initialDocumentAggregate = documentAggregate;
                    cancelChanges(); // sounds a bit weired, but this will actually initialize the document
                    return result;
                });
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

    public Stream<Attendance> getAlreadyBookedAttendancesStream() {
        DocumentAggregate initialDocumentAggregate = getInitialDocumentAggregate();
        if (initialDocumentAggregate == null)
            return Stream.empty();
        return initialDocumentAggregate.getAttendancesStream();
    }

    public List<ScheduledItem> getAlreadyBookedScheduledItems() {
        return getAlreadyBookedAttendancesStream()
            .map(Attendance::getScheduledItem)
            .collect(Collectors.toList());
    }

    public List<DocumentLine> getDocumentLines() {
        return getLastestDocumentAggregate().getDocumentLines();
    }

    public List<DocumentLine> getFamilyDocumentLines(KnownItemFamily family) {
        return DocumentLines.filterFamily(getDocumentLines(), family);
    }

    public List<DocumentLine> getTeachingDocumentLines() {
        return getFamilyDocumentLines(KnownItemFamily.TEACHING);
    }

    public DocumentLine getTeachingDocumentLine() {
        return Collections.first(getTeachingDocumentLines());
    }

    public void applyDocumentLineFreeOfCharge(DocumentLine documentLine, boolean freeOfCharge) {
        if (isDocumentLineFreeOfCharge(documentLine) == freeOfCharge)
            return;
        integrateNewDocumentEvent(PriceDocumentLineEvent.createDocumentLineDiscountEvent(documentLine, freeOfCharge ? 100 : 0), true);
    }

    public boolean isTeachingFreeOfCharge() {
        return isDocumentLineFreeOfCharge(getTeachingDocumentLine());
    }

    public void applyTeachingFreeOfCharge(boolean freeOfCharge) {
        applyDocumentLineFreeOfCharge(getTeachingDocumentLine(), freeOfCharge);
    }

    public boolean isDocumentLineFreeOfCharge(DocumentLine documentLine) {
        return DocumentLines.isFreeOfCharge(documentLine);
    }

    // Shorthand methods to PolicyAggregate

    public List<ScheduledItem> getPolicyScheduledItems() {
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
        return priceCalculator.calculateNoLongStayDiscountTotalPrice();
    }

    public List<Attendance> getBookedAttendances() {
        List<Attendance> attendancesAdded = getAttendancesAdded(false);
        List<Attendance> attendancesRemoved = getAttendancesRemoved(false);
        return Collections.filter(attendancesAdded, attendance -> !attendancesRemoved.contains(attendance));
    }

    // Shorthand methods to lastestDocumentAggregate

    public List<Attendance> getAttendancesAdded(boolean fromChangesOnly) {
        return getLastestDocumentAggregate().getAttendancesAdded(fromChangesOnly);
    }

    public List<Attendance> getAttendancesRemoved(boolean fromChangesOnly) {
        return getLastestDocumentAggregate().getAttendancesRemoved(fromChangesOnly);
    }

    public AddRequestEvent findAddRequestEvent(boolean fromChangesOnly) {
        return getLastestDocumentAggregate().findAddRequestEvent(fromChangesOnly);
    }

    public PriceDocumentLineEvent findPriceDocumentLineEvent(boolean fromChangesOnly) {
        return getLastestDocumentAggregate().findPriceDocumentLineEvent(fromChangesOnly);
    }

    // Static factory and loading methods

    public static WorkingBooking createWholeEventWorkingBooking(PolicyAggregate policyAggregate) {
        WorkingBooking workingBooking = new WorkingBooking(policyAggregate, null);
        workingBooking.bookWholeEvent();
        return workingBooking;
    }

    public static Future<WorkingBooking> loadWorkingBooking(Document document) {
        return DocumentService.loadDocumentWithPolicy(document)
            .map(policyAndDocumentAggregates -> {
                PolicyAggregate policyAggregate = policyAndDocumentAggregates.getPolicyAggregate(); // never null
                DocumentAggregate existingBooking = policyAndDocumentAggregates.getDocumentAggregate(); // might be null
                return new WorkingBooking(policyAggregate, existingBooking);
            });
    }

}
