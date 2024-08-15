package one.modality.ecommerce.backoffice.operations.entities.documentline;

import dev.webfx.platform.async.AsyncFunction;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.DocumentLine;

public final class ToggleCancelDocumentLineRequest extends AbstractSetDocumentLineFieldsRequest<ToggleCancelDocumentLineRequest> {

    private final static String OPERATION_CODE = "ToggleCancelDocumentLine";

    public ToggleCancelDocumentLineRequest(DocumentLine documentLine) {
        this(documentLine, FXMainFrameDialogArea.getDialogArea());
    }

    public ToggleCancelDocumentLineRequest(DocumentLine documentLine, Pane parentContainer) {
        super (documentLine, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<ToggleCancelDocumentLineRequest, Void> getOperationExecutor() {
        return ToggleCancelDocumentLineExecutor::executeRequest;
    }
}
