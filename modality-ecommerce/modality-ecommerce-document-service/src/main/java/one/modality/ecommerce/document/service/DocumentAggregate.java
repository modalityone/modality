package one.modality.ecommerce.document.service;

import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.EntityStore;
import one.modality.base.shared.entities.*;
import one.modality.ecommerce.document.service.events.*;

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
    private final List<AbstractDocumentEvent> documentEvents;

    private Document document;
    private List<DocumentLine> documentLines;
    private List<Attendance> attendances;
    private List<MoneyTransfer> moneyTransfers;

    public DocumentAggregate(DocumentAggregate previousVersion, List<AbstractDocumentEvent> documentEvents) {
        this.previousVersion = previousVersion;
        this.documentEvents = documentEvents;
    }

    public void rebuildDocument(PolicyAggregate policyAggregate) {
        // Rebuilding the document in memory by replaying the sequence of events
        documentLines = new ArrayList<>();
        attendances = new ArrayList<>();
        moneyTransfers = new ArrayList<>();
        EntityStore entityStore;
        if (previousVersion != null) {
            previousVersion.rebuildDocument(policyAggregate);
            document = previousVersion.getDocument();
            documentLines.addAll(previousVersion.getDocumentLines());
            attendances.addAll(previousVersion.getAttendances());
            entityStore = EntityStore.createAbove(document.getStore());
        } else {
            entityStore = EntityStore.createAbove(policyAggregate.getEntityStore());
        }
        documentEvents.forEach(e -> {
            e.setEntityStore(entityStore);
            if (e instanceof AddDocumentEvent) {
                AddDocumentEvent ade = (AddDocumentEvent) e;
                if (documentLines.isEmpty() && attendances.isEmpty()) {
                    document = ade.getDocument();
                } else
                    throw new IllegalArgumentException("There should be only one AddDocumentEvent");
            } else if (e instanceof AddDocumentLineEvent) {
                documentLines.add(((AddDocumentLineEvent) e).getDocumentLine());
            } else if (e instanceof AddAttendancesEvent) {
                attendances.addAll(Arrays.asList(((AddAttendancesEvent) e).getAttendances()));
            } else if (e instanceof RemoveAttendancesEvent) {
                attendances.removeAll(Arrays.asList(((RemoveAttendancesEvent) e).getAttendances()));
            } else if (e instanceof AddMoneyTransferEvent) {
                moneyTransfers.add(((AddMoneyTransferEvent) e).getMoneyTransfer());
            } else if (e instanceof UpdateMoneyTransferEvent) {
                ((UpdateMoneyTransferEvent) e).getMoneyTransfer(); // This should be enough to update the money transfer
            } else {
                Console.log("⚠️ DocumentAggregate doesn't recognize this event: " + e.getClass());
            }
        });
    }

    public DocumentAggregate getPreviousVersion() {
        return previousVersion;
    }

    public List<AbstractDocumentEvent> getDocumentEvents() {
        return documentEvents;
    }

    public Document getDocument() {
        return document;
    }

    public List<DocumentLine> getDocumentLines() {
        return documentLines;
    }

    public Stream<DocumentLine> getDocumentLinesStream() {
        return documentLines.stream();
    }

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

    public int getDeposit() {
        return getSuccessfulMoneyTransfersStream().mapToInt(MoneyTransfer::getAmount).sum();
    }

    public int getPendingDeposit() {
        return getPendingMoneyTransfersStream().mapToInt(MoneyTransfer::getAmount).sum();
    }

}
