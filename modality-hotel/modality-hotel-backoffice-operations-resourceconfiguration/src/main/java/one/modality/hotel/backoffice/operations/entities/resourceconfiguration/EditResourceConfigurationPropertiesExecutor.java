package one.modality.hotel.backoffice.operations.entities.resourceconfiguration;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.orm.entity.controls.entity.sheet.EntityPropertiesSheet;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.ResourceConfiguration;

final class EditResourceConfigurationPropertiesExecutor {

    static Future<Void> executeRequest(EditResourceConfigurationPropertiesRequest rq) {
        return execute(rq.getResourceConfiguration(), rq.getParentContainer());
    }

    private static Future<Void> execute(ResourceConfiguration resourceConfiguration, Pane parentContainer) {
        EntityPropertiesSheet.editEntity(resourceConfiguration, "name,online,max,comment", parentContainer);
        return Future.succeededFuture();
    }
}
