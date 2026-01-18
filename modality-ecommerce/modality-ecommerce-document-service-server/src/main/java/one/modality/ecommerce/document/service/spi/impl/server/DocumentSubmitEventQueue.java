package one.modality.ecommerce.document.service.spi.impl.server;

import dev.webfx.platform.scheduler.Scheduled;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Salmon
 */
final class DocumentSubmitEventQueue {

    private final LocalDateTime queueStartExecutionDateTime;
    private final List<DocumentSubmitRequest> queue = new ArrayList<>();
    private Scheduled scheduled;

    public DocumentSubmitEventQueue(LocalDateTime queueStartExecutionDateTime) {
        this.queueStartExecutionDateTime = queueStartExecutionDateTime;
    }

    public LocalDateTime getQueueStartExecutionDateTime() {
        return queueStartExecutionDateTime;
    }

    public void addRequest(DocumentSubmitRequest request) {
        queue.add(request);
    }

    public DocumentSubmitRequest pollRequest() {
        if (queue.isEmpty())
            return null;
        return queue.remove(0);
    }

    public Scheduled getScheduled() {
        return scheduled;
    }

    public void setScheduled(Scheduled scheduled) {
        this.scheduled = scheduled;
    }
}
