package one.modality.ecommerce.document.service;

import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.EntityStore;
import one.modality.base.shared.entities.*;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;
import one.modality.ecommerce.document.service.events.book.*;
import one.modality.ecommerce.document.service.events.registration.documentline.PriceDocumentLineEvent;
import one.modality.ecommerce.document.service.events.registration.documentline.RemoveDocumentLineEvent;
import one.modality.ecommerce.document.service.events.registration.moneytransfer.RemoveMoneyTransferEvent;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Bruno Salmon
 */
public final class DocumentAggregate {

    private final DocumentAggregate previousVersion;
    private final List<AbstractDocumentEvent> newDocumentEvents;

    private PolicyAggregate policyAggregate;

    private Document document;
    private List<DocumentLine> documentLines;
    private List<Attendance> attendances;
    private List<MoneyTransfer> moneyTransfers;
    private int existingDocumentLinesCount;
    private int existingAttendancesCount;
    private int existingMoneyTransfersCount;

    // Constructor for new bookings built from scratch
    public DocumentAggregate(PolicyAggregate policyAggregate) {
        this(null, null);
        setPolicyAggregate(policyAggregate);
    }

    // Constructor for new working bookings built on top of an existing booking
    public DocumentAggregate(DocumentAggregate previousVersion) {
        this(previousVersion, null);
    }

    // Constructor for existing bookings
    public DocumentAggregate(List<AbstractDocumentEvent> newDocumentEvents) {
        this(null, newDocumentEvents);
    }

    // Constructor for serialization
    public DocumentAggregate(DocumentAggregate previousVersion, List<AbstractDocumentEvent> newDocumentEvents) {
        this.previousVersion = previousVersion;
        this.newDocumentEvents = newDocumentEvents;
        if (previousVersion != null) {
            setPolicyAggregate(previousVersion.getPolicyAggregate());
        }
    }

    public void setPolicyAggregate(PolicyAggregate policyAggregate) {
        this.policyAggregate = policyAggregate;
        // Rebuilding the document in memory by replaying the sequence of events
        documentLines = new ArrayList<>();
        attendances = new ArrayList<>();
        moneyTransfers = new ArrayList<>();
        EntityStore entityStore;
        if (previousVersion != null) {
            previousVersion.setPolicyAggregate(policyAggregate);
            document = previousVersion.getDocument();
            documentLines.addAll(previousVersion.getDocumentLines());
            attendances.addAll(previousVersion.getAttendances());
            moneyTransfers.addAll(previousVersion.getMoneyTransfers());
            entityStore = EntityStore.createAbove(document.getStore());
        } else {
            entityStore = EntityStore.createAbove(policyAggregate.getEntityStore());
        }
        existingDocumentLinesCount = documentLines.size();
        existingAttendancesCount = attendances.size();
        existingMoneyTransfersCount = moneyTransfers.size();
        newDocumentEvents.forEach(e -> {
            e.setEntityStore(entityStore);
            if (e instanceof AddDocumentLineEvent adle) {
                documentLines.add(adle.getDocumentLine());
            } else if (e instanceof RemoveDocumentLineEvent rdle) {
                documentLines.remove(rdle.getDocumentLine());
            } else if (e instanceof AddAttendancesEvent aae) {
                attendances.addAll(Arrays.asList(aae.getAttendances()));
            } else if (e instanceof RemoveAttendancesEvent rae) {
                attendances.removeAll(Arrays.asList(rae.getAttendances()));
            } else if (e instanceof AddMoneyTransferEvent ate) {
                moneyTransfers.add(ate.getMoneyTransfer());
            } else if (e instanceof RemoveMoneyTransferEvent rmte) {
                moneyTransfers.remove(rmte.getMoneyTransfer());
            } else { // Ex: AddDocumentEvent, CancelDocumentEvent, UpdateMoneyTransferEvent, etc...
                e.replayEvent();
                //Console.log("⚠️ DocumentAggregate doesn't recognize this event: " + e.getClass());
            }
            if (document == null)
                document = e.getDocument();
        });
        if (document == null)
            document = entityStore.createEntity(Document.class);
    }

    public PolicyAggregate getPolicyAggregate() {
        return policyAggregate;
    }

    public DocumentAggregate getPreviousVersion() {
        return previousVersion;
    }

    public List<AbstractDocumentEvent> getNewDocumentEvents() {
        return newDocumentEvents;
    }

    public List<AbstractDocumentEvent> getNewDocumentEvents(boolean excludePreviousVersions) {
        if (excludePreviousVersions)
            return getNewDocumentEvents();

        List<AbstractDocumentEvent> allEvents = new ArrayList<>();
        DocumentAggregate current = this;
        while (current != null) {
            allEvents.addAll(current.getNewDocumentEvents());
            current = current.getPreviousVersion();
        }

        // Then create a stream of all events from all aggregates and filter it
        return allEvents;
    }

    public Stream<AbstractDocumentEvent> getNewDocumentEventsStream(boolean excludePreviousVersionEvents) {
        return getNewDocumentEvents(excludePreviousVersionEvents).stream();
    }

    public Stream<AddAttendancesEvent> getAddAttendancesEventStream(boolean excludePreviousVersionEvents) {
        return getNewDocumentEventsStream(excludePreviousVersionEvents)
            // Not GWT compatible
            //.filter(AddAttendancesEvent.class::isInstance)
            //.map(AddAttendancesEvent.class::cast)
            .filter(e -> e instanceof AddAttendancesEvent)
            .map(e -> (AddAttendancesEvent) e);
    }

    public Stream<Attendance> getAttendancesAddedStream(boolean excludePreviousVersionEvents) {
        return getAddAttendancesEventStream(excludePreviousVersionEvents)
            .flatMap(e -> java.util.Arrays.stream(e.getAttendances()));
    }

    public List<Attendance> getAttendancesAdded(boolean excludePreviousVersionEvents) {
        return getAttendancesAddedStream(excludePreviousVersionEvents)
            .collect(Collectors.toList());
    }

    public Stream<RemoveAttendancesEvent> getRemoveAttendancesEventStream(boolean excludePreviousVersionEvents) {
        return getNewDocumentEventsStream(excludePreviousVersionEvents)
            .filter(e -> e instanceof RemoveAttendancesEvent)
            .map(e -> (RemoveAttendancesEvent) e);
    }

    public Stream<Attendance> getAttendancesRemovedStream(boolean excludePreviousVersionEvents) {
        return getRemoveAttendancesEventStream(excludePreviousVersionEvents)
            .flatMap(e -> java.util.Arrays.stream(e.getAttendances()));
    }

    public List<Attendance> getAttendancesRemoved(boolean excludePreviousVersionEvents) {
        return getAttendancesRemovedStream(excludePreviousVersionEvents)
            .collect(Collectors.toList());
    }

    public AddDocumentEvent findAddDocumentEvent(boolean excludePreviousVersionEvents) {
        return getNewDocumentEventsStream(excludePreviousVersionEvents)
            .filter(e -> e instanceof AddDocumentEvent)
            .map(e -> (AddDocumentEvent) e)
            .findFirst().orElse(null);
    }

    public AddRequestEvent findAddRequestEvent(boolean excludePreviousVersionEvents) {
        return getNewDocumentEventsStream(excludePreviousVersionEvents)
            .filter(e -> e instanceof AddRequestEvent)
            .map(e -> (AddRequestEvent) e)
            .findFirst().orElse(null);
    }

    public AddDocumentLineEvent findLatestAddDocumentLineEvent(boolean excludePreviousVersionEvents) {
        return getNewDocumentEventsStream(excludePreviousVersionEvents)
            .filter(e -> e instanceof AddDocumentLineEvent)
            .map(e -> (AddDocumentLineEvent) e)
            .reduce((first, second) -> second) // to get the last one
            .orElse(null);
    }

    public PriceDocumentLineEvent findPriceDocumentLineEvent(boolean excludePreviousVersionEvents) {
        return getNewDocumentEventsStream(excludePreviousVersionEvents)
            .filter(e -> e instanceof PriceDocumentLineEvent)
            .map(e -> (PriceDocumentLineEvent) e)
            .findFirst().orElse(null);
    }

    public ApplyFacilityFeeEvent findApplyFacilityFeeEvent(boolean excludePreviousVersionEvents) {
        return getNewDocumentEventsStream(excludePreviousVersionEvents)
            .filter(e -> e instanceof ApplyFacilityFeeEvent)
            .map(e -> (ApplyFacilityFeeEvent) e)
            .findFirst().orElse(null);
    }

    // Accessing event

    public Event getEvent() {
        if (policyAggregate != null)
            return policyAggregate.getEvent();
        if (document != null)
            return document.getEvent();
        return null;
    }

    public Object getEventPrimaryKey() {
        Event event = getEvent();
        if (event != null)
            return event.getPrimaryKey();
        AddDocumentEvent ade = getAddDocumentEvent();
        if (ade != null)
            return ade.getEventPrimaryKey();
        if (previousVersion != null)
            return previousVersion.getEventPrimaryKey();
        return null;
    }

    private AddDocumentEvent getAddDocumentEvent() {
        return (AddDocumentEvent) Collections.findFirst(newDocumentEvents, e -> e instanceof AddDocumentEvent);
    }

    public Object getDocumentPrimaryKey() {
        if (document != null)
            return document.getPrimaryKey();
        AddDocumentEvent ade = getAddDocumentEvent();
        if (ade != null)
            return ade.getDocumentPrimaryKey();
        if (previousVersion != null)
            return previousVersion.getDocumentPrimaryKey();
        return null;
    }

    public Integer getDocumentRef() {
        if (document != null)
            return document.getRef();
        AddDocumentEvent ade = getAddDocumentEvent();
        if (ade != null)
            return ade.getRef();
        if (previousVersion != null)
            return previousVersion.getDocumentRef();
        return null;
    }

    // Accessing document

    public Document getDocument() {
        return document;
    }

    public String getAttendeeFirstName() {
        return getAddDocumentEvent().getFirstName();
    }

    public String getAttendeeLastName() {
        return getAddDocumentEvent().getLastName();
    }

    public String getAttendeeFullName() {
        return getAttendeeFirstName() + " " + getAttendeeLastName();
    }

    public String getAttendeeEmail() {
        return getAddDocumentEvent().getEmail();
    }

    // Accessing document lines

    public List<DocumentLine> getDocumentLines() {
        return documentLines;
    }

    public Stream<DocumentLine> getDocumentLinesStream() {
        return documentLines.stream();
    }

    public Stream<DocumentLine> getSiteItemDocumentLinesStream(Site site, Item item) {
        return getDocumentLinesStream()
                .filter(line -> Objects.equals(line.getSite(), site) && Objects.equals(line.getItem(), item));
    }

    public List<DocumentLine> getSiteItemDocumentLines(Site site, Item item) {
        return getSiteItemDocumentLinesStream(site, item)
                .collect(Collectors.toList());
    }

    public DocumentLine getFirstSiteItemDocumentLine(Site site, Item item) {
        return getSiteItemDocumentLinesStream(site, item).findFirst().orElse(null);
    }

    public Stream<DocumentLine> getExistingDocumentLinesStream() {
        return documentLines.stream().limit(existingDocumentLinesCount);
    }

    public Stream<DocumentLine> getNewDocumentLinesStream() {
        return documentLines.stream().skip(existingDocumentLinesCount);
    }


    // Accessing attendances

    public List<Attendance> getAttendances() {
        return attendances;
    }

    public Stream<Attendance> getAttendancesStream() {
        return attendances.stream();
    }

    public Stream<Attendance> getLineAttendancesStream(DocumentLine line) {
        return getAttendancesStream()
                .filter(a -> Objects.equals(a.getDocumentLine(), line));
    }

    public List<Attendance> getLineAttendances(DocumentLine line) {
        return getLineAttendancesStream(line)
                .collect(Collectors.toList());
    }

    public Stream<Attendance> getExistingAttendancesStream() {
        return attendances.stream().limit(existingAttendancesCount);
    }

    public Stream<Attendance> getNewAttendancesStream() {
        return attendances.stream().skip(existingAttendancesCount);
    }

    // Accessing money transfers

    public List<MoneyTransfer> getMoneyTransfers() {
        return moneyTransfers;
    }

    public MoneyTransfer getLastMoneyTransfer() {
        return Collections.last(moneyTransfers);
    }

    public Stream<MoneyTransfer> getMoneyTransfersStream() {
        return moneyTransfers.stream();
    }

    public Stream<MoneyTransfer> getPendingMoneyTransfersStream() {
        return moneyTransfers.stream()
                .filter(MoneyTransfer::isPending);
    }

    public boolean hasPendingMoneyTransfers() {
        return getPendingMoneyTransfersStream().findAny().isPresent();
    }

    public Stream<MoneyTransfer> getSuccessfulMoneyTransfersStream() {
        return moneyTransfers.stream()
                .filter(MoneyTransfer::isSuccessful);
    }

    public Stream<MoneyTransfer> getExistingMoneyTransfersStream() {
        return moneyTransfers.stream().limit(existingMoneyTransfersCount);
    }

    public Stream<MoneyTransfer> getNewMoneyTransfersStream() {
        return moneyTransfers.stream().skip(existingMoneyTransfersCount);
    }

}
