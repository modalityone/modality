package one.modality.crm.backoffice.activities.customers;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import one.modality.base.client.entities.filters.FilterButtonSelectorFactoryMixin;

import static one.modality.crm.backoffice.activities.customers.CustomersI18nKeys.*;

/**
 * Customers activity - manages the Person table with comprehensive UX.
 * Provides table view with inline editing for customer/person management.
 *
 * @author David Hello
 * @author Bruno Salmon
 * @author Claude Code
 */
final class CustomersActivity extends ViewDomainActivityBase implements FilterButtonSelectorFactoryMixin {

    private final CustomersPresentationModel pm = new CustomersPresentationModel();
    private CustomersView customersView;
    private CustomersFilterSearchBar filterSearchBar;


    @Override
    public Node buildUi() {
        // Create main container
        BorderPane container = new BorderPane();
        container.setId("customers");

        // Create header with FilterSearchBar
        VBox header = createHeader();

        // Create the FilterSearchBar and add it to the header
        filterSearchBar = new CustomersFilterSearchBar(this, "customers", "Person", container, pm);
        header.getChildren().add(filterSearchBar.buildUi());

        container.setTop(header);

        // Create the main view
        customersView = new CustomersView(pm, this);
        container.setCenter(customersView.getView());

        return container;
    }

    private VBox createHeader() {
        VBox header = new VBox(4);
        header.setPadding(new Insets(20, 24, 20, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("page-header");

        Label title = I18nControls.newLabel(PageTitle);
        title.getStyleClass().add("page-title");

        Label subtitle = I18nControls.newLabel(PageSubtitle);
        subtitle.getStyleClass().add("page-subtitle");

        header.getChildren().addAll(title, subtitle);

        return header;
    }

    @Override
    public void onResume() {
        super.onResume();
        filterSearchBar.onResume(); // activate search text focus on activity resume
        pm.setActive(true);
        if (customersView != null) {
            customersView.setActive(true);
        }
    }

    @Override
    public void onPause() {
        pm.setActive(false);
        if (customersView != null) {
            customersView.setActive(false);
        }
        super.onPause();
    }

    @Override
    protected void refreshDataOnActive() {
        if (customersView != null) {
            customersView.refreshData();
        }
    }
}
