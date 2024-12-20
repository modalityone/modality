package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.i18n.HasI18nKey;
import dev.webfx.stack.i18n.I18nKeys;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.backoffice.operations.entities.document.DocumentI18nKeys;

public final class ToggleCancelDocumentRequest extends AbstractSetDocumentFieldsRequest<ToggleCancelDocumentRequest>
    implements HasI18nKey {

    private final static String OPERATION_CODE = "ToggleCancelDocument";

    public ToggleCancelDocumentRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public ToggleCancelDocumentRequest(Document document, Pane parentContainer) {
        super(document, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public Object getI18nKey() {
        return I18nKeys.appendEllipsis(DocumentI18nKeys.ToggleCancel);
    }

    @Override
    public AsyncFunction<ToggleCancelDocumentRequest, Void> getOperationExecutor() {
        return ToggleCancelDocumentExecutor::executeRequest;
    }
}
