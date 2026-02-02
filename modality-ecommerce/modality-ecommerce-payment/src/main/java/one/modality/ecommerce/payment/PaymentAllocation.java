package one.modality.ecommerce.payment;

/**
 * @param documentPrimaryKey  PK of the document to pay for
 * @param amount              The amount in cents to pay for this document.
 *
 * @author Bruno Salmon
 */
public record PaymentAllocation(
    Object documentPrimaryKey,
    int amount
) { }
