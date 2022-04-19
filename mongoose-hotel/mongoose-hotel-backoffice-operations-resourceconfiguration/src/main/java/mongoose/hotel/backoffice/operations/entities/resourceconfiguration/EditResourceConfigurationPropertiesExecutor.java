package mongoose.hotel.backoffice.operations.entities.resourceconfiguration;

import javafx.scene.layout.Pane;
import dev.webfx.framework.client.ui.controls.entity.sheet.EntityPropertiesSheet;
import dev.webfx.framework.shared.orm.entity.Entity;
import dev.webfx.platform.shared.async.Future;

final class EditResourceConfigurationPropertiesExecutor {

    static Future<Void> executeRequest(EditResourceConfigurationPropertiesRequest rq) {
        return execute(rq.getResourceConfiguration(), rq.getParentContainer());
    }

    private static Future<Void> execute(Entity resourceConfiguration, Pane parentContainer) {
        EntityPropertiesSheet.editEntity(resourceConfiguration, "name,online,max,comment", parentContainer);
        return Future.succeededFuture();
    }
}
