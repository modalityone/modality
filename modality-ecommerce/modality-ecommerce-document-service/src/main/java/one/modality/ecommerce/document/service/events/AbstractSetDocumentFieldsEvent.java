package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.Document;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractSetDocumentFieldsEvent extends AbstractDocumentEvent {

    protected final Object[] fieldIds;
    private final Object[] fieldValues;

    public AbstractSetDocumentFieldsEvent(Object documentPrimaryKey, Object[] fieldIds, Object... fieldValues) {
        super(documentPrimaryKey);
        this.fieldIds = fieldIds;
        this.fieldValues = fieldValues;
    }

    public AbstractSetDocumentFieldsEvent(Document document, Object[] fieldIds, Object... fieldValues) {
        super(document);
        this.fieldIds = fieldIds;
        this.fieldValues = fieldValues;
    }

    public Object[] getFieldIds() {
        return fieldIds;
    }

    public Object[] getFieldValues() {
        return fieldValues;
    }
}
