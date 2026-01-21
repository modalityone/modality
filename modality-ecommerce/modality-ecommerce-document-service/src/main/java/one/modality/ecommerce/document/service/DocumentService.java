package one.modality.ecommerce.document.service;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.service.SingleServiceProvider;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.ecommerce.document.service.spi.DocumentServiceProvider;
import one.modality.ecommerce.policy.service.LoadPolicyArgument;
import one.modality.ecommerce.policy.service.PolicyAggregate;
import one.modality.ecommerce.policy.service.PolicyService;

import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public final class DocumentService {

    private static DocumentServiceProvider getProvider() {
        return SingleServiceProvider.getProvider(DocumentServiceProvider.class, () -> ServiceLoader.load(DocumentServiceProvider.class));
    }

    public static Future<DocumentAggregate> loadDocument(LoadDocumentArgument argument) {
        return getProvider().loadDocument(argument);
    }

    public static Future<DocumentAggregate[]> loadDocuments(LoadDocumentArgument argument) {
        return getProvider().loadDocuments(argument);
    }

    public static Future<SubmitDocumentChangesResult> submitDocumentChanges(SubmitDocumentChangesArgument argument) {
        return getProvider().submitDocumentChanges(argument);
    }

    public static Future<Boolean> leaveEventQueue(Object queueToken) {
        return getProvider().leaveEventQueue(queueToken);
    }

    // Additional top-level utility methods to load a document (not directly implemented by the provider and not directly serialized)

    public static Future<DocumentAggregate> loadDocument(Object event, Object userPerson) {
        return loadDocument(LoadDocumentArgument.ofPerson(userPerson, event));
    }


    // Additional top-level utility methods to load document and policy (not directly implemented by the provider and not directly serialized)

    public static Future<PolicyAndDocumentAggregates> loadDocumentWithPolicy(Document document) {
        return loadDocumentWithPolicyAndHistory(document, null);
    }

    public static Future<PolicyAndDocumentAggregates> loadDocumentWithPolicyAndWholeHistory(Document document) {
        return loadDocumentWithPolicyAndHistory(document, Integer.MAX_VALUE);
    }

    private static Future<PolicyAndDocumentAggregates> loadDocumentWithPolicyAndHistory(Document document, Object history) {
        return loadPolicyAndDocument(
            document.getEvent(),
            LoadDocumentArgument.ofDocumentFromHistory(document, history));
    }

    public static Future<PolicyAndDocumentAggregates> loadPolicyAndDocument(Event event, Object userPerson) {
        return loadPolicyAndDocument(event, userPerson == null ? null : LoadDocumentArgument.ofPerson(userPerson, event));
    }

    private static Future<PolicyAndDocumentAggregates> loadPolicyAndDocument(Event event, LoadDocumentArgument loadDocumentArgument) {
        return Future.all(
            // 0) We load the policy aggregate for this event
            PolicyService.loadPolicy(new LoadPolicyArgument(event)),
            // 1) And eventually the already existing booking of the user (i.e., his last booking for this event)
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
            // was null, or because this person hasn't booked that event yet).
            return Future.succeededFuture(new PolicyAndDocumentAggregates(policyAggregate, documentAggregate));
        });
    }

    // Note: this method doesn't rebuild the PolicyAggregate entities because no event entity was passed
    public static Future<PolicyAndDocumentAggregates> loadPolicyAndDocument(LoadDocumentArgument loadDocumentArgument) {
        return loadDocument(loadDocumentArgument)
            .compose(documentAggregate -> PolicyService.loadPolicy(new LoadPolicyArgument(documentAggregate.getEventPrimaryKey()))
                .compose(policyAggregate -> Future.succeededFuture(new PolicyAndDocumentAggregates(policyAggregate, documentAggregate))));
    }
}