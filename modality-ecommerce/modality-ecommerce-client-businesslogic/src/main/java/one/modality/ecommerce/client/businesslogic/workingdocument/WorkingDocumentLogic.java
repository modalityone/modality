package one.modality.ecommerce.client.businesslogic.workingdocument;

import dev.webfx.platform.util.Arrays;
import one.modality.ecommerce.client.businessdata.workingdocument.WorkingDocument;
import one.modality.ecommerce.client.businesslogic.rules.*;

/**
 * @author Bruno Salmon
 */
public final class WorkingDocumentLogic {

  private static final BusinessRule[] BUSINESS_RULES = {
    new BreakfastRule(),
    new DietRule(),
    new TouristTaxRule(),
    new TranslationRule(),
    new HotelShuttleRule()
  };

  public static void applyBusinessRules(WorkingDocument workingDocument) {
    Arrays.forEach(BUSINESS_RULES, rule -> rule.apply(workingDocument));
  }
}
