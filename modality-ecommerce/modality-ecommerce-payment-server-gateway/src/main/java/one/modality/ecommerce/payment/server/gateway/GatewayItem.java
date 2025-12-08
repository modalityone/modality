package one.modality.ecommerce.payment.server.gateway;

/**
 * @author Bruno Salmon
 */
public record GatewayItem(
    String id,
    String shortName,
    String longName,
    int quantity,
    long amount
) { }
