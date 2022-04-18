package mongoose.ecommerce.backoffice.operations.entities.moneyaccount;

import dev.webfx.framework.client.ui.controls.entity.sheet.EntityPropertiesSheet;
import dev.webfx.framework.shared.orm.entity.UpdateStore;
import dev.webfx.platform.shared.util.async.Future;
import javafx.scene.layout.Pane;
import mongoose.base.shared.entities.MoneyAccount;
import mongoose.base.shared.entities.Organization;

final class EditMoneyAccountExecutor {

    static Future<Void> executeRequest(EditMoneyAccountRequest rq) {
        return execute(rq.getMoneyAccount(), rq.getParentContainer());
    }

    private static Future<Void> execute(MoneyAccount moneyAccount, Pane parentContainer) {
        Pane propertiesSheetPane = new Pane();
        propertiesSheetPane.prefWidthProperty().bind(parentContainer.widthProperty());
        propertiesSheetPane.prefHeightProperty().bind(parentContainer.heightProperty());
        EntityPropertiesSheet.editEntity(moneyAccount, "name,closed,currency,event,gatewayCompany,type", propertiesSheetPane);
        parentContainer.getChildren().add(propertiesSheetPane);

        return Future.succeededFuture();
    }
}