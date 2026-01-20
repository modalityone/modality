package one.modality.ecommerce.document.service.buscall;

import dev.webfx.stack.com.bus.call.spi.AsyncFunctionBusCallEndpoint;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.SubmitDocumentChangesResult;

/**
 * @author Bruno Salmon
 */
public class SubmitDocumentMethodEndpoint extends AsyncFunctionBusCallEndpoint<SubmitDocumentChangesArgument, SubmitDocumentChangesResult> {

    public SubmitDocumentMethodEndpoint() {
        super(DocumentServiceBusAddresses.SUBMIT_DOCUMENT_CHANGES_METHOD_ADDRESS, DocumentService::submitDocumentChanges);
    }

}
