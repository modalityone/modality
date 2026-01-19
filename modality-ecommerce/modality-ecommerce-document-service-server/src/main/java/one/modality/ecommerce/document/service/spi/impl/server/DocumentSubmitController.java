package one.modality.ecommerce.document.service.spi.impl.server;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.stack.com.bus.DeliveryOptions;
import dev.webfx.stack.push.server.PushServerService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesResult;

import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Salmon
 */
final class DocumentSubmitController {

    private static final Map<Object, DocumentSubmitEventQueue> eventQueues = new HashMap<>();

    public static Future<SubmitDocumentChangesResult> submitDocumentChanges(DocumentSubmitRequest info) {
        Object eventPrimaryKey = info.eventPrimaryKey();

        DocumentSubmitEventQueue eventQueue = eventQueues.get(eventPrimaryKey);
        if (eventQueue == null) {
            return createEventSubmitQueue(eventPrimaryKey)
                .compose(newQueue -> {
                    eventQueues.put(eventPrimaryKey, newQueue);
                    return executeOrEnqueueSubmitDocumentChanges(info, newQueue);
                });
        }
        return executeOrEnqueueSubmitDocumentChanges(info, eventQueue);
    }

    private static Future<SubmitDocumentChangesResult> executeOrEnqueueSubmitDocumentChanges(DocumentSubmitRequest info, DocumentSubmitEventQueue eventQueue) {
        Object queueToken = eventQueue.addRequest(info);
        return Future.succeededFuture(SubmitDocumentChangesResult.createEnqueuedResult(queueToken));
    }

    private static void processNextEnqueuedRequest(DocumentSubmitEventQueue eventQueue) {
        DocumentSubmitRequest request = eventQueue.pollRequest();
        if (request != null) {
            ServerDocumentServiceProvider.submitDocumentChangesNow(request)
                .onComplete(ar -> {
                    pushResultToClient(ar.result() != null ? ar.result() : SubmitDocumentChangesResult.createSoldOutResult(null, null), request.runId());
                    processNextEnqueuedRequest(eventQueue);
                });
        }}
    }

    private static Future<DocumentSubmitEventQueue> createEventSubmitQueue(Object eventPrimaryKey) {
        // Temporary implementation (will be read from the database in the next version)
        return Future.succeededFuture(new DocumentSubmitEventQueue(null, null));
        //return Future.succeededFuture(new EventSubmitQueue(LocalDateTime.now().plusMinutes(1)));
    }

    private static void pushResultToClient(SubmitDocumentChangesResult result, Object clientRunId) {
        if (clientRunId != null)
            PushServerService.push("/document/service/push-result", result, new DeliveryOptions(), clientRunId);
    }

}
