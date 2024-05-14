package one.modality.ecommerce.document.service.events;

import java.time.LocalDateTime;

/**
 * @author Bruno Salmon
 */
public abstract class SourceEvent {

    private final LocalDateTime dateTime;

    private String comment;

    public SourceEvent() {
        this(LocalDateTime.now());
    }

    public SourceEvent(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
