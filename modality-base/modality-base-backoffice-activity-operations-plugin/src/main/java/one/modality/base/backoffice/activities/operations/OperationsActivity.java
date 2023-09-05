package one.modality.base.backoffice.activities.operations;

import dev.webfx.stack.orm.domainmodel.activity.domainpresentation.impl.DomainPresentationActivityImpl;

/**
 * @author Bruno Salmon
 */
final class OperationsActivity extends DomainPresentationActivityImpl<OperationsPresentationModel> {

    OperationsActivity() {
        super(OperationsPresentationViewActivity::new, OperationsPresentationLogicActivity::new);
    }
}