package one.modality.ecommerce.document.service.util;

import dev.webfx.platform.util.Arrays;
import dev.webfx.platform.util.Strings;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.util.DocumentLines;
import one.modality.ecommerce.document.service.events.AbstractAttendancesEvent;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;
import one.modality.ecommerce.document.service.events.AbstractDocumentLineEvent;
import one.modality.ecommerce.document.service.events.book.*;
import one.modality.ecommerce.document.service.events.book.EditShareOwnerInfoDocumentLineEvent;
import one.modality.ecommerce.document.service.events.registration.documentline.PriceDocumentLineEvent;
import one.modality.ecommerce.document.service.events.registration.documentline.RemoveDocumentLineEvent;

import java.util.Iterator;
import java.util.List;

/**
 * Utility class to help keep a list of document events minimized.
 *
 * @author Bruno Salmon
 */
public final class DocumentEvents {

    public static void integrateNewDocumentEvent(AbstractDocumentEvent event, List<AbstractDocumentEvent> documentEvents, List<AbstractDocumentEvent> initialDocumentEvents) {
        event = simplifyDocumentEvent(event, documentEvents, initialDocumentEvents); // May simplify the event and even return null if it's not necessary to add it
        if (event != null) {
            documentEvents.add(event);
        }
    }

    private static AbstractDocumentEvent simplifyDocumentEvent(AbstractDocumentEvent e, List<AbstractDocumentEvent> documentEvents, List<AbstractDocumentEvent> initialDocumentEvents) {
        if (e instanceof AddDocumentLineEvent event)
            return simplifyAddDocumentLineEvent(event, documentEvents);

        if (e instanceof RemoveDocumentLineEvent event)
            return simplifyRemoveDocumentLineEvent(event, documentEvents);

        if (e instanceof AddAttendancesEvent event)
            return simplifyAddAttendancesEvent(event, documentEvents);

        if (e instanceof RemoveAttendancesEvent event)
            return simplifyRemoveAttendancesEvent(event, documentEvents, initialDocumentEvents);

        if (e instanceof ApplyFacilityFeeEvent event)
            return simplifyApplyFacilityFeeDocumentEvent(event, documentEvents);

        if (e instanceof AddRequestEvent event)
            return simplifyAddRequestEvent(event, documentEvents);

        if (e instanceof PriceDocumentLineEvent event)
            return simplifyPriceDocumentLineEvent(event, documentEvents, initialDocumentEvents);

        if (e instanceof EditShareOwnerInfoDocumentLineEvent event) {

        }

        return e;
    }

    private static AbstractDocumentEvent simplifyAddDocumentLineEvent(AddDocumentLineEvent event, List<AbstractDocumentEvent> documentEvents) {
        DocumentLine documentLine = event.getDocumentLine();
        // If this line was previously removed, we can just cancel that removal instead of adding the document line again
        for (Iterator<AbstractDocumentEvent> it = documentEvents.iterator(); it.hasNext(); ) {
            AbstractDocumentEvent e = it.next();
            if (e instanceof RemoveDocumentLineEvent removeEvent && DocumentLines.sameDocumentLine(removeEvent.getDocumentLine(), documentLine)) {
                it.remove();
                event = null;
            }
        }
        return event;
    }

    private static AbstractDocumentEvent simplifyRemoveDocumentLineEvent(RemoveDocumentLineEvent event, List<AbstractDocumentEvent> documentEvents) {
        DocumentLine documentLine = event.getDocumentLine();
        // Since we remove this document line, we can simplify the changes by removing all those related to that document line
        for (Iterator<AbstractDocumentEvent> it = documentEvents.iterator(); it.hasNext(); ) {
            AbstractDocumentEvent e = it.next();
            if (e instanceof AbstractDocumentLineEvent documentLineEvent && DocumentLines.sameDocumentLine(documentLineEvent.getDocumentLine(), documentLine)) {
                it.remove();
                // In addition, if we found that this document line was added within these changes, we return null
                // to indicate that it's even not necessary to add this event to the present changes
                if (e instanceof AddDocumentLineEvent) {
                    event = null;
                }
            }
        }
        return event;
    }

    private static AbstractDocumentEvent simplifyAddAttendancesEvent(AddAttendancesEvent event, List<AbstractDocumentEvent> documentEvents) {
        return simplifyAttendancesEvent(event, documentEvents, null);
    }

    private static AbstractDocumentEvent simplifyRemoveAttendancesEvent(RemoveAttendancesEvent event, List<AbstractDocumentEvent> documentEvents, List<AbstractDocumentEvent> initialDocumentEvents) {
        return simplifyAttendancesEvent(event, documentEvents, initialDocumentEvents);
    }

    private static AbstractDocumentEvent simplifyAttendancesEvent(AbstractAttendancesEvent event, List<AbstractDocumentEvent> documentEvents, List<AbstractDocumentEvent> initialDocumentEvents) {
        Attendance[] initialAttendances = event.getAttendances(); // removed or added attendances
        Attendance[] reducedAttendances = initialAttendances; // reduced
        DocumentLine documentLine = event.getDocumentLine();


        //CLAUDE ANALYSIS ON THE BUG THAT REMOVED SONETINE SOME ATTENDANCE:
        /*  Root Cause Analysis
        The bug was in DocumentEvents.simplifyAttendancesEvent() in DocumentEvents.java:102-173.

        The Problem:
        When rebooking transport options, the flow is:

        unbookScheduledItems() creates a RemoveAttendancesEvent for existing shuttle attendances
        bookScheduledItems() creates new AddAttendancesEvent for each selected shuttle
        When the new AddAttendancesEvent([A2']) for the RETURN shuttle was integrated:

        The code found an OLD AddAttendancesEvent([A2]) with the same DocumentLine
        It excluded A2' because it had the same ScheduledItem primary key as A2
        Bug: It didn't account for the fact that A2 was already marked for removal by a RemoveAttendancesEvent
        The new attendance was incorrectly excluded, resulting in the RETURN shuttle having no attendance at summary time
        The Fix:
        Added pre-collection of removed ScheduledItem primary keys before the simplification loop. When processing AddAttendancesEvent entries with the same DocumentLine, the code now filters out attendances that have been removed by RemoveAttendancesEvent entries, so they don't cause false exclusions.

        Key changes:

        Before the loop, collect all ScheduledItem PKs from RemoveAttendancesEvents for the same DocumentLine
        When comparing against existing AddAttendancesEvents, filter out attendances whose ScheduledItems are in the removed set
        Only exclude new attendances based on "effective" (not-removed) existing attendances
        This ensures that when you unbook and rebook a shuttle, the new attendance is correctly added rather than being incorrectly excluded as a "duplicate" of an attendance that was actually removed.
   */


                // When adding attendances, we need to know which ScheduledItems have been removed by RemoveAttendancesEvents
        // so we don't incorrectly exclude new attendances that are re-adding previously removed ones.
        // We pre-collect removed ScheduledItem primary keys to avoid exclusion based on stale AddAttendancesEvents.
        java.util.Set<Object> removedScheduledItemPks = new java.util.HashSet<>();
        if (event instanceof AddAttendancesEvent) {
            for (AbstractDocumentEvent e : documentEvents) {
                if (e instanceof RemoveAttendancesEvent rae && DocumentLines.sameDocumentLine(rae.getDocumentLine(), documentLine)) {
                    for (Attendance a : rae.getAttendances()) {
                        if (a.getScheduledItem() != null) {
                            removedScheduledItemPks.add(Entities.getPrimaryKey(a.getScheduledItem()));
                        }
                    }
                }
            }
        }

        // Since this event may undo previous events, we simplify by removing the undone events (partially or totally)
        for (int i = 0; i < documentEvents.size() && reducedAttendances.length > 0; i++) {
            AbstractDocumentEvent e = documentEvents.get(i);
            // Only attendance events operating on the same document line can be simplified
            if (e instanceof AbstractAttendancesEvent aae && DocumentLines.sameDocumentLine(aae.getDocumentLine(), documentLine)) { // AddAttendancesEvent or RemoveAttendancesEvent
                Attendance[] eventAttendances = aae.getAttendances();
                // If the existing event is not of the same class, it is exclusive with the new event,
                if (event.getClass() != aae.getClass()) { // ex: RemoveAttendance and AddAttendance
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
                // However, when adding attendances, don't exclude based on AddAttendancesEvent entries whose
                // attendances have been subsequently removed by a RemoveAttendancesEvent.
                if (event instanceof AddAttendancesEvent && aae instanceof AddAttendancesEvent && !removedScheduledItemPks.isEmpty()) {
                    // Filter eventAttendances to only include those not removed
                    Attendance[] effectiveEventAttendances = Arrays.filter(eventAttendances,
                        ea -> ea.getScheduledItem() == null || !removedScheduledItemPks.contains(Entities.getPrimaryKey(ea.getScheduledItem())),
                        Attendance[]::new);
                    reducedAttendances = excludeAttendances(reducedAttendances, effectiveEventAttendances);
                } else {
                    reducedAttendances = excludeAttendances(reducedAttendances, eventAttendances);
                }
                // TODO: if there are still attendances on both joining events, we should merge them into 1 single event
            }
        }
        if (event instanceof RemoveAttendancesEvent) {
            // We also remove the associated document line if there are no attendances anymore to it
            if (hasNoRemainingAddedAttendancesOnDocumentLineOtherThan(initialDocumentEvents, documentLine, reducedAttendances)
                && hasNoRemainingAddedAttendancesOnDocumentLineOtherThan(documentEvents, documentLine, reducedAttendances))
                integrateNewDocumentEvent(new RemoveDocumentLineEvent(documentLine), documentEvents, initialDocumentEvents);
        }
        // If at the end of this simplification, there is no more attendances to keep in RemoveAttendancesEvent
        if (reducedAttendances.length == 0) {
            return null; // we return null to indicate that it's not necessary anymore to add it in the document changes
        }
        // If some were removed but not all, we simplify this event with only the necessary attendances
        if (reducedAttendances.length < initialAttendances.length) {
            event = cloneWithNewAttendances(event, reducedAttendances);
        }
        return event;
    }

    private static boolean hasNoRemainingAddedAttendancesOnDocumentLineOtherThan(List<AbstractDocumentEvent> initialDocumentEvents, DocumentLine documentLine, Attendance[] attendances) {
        return Collections.findFirst(initialDocumentEvents, e ->
            e instanceof AddAttendancesEvent aae
            && aae.getDocumentLine() == documentLine
            && Arrays.findFirst(aae.getAttendances(), a -> !Arrays.contains(attendances, a)) != null
        ) == null;
    }

    private static Attendance[] excludeAttendances(Attendance[] attendances, Attendance[] toExclude) {
        return Arrays.filter(attendances, a -> Arrays.findFirst(toExclude, ea -> Entities.samePrimaryKey(ea.getScheduledItem(), a.getScheduledItem())) == null, Attendance[]::new);
    }

    private static AbstractAttendancesEvent cloneWithNewAttendances(AbstractAttendancesEvent event, Attendance[] attendances) {
        if (event instanceof AddAttendancesEvent)
            return new AddAttendancesEvent(attendances);

        if (event instanceof RemoveAttendancesEvent)
            return new RemoveAttendancesEvent(attendances);

        throw new IllegalArgumentException("Unknown AttendancesEvent type: " + event);
    }

    private static AbstractDocumentEvent simplifyApplyFacilityFeeDocumentEvent(ApplyFacilityFeeEvent event, List<AbstractDocumentEvent> documentEvents) {
        // This new event will override all previous events of the same type, so we can get rid of those
        documentEvents.removeIf(e -> e instanceof ApplyFacilityFeeEvent);
        // Note: ideally, we should check if this final event changes or not the initial document and skip it if it
        // doesn't. Ex: if applyFacilityFee = true on first event, and then applyFacilityFee = false on second event and
        // the initial document didn't have facility applied, then we should remove both events (no need to keep the
        // second event applyFacilityFee = false as it finally doesn't change the initial document). However, we can't
        // do that simplification because we don't have access to the initial document here. TODO: improve this.
        return event;
    }

    private static AbstractDocumentEvent simplifyAddRequestEvent(AddRequestEvent event, List<AbstractDocumentEvent> documentEvents) {
        // This new event will override all previous events of the same type, so we can get rid of those
        documentEvents.removeIf(e -> e instanceof AddRequestEvent);
        if (Strings.isBlank(event.getRequest())) {
            // TODO: undo the possible change made on document request
            return null;
        }
        return event;
    }

    private static AbstractDocumentEvent simplifyPriceDocumentLineEvent(PriceDocumentLineEvent event, List<AbstractDocumentEvent> documentEvents, List<AbstractDocumentEvent> initialDocumentEvents) {
        DocumentLine documentLine = event.getDocumentLine();
        // We check if there is already a price event in the current changes for this document line (probably unique, but we call merge just in case)
        PriceDocumentLineEvent currentEvent = PriceDocumentLineEvent.mergeDocumentLinePriceEvents(documentEvents, documentLine, true);
        // If we got one (and it was actually removed from the current changes), we merge them
        event = PriceDocumentLineEvent.mergeDocumentLinePriceEvents(currentEvent, event); // does nothing if currentEvent == null
        // Now we have the final event ready to be added to the changes. However, there are some cases where we can simply remove that event.
        // Case 1: there are some price events already saved, but the event makes no difference when applied to them. Ex: Applying the same discount again
        PriceDocumentLineEvent savedEvent = PriceDocumentLineEvent.mergeDocumentLinePriceEvents(initialDocumentEvents, documentLine, false);
        PriceDocumentLineEvent futureEvent = PriceDocumentLineEvent.mergeDocumentLinePriceEvents(savedEvent, event);
        if (futureEvent == savedEvent)
            return null;
        // Case 2: it's a 0% discount (i.e., discount removal), but there was no discount before, so it makes no difference
        if (PriceDocumentLineEvent.sameFieldValues(event, null, null, null, 0) && (savedEvent == null || savedEvent.getPrice_discount() == null))
            return null;
        return event;
    }

}
