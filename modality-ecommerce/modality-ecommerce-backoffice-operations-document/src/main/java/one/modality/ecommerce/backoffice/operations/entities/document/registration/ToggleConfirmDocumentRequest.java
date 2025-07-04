package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.extras.i18n.HasI18nKey;
import dev.webfx.extras.i18n.I18nKeys;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.backoffice.operations.entities.document.DocumentI18nKeys;

public final class ToggleConfirmDocumentRequest extends AbstractSetDocumentFieldsRequest<ToggleConfirmDocumentRequest>
    implements HasI18nKey {

    private final static String OPERATION_CODE = "ToggleConfirmDocument";

    public ToggleConfirmDocumentRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public ToggleConfirmDocumentRequest(Document document, Pane parentContainer) {
        super(document, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public Object getI18nKey() {
        return I18nKeys.appendEllipsis(DocumentI18nKeys.ToggleConfirm);
    }

    @Override
    public AsyncFunction<ToggleConfirmDocumentRequest, Void> getOperationExecutor() {
        return ToggleConfirmDocumentExecutor::executeRequest;
    }
}
