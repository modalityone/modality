package one.modality.ecommerce.document.service.events.book;

import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class AddRequestEvent extends AbstractDocumentEvent {

    private String request;

    public AddRequestEvent(Document document, String request) {
        super(document);
        this.request = request;
    }

    public AddRequestEvent(Object documentPrimaryKey, String request) {
        super(documentPrimaryKey);
        this.request = request;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    @Override
    public void replayEventOnDocument() {
        super.replayEventOnDocument();
        // TODO: Investigate if it should add, not just set the request.
        //  Also is it not enough to integrate it in history, knowing that the database has a trigger that resets the
        //  document request from all history request?
        document.setRequest(request);
    }
}
