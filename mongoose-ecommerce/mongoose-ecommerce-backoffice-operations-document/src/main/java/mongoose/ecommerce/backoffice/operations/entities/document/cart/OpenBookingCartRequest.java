package mongoose.ecommerce.backoffice.operations.entities.document.cart;

import javafx.scene.layout.Pane;
import mongoose.base.shared.entities.Document;
import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.framework.shared.operation.HasOperationExecutor;
import dev.webfx.platform.shared.util.async.AsyncFunction;

public final class OpenBookingCartRequest implements HasOperationCode,
        HasOperationExecutor<OpenBookingCartRequest, Void> {

    private final static String OPERATION_CODE = "OpenBookingCart";

    private final Document document;
    private final Pane parentContainer;

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
    public AsyncFunction<OpenBookingCartRequest, Void> getOperationExecutor() {
        return OpenBookingCartExecutor::executeRequest;
    }
}
