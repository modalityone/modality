package one.modality.ecommerce.document.service;

import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.ecommerce.document.service.events.AddAttendancesEvent;
import one.modality.ecommerce.document.service.events.AddDocumentLineEvent;
import one.modality.ecommerce.document.service.events.DocumentEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    public DocumentAggregate(DocumentAggregate previousVersion, List<DocumentEvent> documentEvents) {
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
            }
        });
    }

    public Document getDocument() {
        return document;
    }

    public List<DocumentLine> getDocumentLines() {
        return documentLines;
    }

    public List<Attendance> getAttendances() {
        return attendances;
    }

    public List<Attendance> getLineAttendances(DocumentLine line) {
        return attendances.stream().filter(a -> a.getDocumentLine() == line).collect(Collectors.toList());
    }

}
