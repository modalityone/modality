package one.modality.ecommerce.backoffice.operations.entities.document.security;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.i18n.HasI18nKey;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.backoffice.operations.entities.document.DocumentI18nKeys;
import one.modality.ecommerce.backoffice.operations.entities.document.registration.AbstractSetDocumentFieldsRequest;

public final class MarkDocumentAsKnownRequest extends AbstractSetDocumentFieldsRequest<MarkDocumentAsKnownRequest>
    implements HasI18nKey {

    private final static String OPERATION_CODE = "MarkDocumentAsKnown";

    public MarkDocumentAsKnownRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public MarkDocumentAsKnownRequest(Document document, Pane parentContainer) {
        super(document, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public Object getI18nKey() {
        return DocumentI18nKeys.MarkAsKnown;
    }

    @Override
    public AsyncFunction<MarkDocumentAsKnownRequest, Void> getOperationExecutor() {
        return MarkDocumentAsKnownExecutor::executeRequest;
    }

}
