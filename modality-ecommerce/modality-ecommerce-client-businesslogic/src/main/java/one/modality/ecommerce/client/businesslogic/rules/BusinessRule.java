package one.modality.ecommerce.client.businesslogic.rules;

import one.modality.base.shared.entities.Option;
import one.modality.ecommerce.client.businessdata.workingdocument.WorkingDocument;
import one.modality.ecommerce.client.businessdata.workingdocument.WorkingDocumentLine;

/**
 * @author Bruno Salmon
 */
public abstract class BusinessRule {

    public abstract void apply(WorkingDocument wd);

    static WorkingDocumentLine addNewDependentLine(
            WorkingDocument wd,
            Option dependentOption,
            WorkingDocumentLine masterLine,
            long shiftDays) {
        WorkingDocumentLine dependantLine = new WorkingDocumentLine(dependentOption, wd, null);
        applySameAttendances(dependantLine, masterLine, shiftDays);
        wd.getWorkingDocumentLines().add(dependantLine);
        return dependantLine;
    }

    static void applySameAttendances(
            WorkingDocumentLine dependentLine, WorkingDocumentLine masterLine, long shiftDays) {
        dependentLine.setDaysArray(masterLine.getDaysArray().shift(shiftDays));
    }
}
