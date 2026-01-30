package one.modality.ecommerce.document.service.events.book;

import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class EditCarersInfoEvent extends AbstractDocumentEvent {

    private final String carer1Name;
    private final Object carer1DocumentPrimaryKey;
    private final String carer2Name;
    private final Object carer2DocumentPrimaryKey;

    public EditCarersInfoEvent(Object documentPrimaryKey, String carer1Name, Object carer1DocumentPrimaryKey, String carer2Name, Object carer2DocumentPrimaryKey) {
        super(documentPrimaryKey);
        this.carer1Name = carer1Name;
        this.carer1DocumentPrimaryKey = carer1DocumentPrimaryKey;
        this.carer2Name = carer2Name;
        this.carer2DocumentPrimaryKey = carer2DocumentPrimaryKey;
    }

    public EditCarersInfoEvent(Document document, String carer1Name, Document carer1Document, String carer2Name, Document carer2Document) {
        super(document);
        this.carer1Name = carer1Name;
        this.carer1DocumentPrimaryKey = Entities.getPrimaryKey(carer1Document);
        this.carer2Name = carer2Name;
        this.carer2DocumentPrimaryKey = Entities.getPrimaryKey(carer2Document);
    }

    public String getCarer1Name() {
        return carer1Name;
    }

    public Object getCarer1DocumentPrimaryKey() {
        return carer1DocumentPrimaryKey;
    }
    public String getCarer2Name() {
        return carer2Name;
    }
    public Object getCarer2DocumentPrimaryKey() {
        return carer2DocumentPrimaryKey;
    }

    @Override
    public void replayEventOnDocument() {
        super.replayEventOnDocument();
        document.setCarer1Name(carer1Name);
        document.setCarer1Document(carer1DocumentPrimaryKey);
        document.setCarer2Name(carer2Name);
        document.setCarer2Document(carer2DocumentPrimaryKey);
    }
}
