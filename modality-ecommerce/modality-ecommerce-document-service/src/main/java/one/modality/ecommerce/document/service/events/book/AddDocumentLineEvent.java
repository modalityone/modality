package one.modality.ecommerce.document.service.events.book;

import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Site;
import one.modality.ecommerce.document.service.events.AbstractDocumentLineEvent;

/**
 * @author Bruno Salmon
 */
public final class AddDocumentLineEvent extends AbstractDocumentLineEvent {

    private final Object sitePrimaryKey;
    private final Object itemPrimaryKey;

    public AddDocumentLineEvent(DocumentLine documentLine) {
        super(documentLine);
        sitePrimaryKey = Entities.getPrimaryKey(documentLine.getSite());
        itemPrimaryKey = Entities.getPrimaryKey(documentLine.getItem());
    }

    public AddDocumentLineEvent(Object documentPrimaryKey, Object documentLinePrimaryKey, Object itemPrimaryKey, Object sitePrimaryKey) {
        super(documentPrimaryKey, documentLinePrimaryKey);
        this.itemPrimaryKey = itemPrimaryKey;
        this.sitePrimaryKey = sitePrimaryKey;
    }

    public Object getItemPrimaryKey() {
        return itemPrimaryKey;
    }

    public Object getSitePrimaryKey() {
        return sitePrimaryKey;
    }

    @Override
    public DocumentLine getDocumentLine() {
        if (documentLine == null && entityStore != null) {
            documentLine = entityStore.createEntity(DocumentLine.class, getDocumentLinePrimaryKey());
            documentLine.setDocument(getDocument());
            documentLine.setSite(entityStore.getEntity(Site.class, sitePrimaryKey)); // Should be found from PolicyAggregate
            documentLine.setItem(entityStore.getEntity(Item.class, itemPrimaryKey)); // Should be found from PolicyAggregate
            return documentLine;
        }
        return super.getDocumentLine();
    }
}
