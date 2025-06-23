package one.modality.ecommerce.payment.server.gateway;

/**
 * @author Bruno Salmon
 */
public record GatewayCustomer(
    String id,
    String firstName,
    String lastName,
    String email,
    String phone,
    String address,
    String city,
    String zipCode,
    String state,
    String country
) {}
