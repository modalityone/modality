package one.modality.ecommerce.client2018.businesslogic.workingdocument;

import dev.webfx.platform.util.Arrays;
import one.modality.ecommerce.client2018.businessdata.workingdocument.WorkingDocument;
import one.modality.ecommerce.client2018.businesslogic.rules.*;

/**
 * @author Bruno Salmon
 */
public final class WorkingDocumentLogic {

    private final static BusinessRule[] BUSINESS_RULES = {
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
