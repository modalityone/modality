package one.modality.hotel.backoffice.activities.roomsetup.dialog;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Building;
import one.modality.base.shared.entities.BuildingZone;
import one.modality.hotel.backoffice.activities.roomsetup.RoomSetupI18nKeys;
import one.modality.hotel.backoffice.activities.roomsetup.util.UIComponentDecorators;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;

/**
 * Dialog for creating and editing building zones (locations).
 *
 * @author Claude Code
 */
public class BuildingZoneDialog implements DialogManager.ManagedDialog {

    private final DataSourceModel dataSourceModel;
    private final Building parentBuilding;
    private final BuildingZone existingZone;

    // Form fields
    private TextField nameField;

    private Runnable onSaveCallback;
    private final ValidationSupport validationSupport = new ValidationSupport();

    // Track if form has changes
    private final BooleanProperty hasChanges = new SimpleBooleanProperty(false);
    private String initialName;

    public BuildingZoneDialog(DataSourceModel dataSourceModel, Building parentBuilding, BuildingZone existingZone) {
        this.dataSourceModel = dataSourceModel;
        this.parentBuilding = parentBuilding;
        this.existingZone = existingZone;
    }

    public Node buildView() {
        VBox container = new VBox();
        container.setSpacing(20);
        container.setPadding(new Insets(24));
        container.setMinWidth(400);
        container.getStyleClass().add(UIComponentDecorators.CSS_DIALOG_CONTAINER);

        // Header
        HBox header = createHeader();

        // Form
        VBox form = createForm();

        container.getChildren().addAll(header, form);

        // Initialize with existing data if editing
        if (existingZone != null) {
            populateForm();
        }

        // Set up change tracking and validation
        setupChangeTracking();
        setupValidation();

        return container;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(12);
        header.setPadding(new Insets(0, 0, 16, 0));
        header.getStyleClass().add(UIComponentDecorators.CSS_DIALOG_HEADER);

        Label iconLabel = new Label(existingZone != null ? "✏️" : "🚪");

        VBox titleBox = new VBox();
        titleBox.setSpacing(4);
        Label titleLabel = I18nControls.newLabel(existingZone != null ? RoomSetupI18nKeys.ZoneDialogEditTitle : RoomSetupI18nKeys.ZoneDialogAddTitle);
        titleLabel.getStyleClass().add(UIComponentDecorators.CSS_TITLE);

        String buildingName = parentBuilding != null ? parentBuilding.getName() : I18n.getI18nText(RoomSetupI18nKeys.UnknownBuilding);
        Label subtitleLabel = new Label(I18n.getI18nText(RoomSetupI18nKeys.ZoneDialogInBuilding, buildingName));
        subtitleLabel.getStyleClass().add(UIComponentDecorators.CSS_SUBTITLE);

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        header.getChildren().addAll(iconLabel, titleBox);

        return header;
    }

    private VBox createForm() {
        VBox form = new VBox();
        form.setSpacing(16);

        // Name field
        VBox nameSection = new VBox();
        nameSection.setSpacing(8);
        Label nameLabel = I18nControls.newLabel(RoomSetupI18nKeys.FieldName);
        nameLabel.getStyleClass().add(UIComponentDecorators.CSS_FIELD_LABEL);
        nameField = new TextField();
        nameField.setPromptText(I18n.getI18nText(RoomSetupI18nKeys.ZoneNamePlaceholder));
        nameField.getStyleClass().add(UIComponentDecorators.CSS_TEXT_FIELD);
        nameField.setPadding(new Insets(12, 14, 12, 14));
        nameSection.getChildren().addAll(nameLabel, nameField);

        form.getChildren().add(nameSection);

        return form;
    }

    private void populateForm() {
        if (existingZone == null) return;

        nameField.setText(existingZone.getName());
        // Icon is not stored in the entity currently
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public BooleanProperty hasChangesProperty() {
        return hasChanges;
    }

    private void setupChangeTracking() {
        // Store initial value
        initialName = existingZone != null ? (existingZone.getName() != null ? existingZone.getName() : "") : "";

        // Listen to form field changes
        nameField.textProperty().addListener((obs, oldVal, newVal) -> checkForChanges());

        // Initial check
        checkForChanges();
    }

    private void checkForChanges() {
        String currentName = nameField.getText() != null ? nameField.getText() : "";
        boolean changed = !currentName.equals(initialName);
        hasChanges.set(changed);
    }

    private void setupValidation() {
        validationSupport.addRequiredInput(nameField);
    }

    public boolean shouldSave() {
        // Always save when OK is clicked (internal buttons were removed, framework provides OK/Cancel)
        return true;
    }

    public boolean shouldDelete() {
        // Delete functionality removed for now - would need a separate UI element
        return false;
    }

    public void save(DialogCallback dialogCallback) {
        Console.log("BuildingZoneDialog.save() called");

        // Validate required fields first
        if (!validationSupport.isValid()) {
            return;
        }

        try {
            String name = nameField.getText();

            if (parentBuilding == null) {
                Console.log("Parent building is required");
                return;
            }

            UpdateStore updateStore = UpdateStore.create(dataSourceModel);

            BuildingZone zone;
            if (existingZone != null) {
                zone = updateStore.updateEntity(existingZone);
            } else {
                zone = updateStore.insertEntity(BuildingZone.class);
                zone.setBuilding(parentBuilding);
            }

            zone.setName(name.trim());
            // Note: Icon could be stored in a description field or separate column if needed

            updateStore.submitChanges()
                    .onFailure(error -> Console.log("Error saving location: " + error.getMessage()))
                    .onSuccess(result -> {
                        Console.log("Location saved successfully");
                        if (onSaveCallback != null) {
                            onSaveCallback.run();
                        }
                        dialogCallback.closeDialog();
                    });

        } catch (Exception e) {
            Console.log("Error in save: " + e.getMessage());
        }
    }

    public void delete(DialogCallback dialogCallback) {
        // IMPORTANT: Delete functionality is disabled to preserve referential integrity.
        //
        // Reason: The BuildingZone table does not have a 'removed' column in the database schema,
        // and zones may be referenced by Resources (rooms). Hard deletion would violate
        // CLAUDE.md guidelines: "NEVER hard-delete entities - Always use soft-delete".
        //
        // To enable deletion in the future:
        // 1. Add 'removed' column to the building_zone table (database migration)
        // 2. Add isRemoved()/setRemoved() methods to BuildingZone interface
        // 3. Update queries to filter out removed zones (where removed is null or removed=false)
        // 4. Change this method to set removed=true instead of deleteEntity()
        //
        // For now, zones can only be renamed, not deleted.
        dialogCallback.closeDialog();
    }
}
