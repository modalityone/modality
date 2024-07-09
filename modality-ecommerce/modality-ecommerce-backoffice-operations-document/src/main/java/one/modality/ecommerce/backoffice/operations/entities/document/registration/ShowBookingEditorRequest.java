package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.orm.entity.HasEntity;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;

public final class ShowBookingEditorRequest implements HasOperationCode, HasEntity,
        HasOperationExecutor<ShowBookingEditorRequest, Void> {

    private final static String OPERATION_CODE = "ShowBookingEditor";

    private final Document document;
    private final Pane parentContainer;

    public ShowBookingEditorRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public ShowBookingEditorRequest(Document document, Pane parentContainer) {
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
    public Document getEntity() {
        return document;
    }

    @Override
    public AsyncFunction<ShowBookingEditorRequest, Void> getOperationExecutor() {
        return ShowBookingEditorExecutor::executeRequest;
    }
}
