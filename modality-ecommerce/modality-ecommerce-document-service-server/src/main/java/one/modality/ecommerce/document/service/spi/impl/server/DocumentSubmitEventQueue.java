package one.modality.ecommerce.document.service.spi.impl.server;

import dev.webfx.platform.console.Console;
import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.stack.orm.entity.result.EntityChangesBuilder;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entity.message.sender.ModalityEntityMessageSender;

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
        LocalDateTime bookingProcessStart = event.getBookingProcessStart();
        if (bookingProcessStart == null)
            bookingProcessStart = event.getOpeningDate();
        long delayMs = bookingProcessStart == null ? 0 : bookingProcessStart.toInstant(ZoneOffset.UTC).toEpochMilli() - System.currentTimeMillis();
        ready = delayMs <= 0;
        if (!ready) {
            Scheduler.scheduleDelay(delayMs, this::setReady);
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
                log("Released after processing " + processedRequestCount + " request(s)");
            }
        }
    }

    public void removedProcessedRequest(DocumentSubmitRequest processedRequest) {
        processedRequestCount++;
        removeRequest(processedRequest);
        publishProgress();
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
        log("Processed " + processedRequestCount + " request(s) over " + totalRequests + " (" + remainingRequests + " remaining)");
        // We don't publish the progress for a single request in a non-waiting queue (as it will be processed
        // immediately), and this should be actually most of the cases.
        if (ready && totalRequests == 1)
            return;
        // But for events with big opening, we do publish the progress so the front-office can animate a progress bar
        ModalityEntityMessageSender.getFrontOfficeEntityMessageSender().publishEntityChanges(
            EntityChangesBuilder.create()
                .addFieldChange(event, Event.queueProgress, processedRequestCount + "/" + totalRequests)
                .build()
        );
    }

    private void log(String message) {
        Console.log("🪣 [EVENT-QUEUE-" + event.getPrimaryKey() + "-" + event.getName() + "] " + message);
    }

}
