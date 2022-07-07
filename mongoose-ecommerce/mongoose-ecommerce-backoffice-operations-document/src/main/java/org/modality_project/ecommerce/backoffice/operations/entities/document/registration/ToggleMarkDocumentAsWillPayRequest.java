package org.modality_project.ecommerce.backoffice.operations.entities.document.registration;

import javafx.scene.layout.Pane;
import org.modality_project.base.backoffice.operations.entities.generic.ToggleBooleanEntityFieldRequest;
import org.modality_project.base.shared.entities.Document;

public final class ToggleMarkDocumentAsWillPayRequest extends ToggleBooleanEntityFieldRequest {

    private final static String OPERATION_CODE = "ToggleMarkDocumentAsWillPay";

    public ToggleMarkDocumentAsWillPayRequest(Document document, Pane parentContainer) {
        super(document, "willPay", null, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
