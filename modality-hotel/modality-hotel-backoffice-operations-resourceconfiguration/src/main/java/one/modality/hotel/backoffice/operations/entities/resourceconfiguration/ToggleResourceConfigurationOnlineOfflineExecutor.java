package one.modality.hotel.backoffice.operations.entities.resourceconfiguration;

import dev.webfx.platform.async.Batch;
import dev.webfx.platform.async.Future;
import dev.webfx.stack.db.submit.SubmitResult;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.shared.entities.ResourceConfiguration;

final class ToggleResourceConfigurationOnlineOfflineExecutor {

  static Future<Batch<SubmitResult>> executeRequest(
      ToggleResourceConfigurationOnlineOfflineRequest rq) {
    return execute(rq.getResourceConfiguration());
  }

  private static Future<Batch<SubmitResult>> execute(ResourceConfiguration resourceConfiguration) {
    UpdateStore updateStore =
        UpdateStore.create(resourceConfiguration.getStore().getDataSourceModel());
    ResourceConfiguration rc = updateStore.updateEntity(resourceConfiguration);
    rc.setOnline(!rc.isOnline());
    return updateStore.submitChanges();
  }
}
