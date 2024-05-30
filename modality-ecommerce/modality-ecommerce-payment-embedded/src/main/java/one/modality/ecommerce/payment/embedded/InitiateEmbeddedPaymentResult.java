package one.modality.ecommerce.payment.embedded;

/**
 * @author Bruno Salmon
 */
public final class InitiateEmbeddedPaymentResult {

    private final String htmlContent;

    public InitiateEmbeddedPaymentResult(String customPaymentUrl) {
        this.htmlContent = customPaymentUrl;
    }

    public String getHtmlContent() {
        return htmlContent;
    }
}
