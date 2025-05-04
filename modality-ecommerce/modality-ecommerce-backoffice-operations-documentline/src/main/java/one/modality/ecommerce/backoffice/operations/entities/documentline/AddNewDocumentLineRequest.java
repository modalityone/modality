package one.modality.ecommerce.backoffice.operations.entities.documentline;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.i18n.HasI18nKey;
import dev.webfx.stack.i18n.I18nKeys;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;

public final class AddNewDocumentLineRequest implements HasOperationCode, HasI18nKey,
        HasOperationExecutor<AddNewDocumentLineRequest, Void> {

    private final static String OPERATION_CODE = "AddNewDocumentLine";

    private final Document document;
    private final Pane parentContainer;

    public AddNewDocumentLineRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public AddNewDocumentLineRequest(Document document, Pane parentContainer) {
        this.document = document;
        this.parentContainer = parentContainer;
    }

    Document getDocument() {
        return document;
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
        return I18nKeys.appendEllipsis(BaseI18nKeys.Add);
    }

    @Override
    public AsyncFunction<AddNewDocumentLineRequest, Void> getOperationExecutor() {
        return AddNewDocumentLineExecutor::executeRequest;
    }
}
