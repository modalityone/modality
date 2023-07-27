package one.modality.base.backoffice2018.activities.monitor;

import dev.webfx.stack.orm.domainmodel.activity.domainpresentation.impl.DomainPresentationActivityImpl;

/**
 * @author Bruno Salmon
 */
final class MonitorActivity extends DomainPresentationActivityImpl<MonitorPresentationModel> {

    MonitorActivity() {
        super(MonitorPresentationViewActivity::new, MonitorPresentationLogicActivity::new);
    }
}
