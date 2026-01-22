package one.modality.ecommerce.document.service.spi.impl.server;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import one.modality.base.shared.entities.Event;
import one.modality.ecommerce.document.service.SubmitDocumentChangesResult;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Salmon
 */
final class DocumentSubmitController {

    private static final Map<Object, DocumentSubmitEventQueue> eventQueues = new HashMap<>();
    private static final Map<Object, Future<DocumentSubmitEventQueue>> eventQueueCreationFutures = new HashMap<>();
    private static final Map<Object, SubmitDocumentChangesResult> pushingResults = new HashMap<>();

    public static Future<SubmitDocumentChangesResult> submitDocumentChanges(DocumentSubmitRequest request) {
        Object eventPrimaryKey = request.eventPrimaryKey();

        // Getting the event queue if already exists
        DocumentSubmitEventQueue eventQueue = eventQueues.get(eventPrimaryKey);
        if (eventQueue != null) // If the event queue already exists, we can proceed with the request right now
            return executeOrEnqueueSubmitDocumentChanges(request, eventQueue);

        // Otherwise, we first need to create the event queue (or wait if it's already being created)
        Future<DocumentSubmitEventQueue> eventQueueCreationFuture = eventQueueCreationFutures.get(eventPrimaryKey);
        if (eventQueueCreationFuture == null) {
            eventQueueCreationFuture = createEventSubmitQueue(request)
                .onComplete(ar -> eventQueueCreationFutures.remove(eventPrimaryKey));
            eventQueueCreationFutures.put(eventPrimaryKey, eventQueueCreationFuture);
        }
        return eventQueueCreationFuture
            .compose(newQueue -> {
                eventQueues.put(eventPrimaryKey, newQueue);
                // and then proceed with the request
                return executeOrEnqueueSubmitDocumentChanges(request, newQueue);
            });
    }

    private static Future<DocumentSubmitEventQueue> createEventSubmitQueue(DocumentSubmitRequest request) {
        return request.updateStore().getOrCreateEntity(Event.class, request.eventPrimaryKey()).<Event>onExpressionLoaded("name,openingDate,bookingProcessStart")
            .map(DocumentSubmitEventQueue::new);
    }

    private static Future<SubmitDocumentChangesResult> executeOrEnqueueSubmitDocumentChanges(DocumentSubmitRequest request, DocumentSubmitEventQueue eventQueue) {
        // Even if we execute the request immediately, we still need to add it to the queue
        boolean isTheOnlyRequest = eventQueue.isEmpty();
        eventQueue.addRequest(request);

        // We execute the request immediately if it's the only request and the queue is ready (not waiting an opening time) and not processing
        if (isTheOnlyRequest && eventQueue.isReady() && !eventQueue.isProcessing()) {
            eventQueue.setProcessingRequest(request); // We inform the queue we process the request now
            return processRequest(request, eventQueue); // We process it (this will also remove it from the queue and eventually process the next one)
        }

        // Otherwise we return the enqueued result with the queue token, and the request will be processed later
        return Future.succeededFuture(SubmitDocumentChangesResult.createEnqueuedResult(request.queueToken()));
    }

    private static Future<SubmitDocumentChangesResult> processRequest(DocumentSubmitRequest request, DocumentSubmitEventQueue eventQueue) {
        return ServerDocumentServiceProvider.submitDocumentChangesNow(request)
            // And once processed, we inform the queue the request can be removed
            .onComplete(ar -> eventQueue.removedProcessedRequest(request));
    }

    static void processRequestAndNotifyClient(DocumentSubmitRequest request, DocumentSubmitEventQueue eventQueue) {
        processRequest(request, eventQueue)
            .onComplete(ar -> {
                SubmitDocumentChangesResult result = ar.succeeded() ? ar.result() :
                    // Temporary SoldOut result when an exception is raised (ex: double booking)
                    SubmitDocumentChangesResult.createSoldOutResult(null, null);
                notifyClient(request, result);
            });
    }

    static void notifyClient(DocumentSubmitRequest request, SubmitDocumentChangesResult result) {
        pushingResults.put(request.queueToken(), result);
        DocumentSubmitEventQueue.pushResultToClient(
            SubmitDocumentChangesResult.withQueueToken(result, request.queueToken()),
            request.runId()
        ).onComplete(ar -> {
            if (ar.succeeded()) {
                Console.log("✅ Successfully pushed token " + request.queueToken() + " to client " + request.runId());
                pushingResults.remove(request.queueToken());
            } else {
                Console.log("❌ Failed pushing token " + request.queueToken() + " to client " + request.runId());
                if (pushingResults.containsKey(request.queueToken())) {
                    Console.log("Retrying push of token " + request.queueToken() + " to client " + request.runId());
                    notifyClient(request, result);
                }
            }
        });
    }

    static void releaseEventQueue(DocumentSubmitEventQueue eventQueue) {
        eventQueues.remove(eventQueue.getEventPrimaryKey());
    }

    static boolean leaveEventQueue(Object queueToken) {
        for (DocumentSubmitEventQueue eventQueue : eventQueues.values()) {
            if (eventQueue.releaseEventQueue(queueToken)) {
                return true;
            }
        }
        return false;
    }

    static SubmitDocumentChangesResult fetchEventQueueResult(Object queueToken) {
        return pushingResults.remove(queueToken);
    }
}