package one.modality.hotel.backoffice.activities.roomsetup;

import one.modality.base.client.activity.organizationdependent.OrganizationDependentGenericTablePresentationModel;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;

/**
 * Presentation model for the Room Setup activity.
 * Provides organization-dependent context for room management views.
 *
 * @author Claude Code
 */
public class RoomSetupPresentationModel extends OrganizationDependentGenericTablePresentationModel {

    public void doFXBindings() {
        organizationIdProperty().bind(FXOrganizationId.organizationIdProperty());
    }
}
