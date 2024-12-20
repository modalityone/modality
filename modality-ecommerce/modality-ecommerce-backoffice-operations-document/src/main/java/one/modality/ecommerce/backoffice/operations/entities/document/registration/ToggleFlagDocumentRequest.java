package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.i18n.HasI18nKey;
import dev.webfx.stack.i18n.I18nKeys;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.backoffice.operations.entities.document.DocumentI18nKeys;

public final class ToggleFlagDocumentRequest extends AbstractSetDocumentFieldsRequest<ToggleFlagDocumentRequest>
    implements HasI18nKey {

    private final static String OPERATION_CODE = "ToggleFlagDocument";

    public ToggleFlagDocumentRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public ToggleFlagDocumentRequest(Document document, Pane parentContainer) {
        super(document, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public Object getI18nKey() {
        return I18nKeys.appendEllipsis(DocumentI18nKeys.ToggleFlag);
    }

    @Override
    public AsyncFunction<ToggleFlagDocumentRequest, Void> getOperationExecutor() {
        return ToggleFlagDocumentExecutor::executeRequest;
    }

}
