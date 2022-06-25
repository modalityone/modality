package mongoose.base.backoffice.operations.entities.generic;

import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.framework.shared.operation.HasOperationExecutor;
import dev.webfx.framework.shared.orm.entity.Entity;
import dev.webfx.platform.shared.async.AsyncFunction;
import javafx.scene.layout.Pane;
import mongoose.base.shared.entities.Organization;

import java.util.Collection;

// TODO move this class to module mongoose-base-backoffice-operations-snapshot
public final class AddNewSnapshotRequest implements HasOperationCode,
        HasOperationExecutor<AddNewSnapshotRequest, Void> {

    private final static String OPERATION_CODE = "AddNewSnapshot";

    private final Collection<? extends Entity> entities;
    private final Organization organization;
    private final Pane parentContainer;

    public AddNewSnapshotRequest(Collection<? extends Entity> entities, Organization organization, Pane parentContainer) {
        this.entities = entities;
        this.organization = organization;
        this.parentContainer = parentContainer;
    }

    Collection<? extends Entity> getEntities() {
        return entities;
    }
    Organization getOrganization() {
        return organization;
    }
    Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<AddNewSnapshotRequest, Void> getOperationExecutor() {
        return AddNewSnapshotExecutor::executeRequest;
    }

}
