package org.modality_project.ecommerce.backoffice.operations.entities.documentline;

import javafx.scene.layout.Pane;
import org.modality_project.base.backoffice.operations.entities.generic.ToggleBooleanEntityFieldRequest;
import org.modality_project.base.shared.entities.DocumentLine;

public final class ToggleCancelDocumentLineRequest extends ToggleBooleanEntityFieldRequest {

    private final static String OPERATION_CODE = "ToggleCancelDocumentLine";

    public ToggleCancelDocumentLineRequest(DocumentLine documentLine, Pane parentContainer) {
        super (documentLine, "cancelled", "Are you sure you want to cancel this option?", parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
