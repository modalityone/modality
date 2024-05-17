package one.modality.ecommerce.document.service;

import one.modality.base.shared.entities.*;
import one.modality.ecommerce.document.service.events.AddAttendancesEvent;
import one.modality.ecommerce.document.service.events.AddDocumentLineEvent;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;
import one.modality.ecommerce.document.service.events.RemoveAttendancesEvent;

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

    private final Document document;
    private final List<DocumentLine> documentLines;
    private final List<Attendance> attendances;

    public DocumentAggregate(Document document, List<DocumentLine> documentLines, List<Attendance> attendances) {
        this.document = document;
        this.documentLines = documentLines;
        this.attendances = attendances;
    }

    public DocumentAggregate(DocumentAggregate previousVersion, List<AbstractDocumentEvent> documentEvents) {
        documentLines = new ArrayList<>();
        attendances = new ArrayList<>();
        if (previousVersion != null) {
            document = previousVersion.getDocument();
            documentLines.addAll(previousVersion.getDocumentLines());
            attendances.addAll(previousVersion.getAttendances());
        } else {
            document = null;
        }
        documentEvents.forEach(documentEvent -> {
            if (documentEvent instanceof AddDocumentLineEvent) {
                documentLines.add(((AddDocumentLineEvent) documentEvent).getDocumentLine());
            } else if (documentEvent instanceof AddAttendancesEvent) {
                attendances.addAll(Arrays.asList(((AddAttendancesEvent) documentEvent).getAttendances()));
            } else if (documentEvent instanceof RemoveAttendancesEvent) {
                attendances.removeAll(Arrays.asList(((RemoveAttendancesEvent) documentEvent).getAttendances()));
            }
        });
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

}
