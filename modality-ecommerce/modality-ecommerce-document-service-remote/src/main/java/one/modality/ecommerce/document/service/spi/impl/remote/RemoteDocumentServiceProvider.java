package one.modality.ecommerce.document.service.spi.impl.remote;

import dev.webfx.stack.com.bus.call.BusCallService;
import one.modality.ecommerce.document.service.*;
import one.modality.ecommerce.document.service.buscall.DocumentServiceBusAddress;
import one.modality.ecommerce.document.service.spi.DocumentServiceProvider;

import dev.webfx.platform.async.Future;
/**
 * @author Bruno Salmon
 */
public class RemoteDocumentServiceProvider implements DocumentServiceProvider {

    @Override
    public Future<PolicyAggregate> loadPolicy(LoadPolicyArgument argument) {
        return BusCallService.call(DocumentServiceBusAddress.LOAD_POLICY_METHOD_ADDRESS, argument);
    }

    @Override
    public Future<DocumentAggregate> loadDocument(LoadDocumentArgument argument) {
        return BusCallService.call(DocumentServiceBusAddress.LOAD_DOCUMENT_METHOD_ADDRESS, argument);
    }

    @Override
    public Future<Object> submitDocumentChanges(SubmitDocumentChangesArgument argument) {
        return BusCallService.call(DocumentServiceBusAddress.SUBMIT_DOCUMENT_CHANGES_METHOD_ADDRESS, argument);
    }
}
