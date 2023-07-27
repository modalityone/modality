package one.modality.ecommerce.client2018.businesslogic.rules;

import one.modality.ecommerce.client2018.businesslogic.option.OptionLogic;
import one.modality.ecommerce.client2018.businessdata.workingdocument.WorkingDocument;
import one.modality.base.shared.entities.Option;

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
