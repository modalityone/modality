package one.modality.base.backoffice.activities.monitor;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import one.modality.base.client.activity.ModalityDomainPresentationLogicActivityBase;

/**
 * @author Bruno Salmon
 */
final class MonitorPresentationLogicActivity
    extends ModalityDomainPresentationLogicActivityBase<MonitorPresentationModel> {

  MonitorPresentationLogicActivity() {
    super(MonitorPresentationModel::new);
  }

  @Override
  protected void startLogic(MonitorPresentationModel pm) {
    ReactiveEntitiesMapper<Entity> metricsMapper =
        ReactiveEntitiesMapper.createPushReactiveChain(this)
            .always("{class: 'Metrics', orderBy: 'date desc', limit: '500'}");

    ReactiveVisualMapper.create(metricsMapper)
        .setEntityColumns("['0 + id','memoryUsed','memoryTotal']")
        .visualizeResultInto(pm.memoryVisualResultProperty());

    ReactiveVisualMapper.create(metricsMapper)
        .setEntityColumns("['0 + id','systemLoadAverage','processCpuLoad']")
        .visualizeResultInto(pm.cpuVisualResultProperty())
        .start();
  }
}
