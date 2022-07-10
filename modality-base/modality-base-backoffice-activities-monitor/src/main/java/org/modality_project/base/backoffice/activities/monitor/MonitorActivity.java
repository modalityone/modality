package org.modality_project.base.backoffice.activities.monitor;

import dev.webfx.stack.framework.client.activity.impl.combinations.domainpresentation.impl.DomainPresentationActivityImpl;

/**
 * @author Bruno Salmon
 */
final class MonitorActivity extends DomainPresentationActivityImpl<MonitorPresentationModel> {

    MonitorActivity() {
        super(MonitorPresentationViewActivity::new, MonitorPresentationLogicActivity::new);
    }
}
