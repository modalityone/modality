package one.modality.ecommerce.document.service.events;

import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractSourceEvent {

    //private LocalDateTime dateTime;
    //private String comment;

    protected EntityStore entityStore;
    protected UpdateStore updateStore;

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
        updateStore = entityStore instanceof UpdateStore ? (UpdateStore) entityStore : null;
    }

    protected boolean isForSubmit() {
        return updateStore != null;
    }

    public abstract void replayEvent();

    public abstract void setPlayed(boolean played);

    public abstract boolean isPlayed();

}
