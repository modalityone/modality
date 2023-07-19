package one.modality.crm.backoffice2018.activities.organizations;

import dev.webfx.stack.orm.domainmodel.activity.domainpresentation.impl.DomainPresentationActivityImpl;

/**
 * @author Bruno Salmon
 */
final class OrganizationsActivity extends DomainPresentationActivityImpl<OrganizationsPresentationModel> {

    OrganizationsActivity() {
        super(OrganizationsPresentationViewActivity::new, OrganizationsPresentationLogicActivity::new);
    }
}
