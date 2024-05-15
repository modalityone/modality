package one.modality.ecommerce.document.service.buscall;

import dev.webfx.stack.com.bus.call.spi.AsyncFunctionBusCallEndpoint;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.LoadPolicyArgument;
import one.modality.ecommerce.document.service.PolicyAggregate;

/**
 * @author Bruno Salmon
 */
public class LoadPolicyMethodEndpoint extends AsyncFunctionBusCallEndpoint<LoadPolicyArgument, PolicyAggregate> {

    public LoadPolicyMethodEndpoint() {
        super(DocumentServiceBusAddress.LOAD_POLICY_METHOD_ADDRESS, DocumentService::loadPolicy);
    }

}
