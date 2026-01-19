package one.modality.ecommerce.document.service.spi.impl.server;

import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.platform.util.uuid.Uuid;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Salmon
 */
final class DocumentSubmitEventQueue {

    private final LocalDateTime queueStartExecutionDateTime;
    private final Runnable onReadyListener;
    private final List<DocumentSubmitRequest> queue = new ArrayList<>();

    public DocumentSubmitEventQueue(LocalDateTime queueStartExecutionDateTime, Runnable onReadyListener) {
        this.queueStartExecutionDateTime = queueStartExecutionDateTime;
        this.onReadyListener = onReadyListener;
        long delayMs = queueStartExecutionDateTime == null ? 0 : queueStartExecutionDateTime.toInstant(ZoneOffset.UTC).toEpochMilli() - System.currentTimeMillis();
        if (delayMs > 0) {
            Scheduler.scheduleDelay(delayMs, this::fireOnReadyIfApplicable);
        }
    }

    public LocalDateTime getQueueStartExecutionDateTime() {
        return queueStartExecutionDateTime;
    }

    public Object addRequest(DocumentSubmitRequest request) {
        queue.add(request);
        fireOnReadyIfApplicable();
        return Uuid.randomUuid();
    }

    private void fireOnReadyIfApplicable() {
        if (!queue.isEmpty() && onReadyListener != null)
            onReadyListener.run();
    }

    public DocumentSubmitRequest pollRequest() {
        if (queue.isEmpty())
            return null;
        return queue.remove(0);
    }

}
