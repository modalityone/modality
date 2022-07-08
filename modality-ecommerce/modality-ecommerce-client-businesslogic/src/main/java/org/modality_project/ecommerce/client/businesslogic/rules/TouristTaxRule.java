package org.modality_project.ecommerce.client.businesslogic.rules;

import org.modality_project.ecommerce.client.businesslogic.option.OptionLogic;
import org.modality_project.ecommerce.client.businessdata.workingdocument.WorkingDocument;
import org.modality_project.base.shared.entities.Option;

/**
 * @author Bruno Salmon
 */
public final class TouristTaxRule extends BusinessRule {

    @Override
    public void apply(WorkingDocument wd) {
        if (!wd.hasAccommodation())
            wd.removeTouristTax();
        else if (!wd.hasTouristTax()) {
            Option touristTaxOption = wd.getEventAggregate().findFirstOption(o -> OptionLogic.isTouristTaxOption(o) && (o.hasNoParent() || wd.getAccommodationLine() != null && o.getParent().getItem() == wd.getAccommodationLine().getItem()));
            if (touristTaxOption != null)
                addNewDependentLine(wd, touristTaxOption, wd.getAccommodationLine(), 0);
        }
    }
}
