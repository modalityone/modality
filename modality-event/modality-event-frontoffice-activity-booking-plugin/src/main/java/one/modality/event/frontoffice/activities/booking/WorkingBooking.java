package one.modality.event.frontoffice.activities.booking;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.EntityStore;
import one.modality.base.shared.entities.*;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;
import one.modality.ecommerce.document.service.*;
import one.modality.ecommerce.document.service.events.*;
import one.modality.event.client.event.fx.FXEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public class WorkingBooking {

    private final PolicyAggregate policyAggregate;
    private final DocumentAggregate initialDocumentAggregate; // null for new bookings
    private final List<AbstractDocumentEvent> documentChanges = new ArrayList<>();
    private Document document;
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
            documentChanges.add(new AddDocumentLineEvent(documentLine));
            existingAttendances = null;
        }
        Attendance[] attendances = scheduledItems.stream().map(scheduledItem -> {
            if (dev.webfx.platform.util.collection.Collections.findFirst(existingAttendances, a -> Objects.equals(a.getDate(), scheduledItem.getDate())) != null)
                return null;
            Attendance attendance = getEntityStore().createEntity(Attendance.class);
            attendance.setDocumentLine(documentLine);
            attendance.setDate(scheduledItem.getDate());
            attendance.setScheduledItem(scheduledItem);
            return attendance;
        }).filter(Objects::nonNull).toArray(Attendance[]::new);
        if (attendances.length > 0)
            documentChanges.add(new AddAttendancesEvent(attendances));
        lastestDocumentAggregate = null;
    }

    public void removeAttendance(Attendance attendance) {
        removeAttendances(Collections.singletonList(attendance));
    }

    public void removeAttendances(List<Attendance> attendance) {
        documentChanges.add(new RemoveAttendancesEvent(attendance.toArray(new Attendance[0])));
        lastestDocumentAggregate = null;
    }

    public void cancelChanges() {
        documentChanges.clear();
        entityStore = null;
        lastestDocumentAggregate = null;
        if (initialDocumentAggregate != null)
            document = initialDocumentAggregate.getDocument();
        else {
            document = getEntityStore().createEntity(Document.class);
            document.setEvent(FXEvent.getEvent());
            document.setPerson(FXUserPerson.getUserPerson());
            documentChanges.add(new AddDocumentEvent(document));
        }
    }

    public Future<SubmitDocumentChangesResult> submitChanges(String historyComment) {
        return DocumentService.submitDocumentChanges(
                new SubmitDocumentChangesArgument(
                        documentChanges.toArray(new AbstractDocumentEvent[0]),
                        historyComment
                ));
    }

    private EntityStore getEntityStore() {
        if (entityStore == null)
            entityStore = EntityStore.create(DataSourceModelService.getDefaultDataSourceModel());
        return entityStore;
    }

}
