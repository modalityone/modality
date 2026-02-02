package one.modality.crm.backoffice.activities.customers;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.shared.entities.FrontendAccount;
import one.modality.base.shared.entities.Person;

import static one.modality.crm.backoffice.activities.customers.CustomersCssSelectors.*;
import static one.modality.crm.backoffice.activities.customers.CustomersI18nKeys.*;

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
            idLabel.getStyleClass().add(customer_id);
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
                    badge.setText(I18n.getI18nText(BadgeBackoffice));
                    Bootstrap.warningBadge(badge);
                } else if (isOwner) {
                    // It's a frontoffice account and this person is the owner
                    badge.setText(I18n.getI18nText(BadgeFrontofficeOwner));
                    Bootstrap.successBadge(badge);
                } else {
                    // It's a frontoffice account but not owner (shouldn't happen normally)
                    badge.setText(I18n.getI18nText(BadgeFrontoffice));
                    Bootstrap.primaryBadge(badge);
                }
            } else if (accountPerson != null) {
                // Person doesn't have their own account but uses someone else's
                badge.setText(I18n.getI18nText(BadgeLinked));
                ModalityStyle.badgeLightInfo(badge);
            } else {
                // No account at all
                badge.setText(I18n.getI18nText(BadgeNone));
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
                badge.setText(I18n.getI18nText(BadgeDisabled));
                Bootstrap.dangerBadge(badge);
            } else if (Boolean.TRUE.equals(person.isRemoved())) {
                badge.setText(I18n.getI18nText(BadgeRemoved));
                Bootstrap.secondaryBadge(badge);
            } else {
                badge.setText(I18n.getI18nText(BadgeActive));
                ModalityStyle.badgeLightSuccess(badge);
            }

            HBox container = new HBox(badge);
            container.setAlignment(Pos.CENTER);
            return container;
        });

        // Roles/Members Renderer
        ValueRendererRegistry.registerValueRenderer("customerRolesLinks", (value, context) -> {
            Person person = (Person) value;
            FrontendAccount account = person.getFrontendAccount();
            boolean isOwner = Boolean.TRUE.equals(person.isOwner());

            HBox container = new HBox(4);
            container.setAlignment(Pos.CENTER_LEFT);

            if (account != null && Boolean.TRUE.equals(account.isBackoffice())) {
                // Backoffice accounts don't show anything in the Members column
                // Empty container
            } else if (isOwner) {
                // Show members count
                int membersCount = view != null ? view.getMembersCount(person) : 0;
                if(membersCount>0) {
                    Text icon = new Text("🔗");
                    icon.getStyleClass().add(role_icon);
                    Label linkInfo = new Label();
                    linkInfo.setText(I18n.getI18nText(MembersCountText, membersCount));
                    linkInfo.getStyleClass().add(role_link_text);
                    container.getChildren().addAll(icon, linkInfo);
                }
            } else if (account != null) {
                // Non-owner with a frontend account - find and show the owner
                Person owner = view != null ? view.getOwnerForFrontendAccount(account.getPrimaryKey()) : null;
                if (owner != null) {
                    Text icon = new Text("✓");
                    icon.getStyleClass().add(role_icon_success);
                    String ownerName = owner.getFullName();
                    Object id = owner.getPrimaryKey();
                    Label linkInfo = new Label();
                    linkInfo.setText(I18n.getI18nText(LinkedToOwnerText, id, ownerName));
                    linkInfo.getStyleClass().add(role_link_text);
                    container.getChildren().addAll(icon, linkInfo);
                }
            }

            return container;
        });
    }
}
