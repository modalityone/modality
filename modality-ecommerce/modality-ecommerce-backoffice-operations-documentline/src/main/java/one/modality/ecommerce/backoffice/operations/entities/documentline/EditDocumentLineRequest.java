package one.modality.ecommerce.backoffice.operations.entities.documentline;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.i18n.HasI18nKey;
import dev.webfx.stack.i18n.I18nKeys;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.client.i18n.ModalityI18nKeys;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.DocumentLine;

public final class EditDocumentLineRequest implements HasOperationCode, HasI18nKey,
        HasOperationExecutor<EditDocumentLineRequest, Void> {

    private final static String OPERATION_CODE = "EditDocumentLine";

    private final DocumentLine documentLine;
    private final Pane parentContainer;

    public EditDocumentLineRequest(DocumentLine documentLine) {
        this(documentLine, FXMainFrameDialogArea.getDialogArea());
    }

    public EditDocumentLineRequest(DocumentLine documentLine, Pane parentContainer) {
        this.documentLine = documentLine;
        this.parentContainer = parentContainer;
    }

    DocumentLine getDocumentLine() {
        return documentLine;
    }

    Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public Object getI18nKey() {
        return I18nKeys.appendEllipsis(ModalityI18nKeys.Edit);
    }

    @Override
    public AsyncFunction<EditDocumentLineRequest, Void> getOperationExecutor() {
        return EditDocumentLineExecutor::executeRequest;
    }
}
