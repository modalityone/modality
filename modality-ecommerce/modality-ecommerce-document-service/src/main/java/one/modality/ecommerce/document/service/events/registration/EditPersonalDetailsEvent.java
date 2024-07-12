package one.modality.ecommerce.document.service.events.registration;

import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class EditPersonalDetailsEvent extends AbstractDocumentEvent {

    public EditPersonalDetailsEvent(Document document) {
        super(document);
    }
}
