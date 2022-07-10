package org.modality_project.hotel.backoffice.operations.entities.resourceconfiguration;

import dev.webfx.stack.framework.shared.orm.entity.Entity;
import dev.webfx.stack.framework.shared.orm.entity.UpdateStore;
import dev.webfx.stack.platform.shared.services.submit.SubmitResult;
import dev.webfx.stack.platform.async.Batch;
import dev.webfx.stack.platform.async.Future;

final class ToggleResourceConfigurationOnlineOfflineExecutor {

    static Future<Batch<SubmitResult>> executeRequest(ToggleResourceConfigurationOnlineOfflineRequest rq) {
        return execute(rq.getResourceConfiguration());
    }

    private static Future<Batch<SubmitResult>> execute(Entity resourceConfiguration) {
        UpdateStore updateStore = UpdateStore.create(resourceConfiguration.getStore().getDataSourceModel());
        Entity entity = updateStore.updateEntity(resourceConfiguration);
        entity.setFieldValue("online", !resourceConfiguration.getBooleanFieldValue("online"));
        return updateStore.submitChanges();
    }
}
