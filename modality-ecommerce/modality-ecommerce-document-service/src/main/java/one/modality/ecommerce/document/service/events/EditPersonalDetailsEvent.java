package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.Document;

/**
 * @author Bruno Salmon
 */
public final class EditPersonalDetailsEvent extends DocumentEvent {

    public EditPersonalDetailsEvent(Document document) {
        super(document);
    }
}
