package one.modality.ecommerce.payment.server.gateway;

/**
 * @author Bruno Salmon
 */
public record GatewayOrder(
    String id,
    String shortName,
    String longName,
    long amount,
    GatewayItem[] items
) { }
