package one.modality.ecommerce.document.service.spi.impl.server;

import dev.webfx.platform.console.Console;
import dev.webfx.platform.scheduler.Scheduler;
import one.modality.base.shared.entities.Event;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Bruno Salmon
 */
final class DocumentSubmitEventQueue {

    private final Event event; // Keeping reference for debugging purpose
    private boolean ready;
    private DocumentSubmitRequest processingRequest;
    private final Map<Object, DocumentSubmitRequest> queue = new LinkedHashMap<>();
    private int processedRequestCount;

    public DocumentSubmitEventQueue(Event event) {
        this.event = event;
        LocalDateTime bookingProcessStart = event.getLocalDateTimeFieldValue("bookingProcessStart");
        if (bookingProcessStart == null)
            bookingProcessStart = event.getOpeningDate();
        long delayMs = bookingProcessStart == null ? 0 : bookingProcessStart.toInstant(ZoneOffset.UTC).toEpochMilli() - System.currentTimeMillis();
        ready = delayMs <= 0;
        if (!ready) {
            Scheduler.scheduleDelay(delayMs, this::setReady);
        }
        Console.log("Created queue for event " + event.getPrimaryKey() + " (" + event.getName() + ")");
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
        DocumentSubmitRequest request = queue.values().iterator().next();
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
                DocumentSubmitController.processRequestAndNotifyClient(nextRequest, this);
            } else {
                DocumentSubmitController.releaseEventQueue(this);
                Console.log("Released queue for event " + event.getPrimaryKey() + " (" + event.getName() + ")");
            }
        }
    }

    public void removedProcessedRequest(DocumentSubmitRequest processedRequest) {
        processedRequestCount++;
        publishProgress();
        removeRequest(processedRequest);
        if (this.processingRequest == processedRequest) {
            this.processingRequest = null;
            processNextRequestIfReadyAndNotProcessing();
        }
    }

    void removeRequest(DocumentSubmitRequest request) {
        removeRequest(request.queueToken());
    }

    void removeRequest(Object token) {
        queue.remove(token);
    }

    void publishProgress() {
        int remainingRequests = queue.size();
        int totalRequests = processedRequestCount + remainingRequests;
        Console.log("Queue for event " + event.getPrimaryKey() + " (" + event.getName() + ") has processed " + processedRequestCount + " requests over " + totalRequests + " (" + remainingRequests + " remaining)");
    }

}
