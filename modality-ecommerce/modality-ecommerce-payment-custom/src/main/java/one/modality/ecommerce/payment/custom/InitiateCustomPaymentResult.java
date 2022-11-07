package one.modality.ecommerce.payment.custom;

/**
 * @author Bruno Salmon
 */
public final class InitiateCustomPaymentResult {

    private final String htmlContent;

    public InitiateCustomPaymentResult(String customPaymentUrl) {
        this.htmlContent = customPaymentUrl;
    }

    public String getHtmlContent() {
        return htmlContent;
    }
}
