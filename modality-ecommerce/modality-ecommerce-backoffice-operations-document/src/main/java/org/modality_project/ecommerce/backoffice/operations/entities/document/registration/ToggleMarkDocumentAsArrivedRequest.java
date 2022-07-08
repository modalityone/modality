package org.modality_project.ecommerce.backoffice.operations.entities.document.registration;

import javafx.scene.layout.Pane;
import org.modality_project.base.backoffice.operations.entities.generic.ToggleBooleanEntityFieldRequest;
import org.modality_project.base.shared.entities.Document;

public final class ToggleMarkDocumentAsArrivedRequest extends ToggleBooleanEntityFieldRequest {

    private final static String OPERATION_CODE = "ToggleMarkDocumentAsArrived";

    public ToggleMarkDocumentAsArrivedRequest(Document document, Pane parentContainer) {
        super(document, "arrived", null, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
