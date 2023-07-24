// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.payment.delegated {

  // Direct dependencies modules
  requires java.base;
  requires transitive webfx.platform.async;
  requires webfx.platform.util;

  // Exported packages
  exports one.modality.ecommerce.payment.delegated;
  exports one.modality.ecommerce.payment.delegated.spi;

  // Used services
  uses one.modality.ecommerce.payment.delegated.spi.DelegatedPaymentProvider;
}
