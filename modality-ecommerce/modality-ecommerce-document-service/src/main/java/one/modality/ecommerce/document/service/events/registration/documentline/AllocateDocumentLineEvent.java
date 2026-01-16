package one.modality.ecommerce.document.service.events.registration.documentline;

import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.ecommerce.document.service.events.AbstractDocumentLineEvent;

/**
 * @author Bruno Salmon
 */
public final class AllocateDocumentLineEvent extends AbstractDocumentLineEvent {

    private final Object resourceConfiguration;

    public AllocateDocumentLineEvent(Object documentPrimaryKey, Object documentLinePrimaryKey, Object resourceConfigurationPrimaryKey) {
        super(documentPrimaryKey, documentLinePrimaryKey);
        this.resourceConfiguration = resourceConfigurationPrimaryKey;
    }

    public AllocateDocumentLineEvent(DocumentLine documentLine, ResourceConfiguration resourceConfiguration) {
        super(documentLine);
        this.resourceConfiguration = resourceConfiguration;
    }

    public Object getResourceConfiguration() {
        return resourceConfiguration;
    }

    @Override
    public void replayEventOnDocumentLine() {
        super.replayEventOnDocumentLine();
        if (resourceConfiguration != null)
            documentLine.setResourceConfiguration(resourceConfiguration);
    }
}
