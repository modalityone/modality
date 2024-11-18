package one.modality.ecommerce.backoffice.operations.entities.document.cart;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.i18n.HasI18nKey;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.backoffice.operations.entities.document.DocumentI18nKeys;

public final class OpenBookingCartRequest implements HasOperationCode, HasI18nKey,
        HasOperationExecutor<OpenBookingCartRequest, Void> {

    private final static String OPERATION_CODE = "OpenBookingCart";

    private final Document document;
    private final Pane parentContainer;

    public OpenBookingCartRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public OpenBookingCartRequest(Document document, Pane parentContainer) {
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
        return DocumentI18nKeys.OpenBookingCart;
    }

    @Override
    public AsyncFunction<OpenBookingCartRequest, Void> getOperationExecutor() {
        return OpenBookingCartExecutor::executeRequest;
    }
}
