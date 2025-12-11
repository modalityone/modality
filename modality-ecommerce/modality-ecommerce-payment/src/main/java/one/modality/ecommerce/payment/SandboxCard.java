package one.modality.ecommerce.payment;

/**
 * @author Bruno Salmon
 */
public record SandboxCard(
    String name,
    String numbers,
    String expirationDate,
    String cvv,
    String zip
) { }
