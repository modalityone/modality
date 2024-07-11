package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.AsyncFunction;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;

public final class ToggleMarkDocumentAsWillPayRequest extends AbstractSetDocumentFieldsRequest<ToggleMarkDocumentAsWillPayRequest> {

    private final static String OPERATION_CODE = "ToggleMarkDocumentAsWillPay";

    public ToggleMarkDocumentAsWillPayRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }


    public ToggleMarkDocumentAsWillPayRequest(Document document, Pane parentContainer) {
        super(document, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<ToggleMarkDocumentAsWillPayRequest, Void> getOperationExecutor() {
        return ToggleMarkDocumentAsWillPayExecutor::executeRequest;
    }
}
