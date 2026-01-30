package one.modality.ecommerce.document.service.spi.impl.server;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.scheduler.Scheduled;
import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.stack.com.bus.DeliveryOptions;
import dev.webfx.stack.orm.entity.result.EntityChangesBuilder;
import dev.webfx.stack.push.server.PushServerService;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entity.message.sender.ModalityEntityMessageSender;
import one.modality.ecommerce.document.service.SubmitDocumentChangesResult;
import one.modality.ecommerce.document.service.buscall.DocumentServiceBusAddresses;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Bruno Salmon
 */
final class DocumentSubmitEventQueue {

    private final Event event; // Keeping reference for debugging purpose
    private boolean ready;
    private Scheduled scheduled;
    private DocumentSubmitRequest processingRequest;
    private final Map<Object, DocumentSubmitRequest> queue = new HashMap<>();
    private final Random random = new Random();
    private int processedRequests;

    public DocumentSubmitEventQueue(Event event) {
        this.event = event;
        LocalDateTime bookingProcessStart = event.getBookingProcessStart();
        if (bookingProcessStart == null)
            bookingProcessStart = event.getOpeningDate();
        long delayMs = bookingProcessStart == null ? 0 : bookingProcessStart.toInstant(ZoneOffset.UTC).toEpochMilli() - System.currentTimeMillis();
        ready = delayMs <= 0;
        if (!ready) {
            scheduled = Scheduler.scheduleDelay(delayMs, this::setReady);
        }
        log("Created - Start delay: " + delayMs + "ms");
    }

    Object getEventPrimaryKey() {
        return event.getPrimaryKey();
    }

    boolean isEmpty() {
        return queue.isEmpty();
    }

    boolean isReady() {
        return ready;
    }

    private void setReady() {
        ready = true;
        processNextRequestIfReadyAndNotProcessing();
    }

    void addRequest(DocumentSubmitRequest request) {
        queue.put(request.queueToken(), request);
        publishProgress();
    }

    void setProcessingRequest(DocumentSubmitRequest processingRequest) {
        this.processingRequest = processingRequest;
    }

    DocumentSubmitRequest pollProcessingRequest() {
        if (queue.isEmpty())
            return null;
        int index = random.nextInt(queue.size());
        DocumentSubmitRequest request = queue.values().stream().skip(index).findFirst().orElse(null);
        setProcessingRequest(request);
        return request;
    }

    boolean isProcessing() {
        return processingRequest != null;
    }

    private void processNextRequestIfReadyAndNotProcessing() {
        if (isReady() && !isProcessing()) {
            DocumentSubmitRequest nextRequest = pollProcessingRequest();
            if (nextRequest != null) {
                DocumentSubmitController.processRequest(nextRequest, this, true);
            } else {
                DocumentSubmitController.releaseEventQueue(this);
                log("Released after processing " + processedRequests + " request(s)");
            }
        }
    }

    public void removedProcessedRequest(DocumentSubmitRequest request, SubmitDocumentChangesResult result) {
        processedRequests++;
        removeRequest(request);
        publishProgressAndResult(request, result);
        if (processingRequest == request) {
            processingRequest = null;
            processNextRequestIfReadyAndNotProcessing();
        }
    }

    void removeRequest(DocumentSubmitRequest request) {
        removeRequest(request.queueToken());
    }

    DocumentSubmitRequest removeRequest(Object token) {
        return queue.remove(token);
    }

    void publishProgress() {
        publishProgressAndResult(null, null);
    }

    void publishProgressAndResult(DocumentSubmitRequest request, SubmitDocumentChangesResult result) {
        int remainingRequests = queue.size();
        int totalRequests = processedRequests + remainingRequests;
        log("Processed " + processedRequests + " request(s) over " + totalRequests + " (" + remainingRequests + " remaining)");
        // We don't publish the progress for a single request in a non-waiting queue (as it will be processed
        // immediately), and this should be actually most of the cases.
        if (scheduled == null && totalRequests == 1)
            return;
        // But for events with big opening, we do publish the progress so the front-office can animate a progress bar
        log("Notifying front-office of progress");
        ModalityEntityMessageSender.getFrontOfficeEntityMessageSender().publishEntityChanges(
            EntityChangesBuilder.create()
                .addFieldChange(event, Event.queueProgress, processedRequests + "/" + totalRequests)
                .build()
        );
        if (request != null && result != null)
            DocumentSubmitController.notifyClient(request, result, 30);
    }

    static Future<Object> pushResultToClient(SubmitDocumentChangesResult result, Object clientRunId) {
        if (clientRunId == null)
            return Future.succeededFuture("UNKNOWN");
        return PushServerService.push(
            DocumentServiceBusAddresses.SUBMIT_DOCUMENT_CHANGES_FINAL_CLIENT_PUSH_ADDRESS,
            result,
            new DeliveryOptions(),
            clientRunId);
    }

    private void log(String message) {
        Console.log("🪣 [EVENT-QUEUE-" + event.getPrimaryKey() + "-" + event.getName() + "] " + message);
    }

    boolean releaseEventQueue(Object queueToken) {
        if (removeRequest(queueToken) != null) {
            publishProgress();
            return true;
        }
        return false;
    }

}
