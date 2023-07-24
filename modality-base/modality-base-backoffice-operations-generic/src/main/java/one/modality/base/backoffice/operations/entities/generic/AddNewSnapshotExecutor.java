package one.modality.base.backoffice.operations.entities.generic;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.sheet.EntityPropertiesSheet;

import javafx.scene.layout.Pane;

import one.modality.base.shared.entities.MoneyAccount;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.Snapshot;

import java.util.Collection;

// TODO move this class to module modality-base-backoffice-operations-snapshot
final class AddNewSnapshotExecutor {

    static Future<Void> executeRequest(AddNewSnapshotRequest rq) {
        return execute(rq.getEntities(), rq.getOrganization(), rq.getParentContainer());
    }

    private static <E extends Entity> Future<Void> execute(
            Collection<E> entities, Organization organization, Pane parentContainer) {
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
