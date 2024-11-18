package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.i18n.HasI18nKey;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.backoffice.operations.entities.document.DocumentI18nKeys;

public final class ToggleMarkDocumentAsArrivedRequest extends AbstractSetDocumentFieldsRequest<ToggleMarkDocumentAsArrivedRequest>
    implements HasI18nKey {

    private final static String OPERATION_CODE = "ToggleMarkDocumentAsArrived";

    public ToggleMarkDocumentAsArrivedRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public ToggleMarkDocumentAsArrivedRequest(Document document, Pane parentContainer) {
        super(document, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public Object getI18nKey() {
        return DocumentI18nKeys.ToggleMarkAsArrived;
    }

    @Override
    public AsyncFunction<ToggleMarkDocumentAsArrivedRequest, Void> getOperationExecutor() {
        return ToggleMarkDocumentAsArrivedExecutor::executeRequest;
    }
}
