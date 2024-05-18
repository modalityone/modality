package one.modality.ecommerce.document.service.spi;

import one.modality.ecommerce.document.service.*;

import dev.webfx.platform.async.Future;

/**
 * @author Bruno Salmon
 */
public interface DocumentServiceProvider {

    Future<PolicyAggregate> loadPolicy(LoadPolicyArgument argument);

    Future<DocumentAggregate> loadDocument(LoadDocumentArgument argument);

    Future<SubmitDocumentChangesResult> submitDocumentChanges(SubmitDocumentChangesArgument argument);

}
