package one.modality.ecommerce.document.service.util;

import dev.webfx.platform.util.Arrays;
import dev.webfx.platform.util.Strings;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.ecommerce.document.service.events.AbstractAttendancesEvent;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;
import one.modality.ecommerce.document.service.events.AbstractDocumentLineEvent;
import one.modality.ecommerce.document.service.events.book.*;
import one.modality.ecommerce.document.service.events.registration.documentline.RemoveDocumentLineEvent;

import java.util.Iterator;
import java.util.List;

/**
 * Utility class to help keep a list of document events minimized.
 *
 * @author Bruno Salmon
 */
public final class DocumentEvents {

    public static void integrateNewDocumentEvent(AbstractDocumentEvent e, List<AbstractDocumentEvent> documentEvents) {
        e = simplifyDocumentEvent(e, documentEvents); // May simplify the event and even return null if it's not necessary to add it
        if (e != null) {
            documentEvents.add(e);
        }
    }

    private static AbstractDocumentEvent simplifyDocumentEvent(AbstractDocumentEvent e, List<AbstractDocumentEvent> documentEvents) {
        if (e instanceof AddDocumentLineEvent adle) {
            return simplifyAddDocumentLineEvent(adle, documentEvents);
        }
        if (e instanceof RemoveDocumentLineEvent rdle) {
            return simplifyRemoveDocumentLineEvent(rdle, documentEvents);
        }
        if (e instanceof AddAttendancesEvent aae) {
            return simplifyAddAttendancesEvent(aae, documentEvents);
        }
        if (e instanceof RemoveAttendancesEvent rae) {
            return simplifyRemoveAttendancesEvent(rae, documentEvents);
        }
        if (e instanceof ApplyFacilityFeeDocumentEvent affe) {
            return simplifyApplyFacilityFeeDocumentEvent(affe, documentEvents);
        }
        if (e instanceof AddRequestEvent) {
            return simplifyAddRequestEvent((AddRequestEvent) e, documentEvents);
        }
        return e;
    }

    private static AbstractDocumentEvent simplifyAddDocumentLineEvent(AddDocumentLineEvent adle, List<AbstractDocumentEvent> documentEvents) {
        DocumentLine documentLine = adle.getDocumentLine();
        // If this line was previously removed, we can just cancel that removal instead of adding the document line again
        for (Iterator<AbstractDocumentEvent> it = documentEvents.iterator(); it.hasNext(); ) {
            AbstractDocumentEvent e = it.next();
            if (e instanceof RemoveDocumentLineEvent rdle && sameDocumentLine(rdle.getDocumentLine(), documentLine)) {
                it.remove();
                adle = null;
            }
        }
        return adle;
    }

    private static AbstractDocumentEvent simplifyRemoveDocumentLineEvent(RemoveDocumentLineEvent rdle, List<AbstractDocumentEvent> documentEvents) {
        DocumentLine documentLine = rdle.getDocumentLine();
        // Since we remove this document line, we can simplify the changes by removing all those related to that document line
        for (Iterator<AbstractDocumentEvent> it = documentEvents.iterator(); it.hasNext(); ) {
            AbstractDocumentEvent e = it.next();
            if (e instanceof AbstractDocumentLineEvent adle && sameDocumentLine(adle.getDocumentLine(), documentLine)) {
                it.remove();
                // In addition, if we found that this document line was added within these changes, we return null
                // to indicate that it's even not necessary to add this event to the present changes
                if (e instanceof AddDocumentLineEvent) {
                    rdle = null;
                }
            }
        }
        return rdle;
    }

    private static AbstractDocumentEvent simplifyAddAttendancesEvent(AddAttendancesEvent aae, List<AbstractDocumentEvent> documentEvents) {
        return simplifyAttendancesEvent(aae, documentEvents);
    }

    private static AbstractDocumentEvent simplifyRemoveAttendancesEvent(RemoveAttendancesEvent rae, List<AbstractDocumentEvent> documentEvents) {
        return simplifyAttendancesEvent(rae, documentEvents);
    }

    private static AbstractDocumentEvent simplifyAttendancesEvent(AbstractAttendancesEvent ae, List<AbstractDocumentEvent> documentEvents) {
        Attendance[] initialAttendances = ae.getAttendances(); // removed or added attendances
        Attendance[] reducedAttendances = initialAttendances; // reduced
        DocumentLine documentLine = ae.getDocumentLine();
        // Since this event may undo previous events, we simplify by removing the undone events (partially or totally)
        for (int i = 0; i < documentEvents.size() && reducedAttendances.length > 0; i++) {
            AbstractDocumentEvent e = documentEvents.get(i);
            // Only attendance events operating on the same document line can be simplified
            if (e instanceof AbstractAttendancesEvent aae && sameDocumentLine(aae.getDocumentLine(), documentLine)) { // AddAttendancesEvent or RemoveAttendancesEvent
                Attendance[] eventAttendances = aae.getAttendances();
                // If the existing event is not of the same class, it is exclusive with the new event,
                if (ae.getClass() != aae.getClass()) { // ex: RemoveAttendance and AddAttendance
                    // In that case we can remove the overlapping period, i.e., keep only the attendances that are not
                    // overlapping initialAttendances
                    Attendance[] remainingAttendances = excludeAttendances(eventAttendances, initialAttendances);
                    if (remainingAttendances.length == 0) { // If all attendances disappeared,
                        documentEvents.remove(i--); // we can simply remove this event
                    } else if (remainingAttendances.length < eventAttendances.length) { // if some disappeared but not all,
                        // we replace this event with the same event but covering only the remaining attendances
                        documentEvents.set(i, cloneWithNewAttendances(aae, remainingAttendances));
                    }
                }
                // In the same way, the new event can be reduced, and we can remove the overlapping period too.
                // This is true for both exclusive events and joining events (no need to repeat the same attendance
                // in the later case).
                reducedAttendances = excludeAttendances(reducedAttendances, eventAttendances);
                // TODO: if there are still attendances on both joining events, we should merge them into 1 single event
            }
        }
        if (ae instanceof RemoveAttendancesEvent) {
            // We also remove the associated document line if there are no attendances anymore to it
            if (Collections.findFirst(documentEvents, e -> e instanceof AddAttendancesEvent aae && aae.getDocumentLine() == documentLine) == null)
                integrateNewDocumentEvent(new RemoveDocumentLineEvent(documentLine), documentEvents); // Note that we don't integrate this RemoveDocumentLineEvent
        }
        // If at the end of this simplification, there is no more attendances to keep in RemoveAttendancesEvent
        if (reducedAttendances.length == 0) {
            return null; // we return null to indicate that it's not necessary anymore to add it in the document changes
        }
        // If some were removed but not all, we simplify this event with only the necessary attendances
        if (reducedAttendances.length < initialAttendances.length) {
            ae = cloneWithNewAttendances(ae, reducedAttendances);
        }
        return ae;
    }

    private static Attendance[] excludeAttendances(Attendance[] attendances, Attendance[] toExclude) {
        return Arrays.filter(attendances, a -> Arrays.findFirst(toExclude, ea -> Entities.samePrimaryKey(ea.getScheduledItem(), a.getScheduledItem())) == null, Attendance[]::new);
    }

    private static AbstractAttendancesEvent cloneWithNewAttendances(AbstractAttendancesEvent ae, Attendance[] attendances) {
        if (ae instanceof AddAttendancesEvent)
            return new AddAttendancesEvent(attendances);
        else if (ae instanceof RemoveAttendancesEvent)
            return new RemoveAttendancesEvent(attendances);
        else
            throw new IllegalArgumentException("Unknown AttendancesEvent type: " + ae);
    }

    private static AbstractDocumentEvent simplifyApplyFacilityFeeDocumentEvent(ApplyFacilityFeeDocumentEvent affe, List<AbstractDocumentEvent> documentEvents) {
        // This new event will override all previous events of the same type, so we can get rid of those
        documentEvents.removeIf(e -> e instanceof ApplyFacilityFeeDocumentEvent);
        // Note: ideally, we should check if this final event changes or not the initial document and skip it if it
        // doesn't. Ex: if applyFacilityFee = true on first event, and then applyFacilityFee = false on second event and
        // the initial document didn't have facility applied, then we should remove both events (no need to keep the
        // second event applyFacilityFee = false as it finally doesn't change the initial document). However, we can't
        // do that simplification because we don't have access to the initial document here. TODO: improve this.
        return affe;
    }

    private static AbstractDocumentEvent simplifyAddRequestEvent(AddRequestEvent are, List<AbstractDocumentEvent> documentEvents) {
        // This new event will override all previous events of the same type, so we can get rid of those
        documentEvents.removeIf(e -> e instanceof AddRequestEvent);
        if (Strings.isBlank(are.getRequest())) {
            // TODO: undo the possible change made on document request
            return null;
        }
        return are;
    }

    private static boolean sameDocumentLine(DocumentLine line1, DocumentLine line2) {
        if (Entities.samePrimaryKey(line1, line2))
            return true;
        return Entities.samePrimaryKey(line1.getSite(), line2.getSite()) && Entities.samePrimaryKey(line1.getItem(), line2.getItem());
    }

}
