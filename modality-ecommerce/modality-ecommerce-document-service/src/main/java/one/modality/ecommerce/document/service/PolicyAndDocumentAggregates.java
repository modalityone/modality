package one.modality.ecommerce.document.service;

/**
 * @author Bruno Salmon
 */
public final class PolicyAndDocumentAggregates {

    private final PolicyAggregate policyAggregate;
    private final DocumentAggregate documentAggregate;

    public PolicyAndDocumentAggregates(PolicyAggregate policyAggregate, DocumentAggregate documentAggregate) {
        this.policyAggregate = policyAggregate;
        this.documentAggregate = documentAggregate;
    }

    public PolicyAggregate getPolicyAggregate() {
        return policyAggregate;
    }

    public DocumentAggregate getDocumentAggregate() {
        return documentAggregate;
    }
}
