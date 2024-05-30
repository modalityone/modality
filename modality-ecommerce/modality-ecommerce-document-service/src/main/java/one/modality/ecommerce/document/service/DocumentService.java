package one.modality.ecommerce.document.service;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.service.SingleServiceProvider;
import one.modality.ecommerce.document.service.spi.DocumentServiceProvider;

import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public final class DocumentService {

    private static DocumentServiceProvider getProvider() {
        return SingleServiceProvider.getProvider(DocumentServiceProvider.class, () -> ServiceLoader.load(DocumentServiceProvider.class));
    }

    public static Future<PolicyAggregate> loadPolicy(LoadPolicyArgument argument) {
        return getProvider().loadPolicy(argument);
    }

    public static Future<DocumentAggregate> loadDocument(LoadDocumentArgument argument) {
        return getProvider().loadDocument(argument);
    }

    public static Future<SubmitDocumentChangesResult> submitDocumentChanges(SubmitDocumentChangesArgument argument) {
        return getProvider().submitDocumentChanges(argument);
    }

}
