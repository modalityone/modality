package one.modality.ecommerce.document.service.spi;

import one.modality.ecommerce.document.service.*;

import dev.webfx.platform.async.Future;

/**
 * @author Bruno Salmon
 */
public interface DocumentServiceProvider {

    Future<DocumentAggregate> loadDocument(LoadDocumentArgument argument);

    Future<DocumentAggregate[]> loadDocuments(LoadDocumentArgument argument);

    Future<SubmitDocumentChangesResult> submitDocumentChanges(SubmitDocumentChangesArgument argument);

    Future<Boolean> leaveEventQueue(Object queueToken);

}
