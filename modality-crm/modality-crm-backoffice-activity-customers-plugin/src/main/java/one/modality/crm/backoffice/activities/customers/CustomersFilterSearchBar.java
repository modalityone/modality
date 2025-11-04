package one.modality.crm.backoffice.activities.customers;

import dev.webfx.extras.i18n.I18n;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import one.modality.base.client.entities.filters.FilterButtonSelectorFactoryMixin;
import one.modality.base.client.entities.filters.FilterSearchBar;

import static one.modality.crm.backoffice.activities.customers.CustomersI18nKeys.*;

/**
 * Custom FilterSearchBar wrapper for Customers activity that adds an account type filter.
 *
 * @author Claude Code
 */
public final class CustomersFilterSearchBar {

    private final FilterSearchBar filterSearchBar;
    private final ToggleGroup accountTypeToggleGroup;
    private final ToggleButton allAccountsButton;
    private final ToggleButton frontofficeButton;
    private final ToggleButton backofficeButton;

    private final ToggleGroup activeStatusToggleGroup;
    private final ToggleButton allStatusButton;
    private final ToggleButton activeButton;
    private final ToggleButton inactiveButton;

    public CustomersFilterSearchBar(FilterButtonSelectorFactoryMixin mixin, String activityName, String domainClassId, javafx.scene.layout.Pane parent, Object pm) {
        // Create the base FilterSearchBar
        filterSearchBar = new FilterSearchBar(mixin, activityName, domainClassId, parent, pm);

        // Create toggle group for account type filter
        accountTypeToggleGroup = new ToggleGroup();

        allAccountsButton = new ToggleButton(I18n.getI18nText(AccountTypeFilterAll));
        allAccountsButton.setToggleGroup(accountTypeToggleGroup);
        allAccountsButton.getStyleClass().add("account-type-toggle");
        allAccountsButton.setSelected(true);

        frontofficeButton = new ToggleButton(I18n.getI18nText(AccountTypeFilterFrontoffice));
        frontofficeButton.setToggleGroup(accountTypeToggleGroup);
        frontofficeButton.getStyleClass().add("account-type-toggle");

        backofficeButton = new ToggleButton(I18n.getI18nText(AccountTypeFilterBackoffice));
        backofficeButton.setToggleGroup(accountTypeToggleGroup);
        backofficeButton.getStyleClass().add("account-type-toggle");

        // Bind account type toggle selection to presentation model
        if (pm instanceof HasAccountTypeFilterProperty) {
            HasAccountTypeFilterProperty filterPm = (HasAccountTypeFilterProperty) pm;
            accountTypeToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == allAccountsButton) {
                    filterPm.setAccountTypeFilter(null);
                } else if (newVal == frontofficeButton) {
                    filterPm.setAccountTypeFilter("frontoffice");
                } else if (newVal == backofficeButton) {
                    filterPm.setAccountTypeFilter("backoffice");
                }
            });
        }

        // Create toggle group for active status filter
        activeStatusToggleGroup = new ToggleGroup();

        allStatusButton = new ToggleButton(I18n.getI18nText(ActiveStatusFilterAll));
        allStatusButton.setToggleGroup(activeStatusToggleGroup);
        allStatusButton.getStyleClass().add("account-type-toggle");

        activeButton = new ToggleButton(I18n.getI18nText(ActiveStatusFilterActive));
        activeButton.setToggleGroup(activeStatusToggleGroup);
        activeButton.getStyleClass().add("account-type-toggle");
        activeButton.setSelected(true);

        inactiveButton = new ToggleButton(I18n.getI18nText(ActiveStatusFilterInactive));
        inactiveButton.setToggleGroup(activeStatusToggleGroup);
        inactiveButton.getStyleClass().add("account-type-toggle");

        // Bind active status toggle selection to presentation model
        if (pm instanceof HasActiveStatusFilterProperty) {
            HasActiveStatusFilterProperty statusPm = (HasActiveStatusFilterProperty) pm;
            activeStatusToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == allStatusButton) {
                    statusPm.setActiveStatusFilter(null);
                } else if (newVal == activeButton) {
                    statusPm.setActiveStatusFilter("active");
                } else if (newVal == inactiveButton) {
                    statusPm.setActiveStatusFilter("inactive");
                }
            });
        }
    }

    public HBox buildUi() {
        HBox bar = filterSearchBar.buildUi();
        ObservableList<Node> children = bar.getChildren();

        // Create container for account type toggle buttons with spacing between them
        HBox accountTypeContainer = new HBox();
        accountTypeContainer.setSpacing(8); // Spacing between buttons
        accountTypeContainer.getStyleClass().add("account-type-toggle-container");
        accountTypeContainer.getChildren().addAll(allAccountsButton, frontofficeButton, backofficeButton);

        // Create a vertical separator between the two filter groups
        Separator separator = new Separator(Orientation.VERTICAL);
        separator.getStyleClass().add("filter-group-separator");
        separator.setMaxHeight(30); // Height of separator

        // Create spacer regions around the separator for additional spacing
        Region leftSpacer = new Region();
        leftSpacer.setMinWidth(12);
        leftSpacer.setPrefWidth(12);
        leftSpacer.setMaxWidth(12);

        Region rightSpacer = new Region();
        rightSpacer.setMinWidth(12);
        rightSpacer.setPrefWidth(12);
        rightSpacer.setMaxWidth(12);

        // Create container for status toggle buttons with spacing between them
        HBox statusContainer = new HBox();
        statusContainer.setSpacing(8); // Spacing between buttons
        statusContainer.getStyleClass().add("account-type-toggle-container");
        statusContainer.getChildren().addAll(allStatusButton, activeButton, inactiveButton);

        // Add both filter groups after the search box with visual separation
        children.add(accountTypeContainer);
        children.add(leftSpacer);
        children.add(separator);
        children.add(rightSpacer);
        children.add(statusContainer);

        return bar;
    }

    public void onResume() {
        filterSearchBar.onResume();
    }
}
