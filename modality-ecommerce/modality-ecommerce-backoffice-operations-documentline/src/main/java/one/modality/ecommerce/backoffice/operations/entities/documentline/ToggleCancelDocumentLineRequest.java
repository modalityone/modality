package one.modality.ecommerce.backoffice.operations.entities.documentline;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.extras.i18n.HasI18nKey;
import dev.webfx.extras.i18n.I18nKeys;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.ecommerce.backoffice.operations.entities.document.DocumentI18nKeys;

public final class ToggleCancelDocumentLineRequest extends AbstractSetDocumentLineFieldsRequest<ToggleCancelDocumentLineRequest>
    implements HasI18nKey {

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
    public Object getI18nKey() {
        return I18nKeys.appendEllipsis(DocumentI18nKeys.ToggleCancel);
    }

    @Override
    public AsyncFunction<ToggleCancelDocumentLineRequest, Void> getOperationExecutor() {
        return ToggleCancelDocumentLineExecutor::executeRequest;
    }
}
