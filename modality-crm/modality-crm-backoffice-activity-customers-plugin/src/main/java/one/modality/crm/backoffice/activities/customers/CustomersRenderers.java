package one.modality.crm.backoffice.activities.customers;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.stack.orm.entity.Entity;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.FrontendAccount;
import one.modality.base.shared.entities.Person;

/**
 * Custom cell renderers for the Customers table.
 * Provides badges, icons, and action buttons for customer data visualization.
 *
 * @author David Hello
 * @author Claude Code
 */
final class CustomersRenderers {

    private static CustomersView view;

    static void setView(CustomersView customersView) {
        view = customersView;
    }

    static void registerRenderers() {
        // ID Renderer - Display the ID as an integer
        ValueRendererRegistry.registerValueRenderer("customerId", (value, context) -> {
            Person person = (Person) value;
            Label idLabel = new Label(person.getId().getPrimaryKey().toString());
            idLabel.getStyleClass().add("customer-id");
            return idLabel;
        });

        // Account Type Badge Renderer
        ValueRendererRegistry.registerValueRenderer("customerAccountType", (value, context) -> {
            Person person = (Person) value;

            // Get the frontendAccount - it should be loaded now
            FrontendAccount account = person.getFrontendAccount();

            // Check if person is owner (this is a flag on the Person entity)
            boolean isOwner = Boolean.TRUE.equals(person.isOwner());

            // Check if person uses someone else's account
            Person accountPerson = person.getAccountPerson();

            Label badge = new Label();

            if (account != null) {
                // Person has a frontend account
                if (Boolean.TRUE.equals(account.isBackoffice())) {
                    // It's a backoffice account
                    badge.setText("BACKOFFICE");
                    ModalityStyle.badgeLightInfo(badge);
                } else if (isOwner) {
                    // It's a frontoffice account and this person is the owner
                    badge.setText("FRONTOFFICE (OWNER)");
                    ModalityStyle.badgeLightWarning(badge);
                } else {
                    // It's a frontoffice account but not owner (shouldn't happen normally)
                    badge.setText("FRONTOFFICE");
                    ModalityStyle.badgeLightPurple(badge);
                }
            } else if (accountPerson != null) {
                // Person doesn't have their own account but uses someone else's
                badge.setText("LINKED");
                ModalityStyle.badgeLightInfo(badge);
            } else {
                // No account at all
                badge.setText("NONE");
                ModalityStyle.badgeLightGray(badge);
            }

            HBox container = new HBox(badge);
            container.setAlignment(Pos.CENTER);
            return container;
        });

        // Status Renderer - Shows Active/Disabled/Removed
        ValueRendererRegistry.registerValueRenderer("customerStatus", (value, context) -> {
            Person person = (Person) value;
            FrontendAccount account = person.getFrontendAccount();

            Label badge = new Label();

            // Check disabled first, then removed
            if (account != null && Boolean.TRUE.equals(account.isDisabled())) {
                badge.setText("DISABLED");
                ModalityStyle.badgeLightWarning(badge);
            } else if (Boolean.TRUE.equals(person.isRemoved())) {
                badge.setText("REMOVED");
                ModalityStyle.badgeLightDanger(badge);
            } else {
                badge.setText("ACTIVE");
                ModalityStyle.badgeLightSuccess(badge);
            }

            HBox container = new HBox(badge);
            container.setAlignment(Pos.CENTER);
            return container;
        });

        // Roles/Links Renderer
        ValueRendererRegistry.registerValueRenderer("customerRolesLinks", (value, context) -> {
            Person person = (Person) value;
            FrontendAccount account = person.getFrontendAccount();
            boolean isOwner = Boolean.TRUE.equals(person.isOwner());
            Person accountPerson = person.getAccountPerson();

            HBox container = new HBox(4);
            container.setAlignment(Pos.CENTER_LEFT);

            if (account != null && Boolean.TRUE.equals(account.isBackoffice())) {
                // Show backoffice roles (placeholder - would need to query authorization roles)
                // For now, just show a generic badge
                Label roleBadge = new Label("BACKOFFICE");
                ModalityStyle.badgeLightInfo(roleBadge);
                container.getChildren().add(roleBadge);
            } else if (isOwner) {
                // Show linked accounts count
                int linkedCount = view != null ? view.getLinkedAccountsCount(person) : 0;
                if(linkedCount>0) {
                    Text icon = new Text("🔗");
                    icon.getStyleClass().add("role-icon");
                    Label linkInfo = new Label("Linked accounts: " + linkedCount);
                    linkInfo.getStyleClass().add("role-link-text");
                    container.getChildren().addAll(icon, linkInfo);
                }
            } else if (accountPerson != null) {
                // Show owner info
                Text icon = new Text("✓");
                icon.getStyleClass().add("role-icon-success");
                String ownerName = accountPerson.getFullName();
                Object id = accountPerson.getPrimaryKey();
                Label linkInfo = new Label("→ "+ id +" - " + ownerName);
                linkInfo.getStyleClass().add("role-link-text");
                container.getChildren().addAll(icon, linkInfo);
            }

            return container;
        });
    }
}
