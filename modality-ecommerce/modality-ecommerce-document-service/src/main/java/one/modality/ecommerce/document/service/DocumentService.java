package one.modality.ecommerce.document.service;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.service.SingleServiceProvider;
import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Person;
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

    // Additional top-level utility methods to load document (not directly implemented by provider and not directly serialised)

    public static Future<DocumentAggregate> loadDocument(Event event, Person userPerson) {
        return loadDocument(Entities.getPrimaryKey(event), Entities.getPrimaryKey(userPerson));
    }

    public static Future<DocumentAggregate> loadDocument(Object eventPrimaryKey, Object userPersonPrimaryKey) {
        return eventPrimaryKey == null || userPersonPrimaryKey == null ? Future.succeededFuture(null) :
                loadDocument(new LoadDocumentArgument(userPersonPrimaryKey, eventPrimaryKey));
    }


    // Additional top-level utility methods to load document and policy (not directly implemented by provider and not directly serialised)

    public static Future<PolicyAndDocumentAggregates> loadDocumentWithPolicy(Document document) {
        return loadDocumentWithPolicyAndHistory(document, null);
    }

    public static Future<PolicyAndDocumentAggregates> loadDocumentWithPolicyAndWholeHistory(Document document) {
        return loadDocumentWithPolicyAndHistory(document, Integer.MAX_VALUE);
    }

    private static Future<PolicyAndDocumentAggregates> loadDocumentWithPolicyAndHistory(Document document, Object historyPrimaryKey) {
        return loadPolicyAndDocument(
                document.getEvent(),
                new LoadDocumentArgument(document.getPrimaryKey(), null, null, historyPrimaryKey));
    }

    public static Future<PolicyAndDocumentAggregates> loadPolicyAndDocument(Event event, Object userPersonPrimaryKey) {
        return loadPolicyAndDocument(event, userPersonPrimaryKey == null ? null : new LoadDocumentArgument(userPersonPrimaryKey, event.getPrimaryKey()));
    }

    private static Future<PolicyAndDocumentAggregates> loadPolicyAndDocument(Event event, LoadDocumentArgument loadDocumentArgument) {
        return Future.all(
                // 0) We load the policy aggregate for this event
                loadPolicy(new LoadPolicyArgument(event)),
                // 1) And eventually the already existing booking of the user (i.e. his last booking for this event)
                loadDocumentArgument == null ? Future.succeededFuture(null) : // unless the user is not provided
                        loadDocument(loadDocumentArgument)
        ).compose(compositeFuture -> {
            PolicyAggregate policyAggregate = compositeFuture.resultAt(0); // 0 = policy aggregate (never null)
            policyAggregate.rebuildEntities(event); // we rebuild the entities
            DocumentAggregate documentAggregate = compositeFuture.resultAt(1); // 1 = document aggregate (may be null)
            if (documentAggregate != null) {
                documentAggregate.setPolicyAggregate(policyAggregate); // rebuild the entities at the same time
            }
            // The reason why we return a PolicyAndDocumentAggregates instance (instead of just DocumentAggregate which
            // has a getPolicy() method) is because documentAggregate may be null (either because userPersonPrimaryKey
            // was null, or because this person hasn't booked yet that event).
            return Future.succeededFuture(new PolicyAndDocumentAggregates(policyAggregate, documentAggregate));
        });
    }

}
