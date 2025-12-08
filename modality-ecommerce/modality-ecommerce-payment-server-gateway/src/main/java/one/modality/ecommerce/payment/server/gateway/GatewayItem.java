package one.modality.ecommerce.payment.server.gateway;

/**
 * @author Bruno Salmon
 */
public record GatewayItem(
    String id,
    String name,
    String description,
    int quantity,
    long price
) { }
