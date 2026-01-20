package one.modality.ecommerce.document.service.buscall;

import dev.webfx.stack.com.bus.call.spi.AsyncFunctionBusCallEndpoint;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.LoadDocumentArgument;

/**
 * @author Bruno Salmon
 */
public class LoadDocumentsMethodEndpoint extends AsyncFunctionBusCallEndpoint<LoadDocumentArgument, DocumentAggregate[]> {

    public LoadDocumentsMethodEndpoint() {
        super(DocumentServiceBusAddresses.LOAD_DOCUMENTS_METHOD_ADDRESS, DocumentService::loadDocuments);
    }

}
