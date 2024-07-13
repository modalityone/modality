package one.modality.ecommerce.document.service.util;

import dev.webfx.platform.util.Arrays;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.ecommerce.document.service.events.*;
import one.modality.ecommerce.document.service.events.book.AddAttendancesEvent;
import one.modality.ecommerce.document.service.events.book.AddDocumentLineEvent;
import one.modality.ecommerce.document.service.events.book.RemoveAttendancesEvent;
import one.modality.ecommerce.document.service.events.registration.documentline.RemoveDocumentLineEvent;

import java.util.Iterator;
import java.util.List;

/**
 * Utility class to help keeping a list of document events minimised.
 * 
 * @author Bruno Salmon
 */
public final class DocumentEvents {

    public static void integrateNewDocumentEvent(AbstractDocumentEvent e, List<AbstractDocumentEvent> documentEvents) {
        e = simplifyDocumentEvent(e, documentEvents); // May simplify the event, and even return null if it's not necessary to add it
        if (e != null) {
            documentEvents.add(e);
        }
    }

    private static AbstractDocumentEvent simplifyDocumentEvent(AbstractDocumentEvent e, List<AbstractDocumentEvent> documentEvents) {
        if (e instanceof RemoveDocumentLineEvent) {
            return simplifyRemoveDocumentLineEvent((RemoveDocumentLineEvent) e, documentEvents);
        }
        if (e instanceof RemoveAttendancesEvent) {
            return simplifyAttendancesRemoval((RemoveAttendancesEvent) e, documentEvents);
        }
        return e;
    }

    private static AbstractDocumentEvent simplifyRemoveDocumentLineEvent(RemoveDocumentLineEvent rdle, List<AbstractDocumentEvent> documentEvents) {
        DocumentLine documentLine = rdle.getDocumentLine();
        // Since we remove this document line, we can simplify the changes by removing all those related to that document line
        for (Iterator<AbstractDocumentEvent> it = documentEvents.iterator(); it.hasNext(); ) {
            AbstractDocumentEvent e = it.next();
            if (e instanceof AbstractDocumentLineEvent) {
                AbstractDocumentLineEvent adle = (AbstractDocumentLineEvent) e;
                if (adle.getDocumentLine() == documentLine) {
                    it.remove();
                    // In addition, if we found that this document line was added within these changes, we return null
                    // to indicate that it's even not necessary to add this event to the present changes
                    if (e instanceof AddDocumentLineEvent) {
                        rdle = null;
                    }
                }
            }
        }
        return rdle;
    }

    private static AbstractDocumentEvent simplifyAttendancesRemoval(RemoveAttendancesEvent rae, List<AbstractDocumentEvent> documentEvents) {
        Attendance[] removedAttendances = rae.getAttendances();
        Attendance[] reducedRemovedAttendances = removedAttendances;
        // Since we remove these attendances, we can simplify the changes by removing them from other attendances events
        for (int i = 0; i < documentEvents.size(); i++) {
            AbstractDocumentEvent e = documentEvents.get(i);
            if (e instanceof AbstractAttendancesEvent) { // AddAttendancesEvent or RemoveAttendancesEvent
                AbstractAttendancesEvent aae = (AbstractAttendancesEvent) e;
                Attendance[] eventAttendances = aae.getAttendances();
                // Looking for the remaining attendances for this event (those not covered by removedAttendances)
                Attendance[] remainingAttendances = Arrays.filter(eventAttendances, a -> !Arrays.contains(removedAttendances, a), Attendance[]::new);
                if (remainingAttendances.length == 0) { // If all attendances disappeared, we can simply remove this event
                    documentEvents.remove(i--);
                } else if (remainingAttendances.length < eventAttendances.length) { // if some disappeared but not all
                    // we replace this event with the same event but covering only the remaining attendances
                    if (aae instanceof AddAttendancesEvent) {
                        aae = new AddAttendancesEvent(remainingAttendances);
                    } else if (aae instanceof RemoveAttendancesEvent) {
                        aae = new RemoveAttendancesEvent(remainingAttendances);
                    }
                    documentEvents.set(i, aae);
                }
                // In both cases, if the event was AddAttendancesEvent, because we removed some attendances from start
                // at this point, there is no need anymore to keep them anymore in the passed removeAttendancesEvent
                if (remainingAttendances.length < eventAttendances.length && aae instanceof AddAttendancesEvent) {
                    reducedRemovedAttendances = Arrays.filter(reducedRemovedAttendances, a -> !Arrays.contains(eventAttendances, a), Attendance[]::new);
                }
            }
        }
        // If at the end of this simplification there is no more attendances to keep in the passed RemoveAttendancesEvent
        if (reducedRemovedAttendances.length == 0) {
            return null; // we return null to indicate that it's not necessary anymore to add it in the document changes
        }
        // If some were removed but not all, we simplify this event with only the necessary attendances
        if (reducedRemovedAttendances != removedAttendances) {
            rae = new RemoveAttendancesEvent(reducedRemovedAttendances);
        }
        return rae;
    }
    
}
