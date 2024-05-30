package one.modality.ecommerce.payment.redirect;

/**
 * @author Bruno Salmon
 */
public class InitiateRedirectPaymentResult {

    private final String redirectPaymentUrl;

    public InitiateRedirectPaymentResult(String redirectPaymentUrl) {
        this.redirectPaymentUrl = redirectPaymentUrl;
    }

    public String getRedirectPaymentUrl() {
        return redirectPaymentUrl;
    }

}
