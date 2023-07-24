package one.modality.ecommerce.backoffice.operations.entities.moneyaccount;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.sheet.EntityPropertiesSheet;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.MoneyAccount;
import one.modality.base.shared.entities.Organization;

final class AddNewMoneyAccountExecutor {

  static Future<Void> executeRequest(AddNewMoneyAccountRequest rq) {
    return execute(rq.getOrganization(), rq.getParentContainer());
  }

  private static Future<Void> execute(Organization organization, Pane parentContainer) {
    UpdateStore updateStore = UpdateStore.createAbove(organization.getStore());
    MoneyAccount insertEntity = updateStore.insertEntity(MoneyAccount.class);
    insertEntity.setOrganization(organization);

    EntityPropertiesSheet.editEntity(
        insertEntity, "name,closed,currency,event,gatewayCompany,type", parentContainer);

    return Future.succeededFuture();
  }
}
