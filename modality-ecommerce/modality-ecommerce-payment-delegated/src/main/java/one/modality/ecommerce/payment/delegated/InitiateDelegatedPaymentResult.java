package one.modality.ecommerce.payment.delegated;

/**
 * @author Bruno Salmon
 */
public class InitiateDelegatedPaymentResult {

  private final String delegatedPaymentUrl;

  public InitiateDelegatedPaymentResult(String delegatedPaymentUrl) {
    this.delegatedPaymentUrl = delegatedPaymentUrl;
  }

  public String getDelegatedPaymentUrl() {
    return delegatedPaymentUrl;
  }
}
