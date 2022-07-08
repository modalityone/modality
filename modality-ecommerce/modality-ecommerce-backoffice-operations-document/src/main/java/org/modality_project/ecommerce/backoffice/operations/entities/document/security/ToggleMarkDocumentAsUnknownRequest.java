package org.modality_project.ecommerce.backoffice.operations.entities.document.security;

import javafx.scene.layout.Pane;
import org.modality_project.base.backoffice.operations.entities.generic.SetEntityFieldRequest;
import org.modality_project.base.shared.entities.Document;

public final class ToggleMarkDocumentAsUnknownRequest extends SetEntityFieldRequest {

    private final static String OPERATION_CODE = "ToggleMarkDocumentAsUnknown";

    public ToggleMarkDocumentAsUnknownRequest(Document document, Pane parentContainer) {
        super(document, "person_unknown,person_known,person_verified", "true,false,false", null, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
