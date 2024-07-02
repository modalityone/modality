package one.modality.ecommerce.document.service.events;

import dev.webfx.stack.orm.entity.EntityStore;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractSourceEvent {

    //private LocalDateTime dateTime;
    //private String comment;

    protected EntityStore entityStore;

    public AbstractSourceEvent() {
        //this(LocalDateTime.now());
    }

    /*public AbstractSourceEvent(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
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
    }*/

    public void setEntityStore(EntityStore entityStore) {
        this.entityStore = entityStore;
    }
}
