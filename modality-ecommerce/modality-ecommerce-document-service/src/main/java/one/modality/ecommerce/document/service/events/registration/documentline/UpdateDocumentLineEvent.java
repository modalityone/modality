package one.modality.ecommerce.document.service.events.registration.documentline;

import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.ecommerce.document.service.events.AbstractDocumentLineEvent;

/**
 * Event for updating document line properties such as item (accommodation type)
 * and resource configuration (room assignment).
 *
 * This event is used when editing an existing booking option to change:
 * - The accommodation type (Item)
 * - The room assignment (ResourceConfiguration)
 *
 * @author Claude Code
 */
public final class UpdateDocumentLineEvent extends AbstractDocumentLineEvent {

    private final Object itemPrimaryKey;
    private final Object resourceConfigurationPrimaryKey;

    /**
     * Constructor for server-side deserialization.
     */
    public UpdateDocumentLineEvent(Object documentPrimaryKey, Object documentLinePrimaryKey,
                                   Object itemPrimaryKey, Object resourceConfigurationPrimaryKey) {
        super(documentPrimaryKey, documentLinePrimaryKey);
        this.itemPrimaryKey = itemPrimaryKey;
        this.resourceConfigurationPrimaryKey = resourceConfigurationPrimaryKey;
    }

    /**
     * Constructor for client-side creation.
     */
    public UpdateDocumentLineEvent(DocumentLine documentLine, Item newItem, ResourceConfiguration newResourceConfiguration) {
        super(documentLine);
        this.itemPrimaryKey = newItem != null ? Entities.getPrimaryKey(newItem) : null;
        this.resourceConfigurationPrimaryKey = newResourceConfiguration != null
            ? Entities.getPrimaryKey(newResourceConfiguration) : null;
    }

    public Object getItemPrimaryKey() {
        return itemPrimaryKey;
    }

    public Object getResourceConfigurationPrimaryKey() {
        return resourceConfigurationPrimaryKey;
    }

    @Override
    public void replayEventOnDocumentLine() {
        super.replayEventOnDocumentLine();
        if (itemPrimaryKey != null) {
            if (isForSubmit()) {
                documentLine.setItem(itemPrimaryKey);
            } else {
                Item item = entityStore.getEntity(Item.class, itemPrimaryKey, true);
                documentLine.setItem(item);
            }
        }
        if (resourceConfigurationPrimaryKey != null) {
            if (isForSubmit()) {
                documentLine.setResourceConfiguration(resourceConfigurationPrimaryKey);
            } else {
                ResourceConfiguration rc = entityStore.getEntity(ResourceConfiguration.class, resourceConfigurationPrimaryKey, true);
                documentLine.setResourceConfiguration(rc);
            }
        }
    }

    /**
     * Creates an event for changing only the item (accommodation type).
     */
    public static UpdateDocumentLineEvent createItemChangeEvent(DocumentLine documentLine, Item newItem) {
        return new UpdateDocumentLineEvent(documentLine, newItem, null);
    }

    /**
     * Creates an event for changing only the resource configuration (room assignment).
     */
    public static UpdateDocumentLineEvent createResourceConfigurationChangeEvent(DocumentLine documentLine, ResourceConfiguration newResourceConfiguration) {
        return new UpdateDocumentLineEvent(documentLine, null, newResourceConfiguration);
    }
}
