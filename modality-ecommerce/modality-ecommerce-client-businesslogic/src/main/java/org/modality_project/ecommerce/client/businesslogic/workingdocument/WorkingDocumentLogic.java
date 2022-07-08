package org.modality_project.ecommerce.client.businesslogic.workingdocument;

import dev.webfx.platform.shared.util.Arrays;
import org.modality_project.ecommerce.client.businessdata.workingdocument.WorkingDocument;
import org.modality_project.ecommerce.client.businesslogic.rules.*;
import org.modality_project.ecommerce.client.businesslogic.rules.*;

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
