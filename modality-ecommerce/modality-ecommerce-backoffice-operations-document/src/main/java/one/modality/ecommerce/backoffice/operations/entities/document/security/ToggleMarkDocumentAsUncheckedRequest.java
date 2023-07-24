package one.modality.ecommerce.backoffice.operations.entities.document.security;

import javafx.scene.layout.Pane;

import one.modality.base.backoffice.operations.entities.generic.SetEntityFieldRequest;
import one.modality.base.shared.entities.Document;

public final class ToggleMarkDocumentAsUncheckedRequest extends SetEntityFieldRequest {

    private static final String OPERATION_CODE = "ToggleMarkDocumentAsUnchecked";

    public ToggleMarkDocumentAsUncheckedRequest(Document document, Pane parentContainer) {
        super(
                document,
                "person_unknown,person_known,person_verified",
                "false,false,false",
                null,
                parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
