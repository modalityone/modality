package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.i18n.HasI18nKey;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.backoffice.operations.entities.document.DocumentI18nKeys;

public final class ToggleMarkDocumentAsWillPayRequest extends AbstractSetDocumentFieldsRequest<ToggleMarkDocumentAsWillPayRequest>
    implements HasI18nKey {

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
    public Object getI18nKey() {
        return DocumentI18nKeys.ToggleWillPay;
    }

    @Override
    public AsyncFunction<ToggleMarkDocumentAsWillPayRequest, Void> getOperationExecutor() {
        return ToggleMarkDocumentAsWillPayExecutor::executeRequest;
    }
}
