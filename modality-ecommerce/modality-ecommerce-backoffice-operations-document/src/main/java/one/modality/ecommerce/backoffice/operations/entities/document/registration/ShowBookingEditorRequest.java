package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.extras.i18n.HasI18nKey;
import dev.webfx.stack.orm.entity.HasEntity;
import dev.webfx.extras.operation.HasOperationCode;
import dev.webfx.extras.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.backoffice.operations.entities.document.DocumentI18nKeys;

public final class ShowBookingEditorRequest implements HasOperationCode, HasI18nKey, HasEntity,
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
    public Object getI18nKey() {
        return DocumentI18nKeys.ShowBookingEditor;
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
