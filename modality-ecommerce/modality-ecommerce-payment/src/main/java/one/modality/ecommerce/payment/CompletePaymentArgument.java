package one.modality.ecommerce.payment;

/**
 * @param paymentPrimaryKey PK of the generated payment in the database (MoneyTransfer in Modality)
 *
 * @author Bruno Salmon
 */
public record CompletePaymentArgument(
    Object paymentPrimaryKey,
    boolean isLive,
    String gatewayName,
    String gatewayCompletePaymentPayload
) { }
