package one.modality.event.frontoffice.activities.booking;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.EntityStore;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.AddAttendancesEvent;
import one.modality.ecommerce.document.service.events.AddDocumentLineEvent;
import one.modality.ecommerce.document.service.events.DocumentEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Salmon
 */
public class WorkingBooking {

    private final PolicyAggregate policyAggregate;
    private final DocumentAggregate initialDocumentAggregate; // null for new bookings
    private final List<DocumentEvent> documentChanges = new ArrayList<>();
    private DocumentAggregate lastestDocumentAggregate;
    private EntityStore entityStore;

    public WorkingBooking(PolicyAggregate policyAggregate) {
        this(policyAggregate, null);
    }

    public WorkingBooking(PolicyAggregate policyAggregate, DocumentAggregate initialDocumentAggregate) {
        this.policyAggregate = policyAggregate;
        this.initialDocumentAggregate = initialDocumentAggregate;
    }

    public PolicyAggregate getPolicyAggregate() {
        return policyAggregate;
    }

    public DocumentAggregate getInitialDocumentAggregate() {
        return initialDocumentAggregate;
    }

    public DocumentAggregate getLastestDocumentAggregate() {
        if (lastestDocumentAggregate == null)
            lastestDocumentAggregate = new DocumentAggregate(initialDocumentAggregate, documentChanges);
        return lastestDocumentAggregate;
    }

    public void bookScheduledItems(List<ScheduledItem> scheduledItems) {
        if (scheduledItems.isEmpty())
            return;
        // first draft version assuming it's a new booking and new line
        lastestDocumentAggregate = null;
        DocumentLine documentLine = getEntityStore().createEntity(DocumentLine.class);
        documentLine.setSite(scheduledItems.get(0).getSite());
        documentLine.setItem(scheduledItems.get(0).getItem());
        documentChanges.add(new AddDocumentLineEvent(documentLine));
        Attendance[] attendances = scheduledItems.stream().map(scheduledItem -> {
            Attendance attendance = getEntityStore().createEntity(Attendance.class);
            attendance.setDocumentLine(documentLine);
            attendance.setDate(scheduledItem.getDate());
            attendance.setScheduledItem(scheduledItem);
            return attendance;
        }).toArray(Attendance[]::new);
        documentChanges.add(new AddAttendancesEvent(attendances));
    }

    public void cancelChanges() {
        documentChanges.clear();
        entityStore = null;
        lastestDocumentAggregate = null;
    }

    public Future<Object> submitChanges() {
        return DocumentService.submitDocumentChanges(new SubmitDocumentChangesArgument(documentChanges.toArray(new DocumentEvent[0])));
    }

    private EntityStore getEntityStore() {
        if (entityStore == null)
            entityStore = EntityStore.create(DataSourceModelService.getDefaultDataSourceModel());
        return entityStore;
    }

    /* Previous code

    private List<ScheduledItem> scheduledItems;
    private List<DocumentLine> unscheduledLines;
    public List<ScheduledItem> getScheduledItems() {
        return scheduledItems;
    }

    public void setScheduledItems(List<ScheduledItem> scheduledItems) {
        this.scheduledItems = scheduledItems;
    }

    public List<DocumentLine> getUnscheduledLines() {
        return unscheduledLines;
    }

    public void setUnscheduledLines(List<DocumentLine> unscheduledLines) {
        this.unscheduledLines = unscheduledLines;
    }

    public LocalDateTime getArrivalDate() {
        if (scheduledItems== null || scheduledItems.isEmpty()) {
            return null;
        }
        LocalDateTime minDateTime = LocalDateTime.of(scheduledItems.get(0).getDate(), scheduledItems.get(0).getStartTime());
        for (ScheduledItem si : scheduledItems) {
            LocalDateTime currentDateTime = LocalDateTime.of(si.getDate(), si.getStartTime());
            if (currentDateTime.isBefore(minDateTime)) {
                minDateTime = currentDateTime;
            }
        }
        return minDateTime;
    }

    public LocalDateTime getDepartureDate() {
        if (scheduledItems== null || scheduledItems.isEmpty()) {
            return null;
        }
        LocalDateTime maxDateTime = LocalDateTime.of(scheduledItems.get(0).getDate(), scheduledItems.get(0).getEndTime());
        for (ScheduledItem si : scheduledItems) {
            LocalDateTime currentDateTime = LocalDateTime.of(si.getDate(), si.getEndTime());
            if (currentDateTime.isAfter(maxDateTime)) {
                maxDateTime = currentDateTime;
            }
        }
        return maxDateTime;
    }
*/
}
