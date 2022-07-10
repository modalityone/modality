package org.modality_project.base.backoffice.operations.entities.generic;

import dev.webfx.stack.framework.client.ui.controls.entity.sheet.EntityPropertiesSheet;
import dev.webfx.stack.framework.shared.orm.entity.Entity;
import dev.webfx.stack.framework.shared.orm.entity.UpdateStore;
import dev.webfx.stack.platform.async.Future;
import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.Snapshot;
import org.modality_project.base.shared.entities.MoneyAccount;
import org.modality_project.base.shared.entities.Organization;

import java.util.Collection;

// TODO move this class to module modality-base-backoffice-operations-snapshot
final class AddNewSnapshotExecutor {

    static Future<Void> executeRequest(AddNewSnapshotRequest rq) {
        return execute(rq.getEntities(), rq.getOrganization(), rq.getParentContainer());
    }

    private static <E extends Entity> Future<Void> execute(Collection<E> entities, Organization organization, Pane parentContainer) {
        // TODO
        if (entities == null || entities.isEmpty()) {
            // TODO show popup
            System.out.println("entities null or empty.");
            return Future.succeededFuture();
        }

        UpdateStore updateStore = UpdateStore.createAbove(organization.getStore());
        MoneyAccount insertEntity2 = updateStore.insertEntity(MoneyAccount.class);

        Snapshot insertEntity = updateStore.insertEntity(Snapshot.class);
        insertEntity.setOrganization(organization);

        Pane propertiesSheetPane = new Pane();
        propertiesSheetPane.prefWidthProperty().bind(parentContainer.widthProperty());
        propertiesSheetPane.prefHeightProperty().bind(parentContainer.heightProperty());
        EntityPropertiesSheet.editEntity(insertEntity, "name", propertiesSheetPane);
        parentContainer.getChildren().add(propertiesSheetPane);

        return Future.succeededFuture();
    }
}
