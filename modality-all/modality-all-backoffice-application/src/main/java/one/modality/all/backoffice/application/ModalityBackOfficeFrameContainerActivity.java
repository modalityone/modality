package one.modality.all.backoffice.application;

import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.scene.control.Button;
import one.modality.base.client.application.ModalityClientFrameContainerActivity;
import one.modality.base.shared.entities.Organization;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

/**
 * @author Bruno Salmon
 */
public class ModalityBackOfficeFrameContainerActivity extends ModalityClientFrameContainerActivity {

    @Override
    protected Button createContainerHeaderCenterItem() {
        EntityButtonSelector<Organization> organizationSelector = new EntityButtonSelector<>(
                "{class: 'Organization', alias: 'o', where: 'exists(select Event where organization=o)'}",
                this, containerPane, getDataSourceModel()
        );
        FXOrganization.organizationProperty().bind(organizationSelector.selectedItemProperty());
        return organizationSelector.getButton();
    }
}
