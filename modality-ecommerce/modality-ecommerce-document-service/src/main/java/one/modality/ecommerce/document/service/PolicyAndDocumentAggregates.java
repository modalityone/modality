package one.modality.ecommerce.document.service;

import one.modality.ecommerce.policy.service.PolicyAggregate;

/**
 * @author Bruno Salmon
 */
public record PolicyAndDocumentAggregates(PolicyAggregate policyAggregate, DocumentAggregate documentAggregate) {

}
