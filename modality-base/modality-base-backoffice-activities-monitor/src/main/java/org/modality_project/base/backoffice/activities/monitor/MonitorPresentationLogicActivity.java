package org.modality_project.base.backoffice.activities.monitor;

import org.modality_project.base.client.activity.ModalityDomainPresentationLogicActivityBase;
import dev.webfx.stack.framework.client.orm.reactive.mapping.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.framework.client.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.framework.shared.orm.entity.Entity;

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
        ReactiveEntitiesMapper<Entity> metricsMapper = ReactiveEntitiesMapper.createPushReactiveChain(this)
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
