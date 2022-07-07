package org.modality_project.ecommerce.backoffice.operations.entities.document.registration;

import javafx.scene.layout.Pane;
import org.modality_project.base.backoffice.operations.entities.generic.SetEntityFieldRequest;
import org.modality_project.base.shared.entities.Document;

public final class ToggleCancelDocumentRequest extends SetEntityFieldRequest {

    private final static String OPERATION_CODE = "ToggleCancelDocument";

    public ToggleCancelDocumentRequest(Document document, Pane parentContainer) {
        super(document, "cancelled,read", "!cancelled,passReady?false:true", "Are you sure you want to cancel this booking?", parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
