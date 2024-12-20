package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.i18n.HasI18nKey;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.backoffice.operations.entities.document.DocumentI18nKeys;

public final class MarkDocumentPassAsUpdatedRequest extends AbstractSetDocumentFieldsRequest<MarkDocumentPassAsUpdatedRequest>
    implements HasI18nKey {

    private final static String OPERATION_CODE = "MarkDocumentPassAsUpdated";

    public MarkDocumentPassAsUpdatedRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public MarkDocumentPassAsUpdatedRequest(Document document, Pane parentContainer) {
        super(document, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public Object getI18nKey() {
        return DocumentI18nKeys.PassUpdated;
    }

    @Override
    public AsyncFunction<MarkDocumentPassAsUpdatedRequest, Void> getOperationExecutor() {
        return MarkDocumentPassAsUpdatedExecutor::executeRequest;
    }
}
