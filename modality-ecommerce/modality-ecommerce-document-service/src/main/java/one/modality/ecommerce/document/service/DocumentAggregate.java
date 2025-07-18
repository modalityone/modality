package one.modality.ecommerce.document.service;

import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.EntityStore;
import one.modality.base.shared.entities.*;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;
import one.modality.ecommerce.document.service.events.book.*;
import one.modality.ecommerce.document.service.events.registration.documentline.RemoveDocumentLineEvent;
import one.modality.ecommerce.document.service.events.registration.moneytransfer.RemoveMoneyTransferEvent;

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

    public DocumentAggregate getPreviousVersion() {
        return previousVersion;
    }

    public List<AbstractDocumentEvent> getNewDocumentEvents() {
        return newDocumentEvents;
    }

    public PolicyAggregate getPolicyAggregate() {
        return policyAggregate;
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
        AddDocumentEvent ade = (AddDocumentEvent) Collections.findFirst(newDocumentEvents, e -> e instanceof AddDocumentEvent);
        if (ade != null)
            return ade.getEventPrimaryKey();
        if (previousVersion != null)
            return previousVersion.getEventPrimaryKey();
        return null;
    }

    // Accessing document

    public Document getDocument() {
        return document;
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

    public int getDeposit() {
        return getSuccessfulMoneyTransfersStream().mapToInt(MoneyTransfer::getAmount).sum();
    }

    public int getPendingDeposit() {
        return getPendingMoneyTransfersStream().mapToInt(MoneyTransfer::getAmount).sum();
    }

}
