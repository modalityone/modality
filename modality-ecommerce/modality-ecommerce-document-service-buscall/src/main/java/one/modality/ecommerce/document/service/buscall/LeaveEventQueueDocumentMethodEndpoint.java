package one.modality.ecommerce.document.service.buscall;

import dev.webfx.stack.com.bus.call.spi.AsyncFunctionBusCallEndpoint;
import one.modality.ecommerce.document.service.DocumentService;

/**
 * @author Bruno Salmon
 */
public class LeaveEventQueueDocumentMethodEndpoint extends AsyncFunctionBusCallEndpoint<Object, Boolean> {

    public LeaveEventQueueDocumentMethodEndpoint() {
        super(DocumentServiceBusAddresses.LEAVE_EVENT_QUEUE_ADDRESS, DocumentService::leaveEventQueue);
    }

}
