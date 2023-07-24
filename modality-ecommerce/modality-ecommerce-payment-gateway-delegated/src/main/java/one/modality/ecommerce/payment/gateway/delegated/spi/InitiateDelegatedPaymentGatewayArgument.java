package one.modality.ecommerce.payment.gateway.delegated.spi;

import one.modality.ecommerce.payment.delegated.InitiateDelegatedPaymentArgument;

/**
 * @author Bruno Salmon
 */
public class InitiateDelegatedPaymentGatewayArgument {

  private final InitiateDelegatedPaymentArgument userArgument;
  private final String accountId;
  private final String successUrl;
  private final String cancelUrl;

  public InitiateDelegatedPaymentGatewayArgument(
      InitiateDelegatedPaymentArgument userArgument,
      String accountId,
      String successUrl,
      String cancelUrl) {
    this.userArgument = userArgument;
    this.accountId = accountId;
    this.successUrl = successUrl;
    this.cancelUrl = cancelUrl;
  }

  public InitiateDelegatedPaymentArgument getUserArgument() {
    return userArgument;
  }

  public String getAccountId() {
    return accountId;
  }

  public String getSuccessUrl() {
    return successUrl;
  }

  public String getCancelUrl() {
    return cancelUrl;
  }
}
