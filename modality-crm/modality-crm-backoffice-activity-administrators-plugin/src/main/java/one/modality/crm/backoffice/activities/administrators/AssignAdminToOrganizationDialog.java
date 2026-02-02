package one.modality.crm.backoffice.activities.administrators;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.domainmodel.functions.AbcNames;
import one.modality.base.shared.entities.AuthorizationOrganizationAdmin;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.Person;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dialog for managing admins of an organization.
 * Displays current admins as badges with ability to add/remove.
 *
 * @author Claude Code
 */
final class AssignAdminToOrganizationDialog {

    /**
     * Shows the manage admins dialog.
     *
     * @param organization The organization to manage
     * @param admins       Current list of organization admins
     */
    public static void show(Organization organization, List<AuthorizationOrganizationAdmin> admins) {
        UpdateStore updateStore = UpdateStore.createAbove(organization.getStore());

        // Track selected admins
        Set<Object> selectedAdminIds = new HashSet<>();
        for (AuthorizationOrganizationAdmin admin : admins) {
            Person person = admin.getAdmin();
            if (person != null) {
                selectedAdminIds.add(person.getPrimaryKey());
            }
        }

        // Create dialog content
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setMinWidth(500);
        dialogContent.setPrefWidth(600);
        dialogContent.setMaxWidth(700);

        // Title
        Label titleLabel = Bootstrap.strong(I18nControls.newLabel(AdministratorsI18nKeys.ManageManagers));
        titleLabel.getStyleClass().add("modal-title");

        // Organization name
        Label orgLabel = Bootstrap.strong(new Label(organization.getName()));
        orgLabel.getStyleClass().add("organization-info-name");

        // Current admins section
        VBox adminsSection = new VBox(8);
        Label currentAdminsLabel = I18nControls.newLabel(AdministratorsI18nKeys.CurrentManagers);
        currentAdminsLabel.getStyleClass().add("form-label");

        // Selected admins display panel
        VBox selectedPanel = new VBox(8);
        selectedPanel.setPadding(new Insets(12));
        selectedPanel.setMaxWidth(Double.MAX_VALUE);
        selectedPanel.getStyleClass().add("admin-selected-panel");

        // Selected admins header with count
        Label selectedCountLabel = new Label();
        selectedCountLabel.getStyleClass().add("admin-count-label");

        // Flow pane for admin chips
        FlowPane adminsChipsPane = new FlowPane();
        adminsChipsPane.setHgap(6);
        adminsChipsPane.setVgap(6);
        adminsChipsPane.setMaxWidth(Double.MAX_VALUE);

        selectedPanel.getChildren().addAll(selectedCountLabel, adminsChipsPane);
        adminsSection.getChildren().addAll(currentAdminsLabel, selectedPanel);

        // Track admins to add/remove
        List<Person> adminsToAdd = new ArrayList<>();
        Set<Object> adminsToRemove = new HashSet<>();

        // Create a BooleanBinding that checks if there are no changes
        BooleanBinding hasNoChangesBinding = new BooleanBinding() {
            @Override
            protected boolean computeValue() {
                return adminsToAdd.isEmpty() && adminsToRemove.isEmpty();
            }
        };

        // Method to update the admins display (using array to allow self-reference)
        Runnable[] updateAdminsDisplayHolder = new Runnable[1];
        updateAdminsDisplayHolder[0] = () -> {
            // Update count
            int totalCount = selectedAdminIds.size() + adminsToAdd.size() - adminsToRemove.size();
            String countText = I18n.getI18nText(totalCount == 1 ? AdministratorsI18nKeys.AdminSingular : AdministratorsI18nKeys.AdminPlural);
            selectedCountLabel.setText(countText + " (" + totalCount + ")");

            // Clear and rebuild chips
            adminsChipsPane.getChildren().clear();

            if (totalCount == 0) {
                Label empty = I18nControls.newLabel(AdministratorsI18nKeys.None);
                empty.getStyleClass().add("admin-text-italic");
                adminsChipsPane.getChildren().add(empty);
            } else {
                // Show existing admins (not marked for removal)
                for (AuthorizationOrganizationAdmin admin : admins) {
                    Person person = admin.getAdmin();
                    if (person != null && !adminsToRemove.contains(person.getPrimaryKey())) {
                        HBox chip = createAdminChip(person, () -> {
                            adminsToRemove.add(person.getPrimaryKey());
                            Platform.runLater(() -> {
                                updateAdminsDisplayHolder[0].run();
                                hasNoChangesBinding.invalidate();
                            });
                        });
                        adminsChipsPane.getChildren().add(chip);
                    }
                }

                // Show newly added admins
                for (Person admin : adminsToAdd) {
                    HBox chip = createAdminChip(admin, () -> {
                        adminsToAdd.remove(admin);
                        Platform.runLater(() -> {
                            updateAdminsDisplayHolder[0].run();
                            hasNoChangesBinding.invalidate();
                        });
                    });
                    adminsChipsPane.getChildren().add(chip);
                }
            }
        };
        Runnable updateAdminsDisplay = updateAdminsDisplayHolder[0];

        // Initial display
        updateAdminsDisplay.run();

        // Add admin section
        VBox addAdminSection = new VBox(8);
        Label addAdminLabel = I18nControls.newLabel(AdministratorsI18nKeys.SelectManager);
        addAdminLabel.getStyleClass().add("form-label");

        // Create EntityButtonSelector for Person
        EntityButtonSelector<Person> adminSelector = new EntityButtonSelector<Person>( // language=JSON5
            "{class: 'Person', alias: 'p', columns: [{expression: '[firstName,lastName,`(` + email + `)`]'}], where: 'owner and !removed and frontendAccount.(backoffice and !disabled)', orderBy: 'firstName,lastName'}",
            new ButtonFactoryMixin() {
            }, FXMainFrameDialogArea::getDialogArea, organization.getStore().getDataSourceModel()
        ) {
            @Override
            protected void setSearchParameters(String search, EntityStore store) {
                super.setSearchParameters(search, store);
                store.setParameterValue("abcSearchLike", AbcNames.evaluate(search, true));
            }
        }.setSearchCondition("abcNames(p?.fullName) like ?abcSearchLike or lower(p?.email) like ?searchEmailLike");

        Button adminButton = adminSelector.getButton();
        adminButton.setMaxWidth(Double.MAX_VALUE);
        adminButton.setText(I18n.getI18nText(AdministratorsI18nKeys.SelectManager) + "...");

        // Listen for person selection
        adminSelector.selectedItemProperty().addListener((obs, oldVal, selectedPerson) -> {
            if (selectedPerson != null) {
                // Check if admin is already in the list
                boolean alreadyExists = selectedAdminIds.contains(selectedPerson.getPrimaryKey()) ||
                    adminsToAdd.stream().anyMatch(m -> m.getPrimaryKey().equals(selectedPerson.getPrimaryKey()));

                if (!alreadyExists && !adminsToRemove.contains(selectedPerson.getPrimaryKey())) {
                    // Add to list
                    adminsToAdd.add(selectedPerson);
                    Platform.runLater(() -> {
                        updateAdminsDisplay.run();
                        hasNoChangesBinding.invalidate();
                    });
                }

                // Reset selection
                Platform.runLater(() -> {
                    adminSelector.setSelectedItem(null);
                    adminButton.setText(I18n.getI18nText(AdministratorsI18nKeys.SelectManager) + "...");
                });
            }
        });

        // Update button text when selection changes
        adminSelector.selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Platform.runLater(() -> adminButton.setText(newVal.getFirstName() + " " + newVal.getLastName() + " (" + newVal.getEmail() + ")"));
            }
        });

        addAdminSection.getChildren().addAll(addAdminLabel, adminButton);

        // Footer buttons
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = Bootstrap.button(I18nControls.newButton(BaseI18nKeys.Cancel));
        Button saveButton = Bootstrap.successButton(I18nControls.newButton(BaseI18nKeys.SaveChanges));

        // Bind save button disable property to hasNoChangesBinding
        saveButton.disableProperty().bind(hasNoChangesBinding);

        footer.getChildren().addAll(cancelButton, saveButton);

        // Add all to dialog
        dialogContent.getChildren().addAll(
            titleLabel,
            orgLabel,
            adminsSection,
            addAdminSection,
            footer
        );

        // Show dialog
        BorderPane dialogPane = new BorderPane(dialogContent);
        dialogPane.getStyleClass().add("modal-dialog-pane");
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(
            dialogPane, FXMainFrameDialogArea.getDialogArea()
        );

        // Button actions
        cancelButton.setOnAction(e -> dialogCallback.closeDialog());

        saveButton.setOnAction(e -> {
            // Remove admins
            for (AuthorizationOrganizationAdmin admin : admins) {
                Person person = admin.getAdmin();
                if (person != null && adminsToRemove.contains(person.getPrimaryKey())) {
                    updateStore.deleteEntity(admin);
                }
            }

            // Add new admins
            for (Person admin : adminsToAdd) {
                AuthorizationOrganizationAdmin orgAdmin = updateStore.insertEntity(AuthorizationOrganizationAdmin.class);
                orgAdmin.setOrganization(organization);
                orgAdmin.setAdmin(admin);
            }

            // Submit all changes in one transaction
            updateStore.submitChanges()
                .onSuccess(result -> dialogCallback.closeDialog())
                .onFailure(error -> Platform.runLater(() -> showErrorDialog(error.getMessage())));
        });
    }

    private static HBox createAdminChip(Person person, Runnable onRemove) {
        HBox chipContainer = new HBox(4);
        chipContainer.setAlignment(Pos.CENTER);
        chipContainer.setPadding(new Insets(6, 8, 6, 12));
        chipContainer.getStyleClass().add("admin-chip");

        // Admin name label
        String adminName = person.getFirstName() + " " + person.getLastName();
        Label nameLabel = new Label(adminName);
        nameLabel.getStyleClass().add("admin-chip-label");

        // Remove button (×)
        Label removeBtn = new Label("×");
        removeBtn.getStyleClass().add("admin-chip-remove");
        removeBtn.setOnMouseClicked(e -> onRemove.run());

        chipContainer.getChildren().addAll(nameLabel, removeBtn);
        return chipContainer;
    }

    private static void showErrorDialog(String content) {
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setMinWidth(350);
        dialogContent.setPrefWidth(500);
        dialogContent.setMaxWidth(700);

        Label titleLabel = Bootstrap.strong(I18nControls.newLabel(BaseI18nKeys.Error));
        titleLabel.getStyleClass().add("error-dialog-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        Label headerLabel = I18nControls.newLabel(AdministratorsI18nKeys.FailedToAssignAdmin);
        headerLabel.setWrapText(true);
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        headerLabel.getStyleClass().add("error-dialog-header");

        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(Double.MAX_VALUE);
        contentLabel.getStyleClass().add("error-dialog-content");

        dialogContent.getChildren().addAll(titleLabel, headerLabel, contentLabel);

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button okButton = Bootstrap.dangerButton(I18nControls.newButton(BaseI18nKeys.OK));

        footer.getChildren().add(okButton);
        dialogContent.getChildren().add(footer);

        BorderPane dialogPane = new BorderPane(dialogContent);
        dialogPane.getStyleClass().add("modal-dialog-pane");
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());

        okButton.setOnAction(e -> dialogCallback.closeDialog());
    }
}
