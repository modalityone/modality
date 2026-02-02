package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.HasEntity;
import dev.webfx.extras.operation.HasOperationCode;
import dev.webfx.extras.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Document;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractSetDocumentFieldsRequest<T extends AbstractSetDocumentFieldsRequest> implements HasOperationCode,
        HasEntity,
        HasOperationExecutor<T, Void> {

    private final Document document;
    private final Pane parentContainer;

    public AbstractSetDocumentFieldsRequest(Document document, Pane parentContainer) {
        this.document = document;
        this.parentContainer = parentContainer;
    }

    public Document getDocument() {
        return document;
    }

    public Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public Entity getEntity() {
        return document;
    }
}
