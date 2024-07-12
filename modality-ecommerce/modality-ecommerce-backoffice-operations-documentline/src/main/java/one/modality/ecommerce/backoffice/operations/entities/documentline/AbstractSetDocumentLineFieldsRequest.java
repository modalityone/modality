package one.modality.ecommerce.backoffice.operations.entities.documentline;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.HasEntity;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.DocumentLine;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractSetDocumentLineFieldsRequest<T extends AbstractSetDocumentLineFieldsRequest> implements HasOperationCode,
        HasEntity,
        HasOperationExecutor<T, Void> {

    private final DocumentLine documentLine;
    private final Pane parentContainer;

    public AbstractSetDocumentLineFieldsRequest(DocumentLine documentLine, Pane parentContainer) {
        this.documentLine = documentLine;
        this.parentContainer = parentContainer;
    }

    public DocumentLine getDocumentLine() {
        return documentLine;
    }

    public Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public Entity getEntity() {
        return documentLine;
    }
}
