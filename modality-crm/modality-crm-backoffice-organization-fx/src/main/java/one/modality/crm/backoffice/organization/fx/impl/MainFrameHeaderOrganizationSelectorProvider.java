package one.modality.crm.backoffice.organization.fx.impl;

import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import one.modality.base.backoffice.application.MainFrameHeaderNodeProvider;
import one.modality.base.shared.entities.Organization;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

/**
 * @author Bruno Salmon
 */
public class MainFrameHeaderOrganizationSelectorProvider implements MainFrameHeaderNodeProvider {

    private EntityButtonSelector<Organization> organizationSelector;

    @Override
    public String getName() {
        return "organizationSelector";
    }

    @Override
    public Node getHeaderNode(ButtonFactoryMixin buttonFactory, Pane frameContainer, DataSourceModel dataSourceModel) {
        if (organizationSelector == null) {
            organizationSelector = new EntityButtonSelector<>(
                    "{class: 'Organization', alias: 'o', where: 'exists(select Event where organization=o)'}",
                    buttonFactory, frameContainer, dataSourceModel
            );
            // Doing a bidirectional binding with FXOrganization
            organizationSelector.selectedItemProperty().bindBidirectional(FXOrganization.organizationProperty());
        }
        Button button = organizationSelector.getButton();
        return button;
    }
}
