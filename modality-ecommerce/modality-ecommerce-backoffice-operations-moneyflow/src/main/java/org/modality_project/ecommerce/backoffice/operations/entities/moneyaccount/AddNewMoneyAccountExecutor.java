package org.modality_project.ecommerce.backoffice.operations.entities.moneyaccount;

import dev.webfx.stack.orm.entity.controls.entity.sheet.EntityPropertiesSheet;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.async.Future;
import javafx.scene.layout.Pane;
import org.modality_project.base.shared.entities.MoneyAccount;
import org.modality_project.base.shared.entities.Organization;

final class AddNewMoneyAccountExecutor {

    static Future<Void> executeRequest(AddNewMoneyAccountRequest rq) {
        return execute(rq.getOrganization(), rq.getParentContainer());
    }

    private static Future<Void> execute(Organization organization, Pane parentContainer) {
        UpdateStore updateStore = UpdateStore.createAbove(organization.getStore());
        MoneyAccount insertEntity = updateStore.insertEntity(MoneyAccount.class);
        insertEntity.setOrganization(organization);

        Pane propertiesSheetPane = new Pane();
        propertiesSheetPane.prefWidthProperty().bind(parentContainer.widthProperty());
        propertiesSheetPane.prefHeightProperty().bind(parentContainer.heightProperty());
        EntityPropertiesSheet.editEntity(insertEntity, "name,closed,currency,event,gatewayCompany,type", propertiesSheetPane);
        parentContainer.getChildren().add(propertiesSheetPane);

        return Future.succeededFuture();
    }
}