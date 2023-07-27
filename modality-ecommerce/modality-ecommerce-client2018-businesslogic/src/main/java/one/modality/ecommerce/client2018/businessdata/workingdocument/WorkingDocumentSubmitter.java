package one.modality.ecommerce.client2018.businessdata.workingdocument;

import one.modality.hotel.shared2018.businessdata.time.DaysArray;
import one.modality.base.client2018.aggregates.event.EventAggregate;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Cart;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.db.submit.SubmitArgument;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.platform.util.uuid.Uuid;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class WorkingDocumentSubmitter {

    public static Future<Document> submit(WorkingDocument wd, String comment) {
        UpdateStore store = wd.getUpdateStore();
        EventAggregate eventAggregate = wd.getEventAggregate();
        WorkingDocument loadedWorkingDocument = wd.getLoadedWorkingDocument();
        Document du;
        if (loadedWorkingDocument != null)
            du = store.updateEntity(loadedWorkingDocument.getDocument());
        else {
            du = store.insertEntity(Document.class);
            du.setEvent(eventAggregate.getEvent());
            Cart cart = eventAggregate.getActiveCart();
            if (cart == null) {
                cart = store.insertEntity(Cart.class);
                cart.setUuid(Uuid.randomUuid());
            }
            du.setCart(cart);
        }
        WorkingDocument.syncPersonDetails(wd.getDocument(), du);
        for (WorkingDocumentLine wdl : wd.getWorkingDocumentLines()) {
            List<Attendance> attendances = wdl.getAttendances();
            DocumentLine dl = wdl.getDocumentLine(), dlu;
            if (dl == null && loadedWorkingDocument != null) {
                WorkingDocumentLine oldWdl = loadedWorkingDocument.findSameWorkingDocumentLine(wdl);
                if (oldWdl != null) {
                    dl = oldWdl.getDocumentLine();
                    attendances = oldWdl.getAttendances();
                }
            }
            if (dl == null) {
                dlu = store.insertEntity(DocumentLine.class);
                dlu.setDocument(du);
            } else
                dlu = store.updateEntity(dl);
            dlu.setSite(wdl.getSite());
            dlu.setItem(wdl.getItem());

            DaysArray daysArray = wdl.getDaysArray();
            int j = 0, m = Collections.size(attendances), n = daysArray.getArray().length;
            if (m > 0 && n == 0) // means that all attendances have been removed
                removeLine(wd, dl);
            else {
                for (int i = 0; i < n; i++) {
                    LocalDate date = daysArray.getDate(i);
                    while (j < m && attendances.get(j).getDate().compareTo(date) < 0) // isBefore() doesn't work on Android
                        store.deleteEntity(attendances.get(j++));
                    if (j < m && attendances.get(j).getDate().equals(date))
                        j++;
                    else {
                        Attendance au = store.insertEntity(Attendance.class);
                        au.setDate(date);
                        au.setDocumentLine(dlu);
                    }
                }
                while (j < m)
                    store.deleteEntity(attendances.get(j++));
            }
        }
        if (loadedWorkingDocument != null)
            for (WorkingDocumentLine lastWdl : loadedWorkingDocument.getWorkingDocumentLines()) {
                if (wd.findSameWorkingDocumentLine(lastWdl) == null)
                    removeLine(wd, lastWdl.getDocumentLine());
            }
        return store.submitChanges(SubmitArgument.builder()
                .setStatement("select set_transaction_parameters(false)")
                .setDataSourceId(store.getDataSourceId())
                .build())
                .map(batch -> du);
    }

    private static void removeLine(WorkingDocument wd, DocumentLine dl) {
        wd.getUpdateStore().deleteEntity(dl); // TODO: should probably be cancelled instead in some cases (and keep the non refundable part)
    }
}
