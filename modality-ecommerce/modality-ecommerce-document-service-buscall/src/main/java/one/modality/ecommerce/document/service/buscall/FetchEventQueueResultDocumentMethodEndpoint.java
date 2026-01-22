package one.modality.ecommerce.document.service.buscall;

import dev.webfx.stack.com.bus.call.spi.AsyncFunctionBusCallEndpoint;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesResult;

/**
 * @author Bruno Salmon
 */
public class FetchEventQueueResultDocumentMethodEndpoint extends AsyncFunctionBusCallEndpoint<Object, SubmitDocumentChangesResult> {

    public FetchEventQueueResultDocumentMethodEndpoint() {
        super(DocumentServiceBusAddresses.FETCH_EVENT_QUEUE_RESULT_ADDRESS, DocumentService::fetchEventQueueResult);
    }

}
