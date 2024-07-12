package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.DocumentLine;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractSetDocumentLineFieldsEvent extends AbstractDocumentLineEvent {

    protected final Object[] fieldIds;
    private final Object[] fieldValues;

    public AbstractSetDocumentLineFieldsEvent(Object documentPrimaryKey, Object documentLinePrimaryKey, Object[] fieldIds, Object... fieldValues) {
        super(documentPrimaryKey, documentLinePrimaryKey);
        this.fieldIds = fieldIds;
        this.fieldValues = fieldValues;
    }

    public AbstractSetDocumentLineFieldsEvent(DocumentLine documentLine, Object[] fieldIds, Object... fieldValues) {
        super(documentLine);
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
