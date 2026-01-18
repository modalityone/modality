package one.modality.ecommerce.document.service.spi.impl.server;

import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.session.state.ThreadLocalStateHolder;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;
import one.modality.ecommerce.document.service.events.AbstractDocumentLineEvent;

/**
 * @author Bruno Salmon
 */
record DocumentSubmitRequest(
    SubmitDocumentChangesArgument argument,
    String runId,
    UpdateStore updateStore,
    Document document,
    DocumentLine documentLine,
    Object eventPrimaryKey
) {

    static DocumentSubmitRequest create(SubmitDocumentChangesArgument argument) {
        // Capturing the required client state info from thread local (before it will be wiped out by the async call)
        String runId = ThreadLocalStateHolder.getRunId();

        UpdateStore updateStore = UpdateStore.create(DataSourceModelService.getDefaultDataSourceModel());
        Document document = null;
        DocumentLine documentLine = null;
        AbstractDocumentEvent[] documentEvents = argument.documentEvents();
        for (AbstractDocumentEvent e : documentEvents) {
            e.setEntityStore(updateStore); // This indicates it's for submission
            e.replayEvent();
            if (document == null)
                document = e.getDocument();
            if (documentLine == null && e instanceof AbstractDocumentLineEvent) {
                documentLine = ((AbstractDocumentLineEvent) e).getDocumentLine();
            }
        }

        if (document == null && documentLine != null)
            document = documentLine.getDocument();

        Object eventPrimaryKey = document == null ? null : Entities.getPrimaryKey(document.getEventId());

        return new DocumentSubmitRequest(argument, runId, updateStore, document, documentLine, eventPrimaryKey);
    }

}
