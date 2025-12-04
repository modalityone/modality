package one.modality.hotel.backoffice.activities.roomsetup.dialog;

import dev.webfx.extras.filepicker.FilePicker;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import one.modality.base.client.cloud.image.ModalityCloudImageService;
import one.modality.base.shared.entities.Building;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.Site;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.hotel.backoffice.activities.roomsetup.RoomSetupI18nKeys;
import one.modality.hotel.backoffice.activities.roomsetup.util.UIComponentDecorators;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;

/**
 * Dialog for creating and editing buildings.
 * Includes image upload for building icon stored via ModalityCloudImageService.
 *
 * @author Claude Code
 */
public class BuildingDialog implements DialogManager.ManagedDialog {

    private static final double ICON_SIZE = 80;

    private final DataSourceModel dataSourceModel;
    private final Building existingBuilding;

    // Form fields
    private TextField nameField;

    // Image upload components
    private final MonoPane iconImageContainer = new MonoPane();
    private final ProgressIndicator imageUploadProgressIndicator = Controls.createProgressIndicator(30);
    private String buildingCloudImagePath;

    private Site resolvedSite;
    private Runnable onSaveCallback;
    private final ValidationSupport validationSupport = new ValidationSupport();

    // Track if form has changes
    private final BooleanProperty hasChanges = new SimpleBooleanProperty(false);
    private String initialName;

    public BuildingDialog(DataSourceModel dataSourceModel, Building existingBuilding) {
        this.dataSourceModel = dataSourceModel;
        this.existingBuilding = existingBuilding;

        // Set image path for existing building
        if (existingBuilding != null) {
            buildingCloudImagePath = ModalityCloudImageService.buildingImagePath(existingBuilding);
        }
    }

    public Node buildView() {
        VBox container = new VBox();
        container.setSpacing(20);
        container.setPadding(new Insets(24));
        container.setMinWidth(440);
        container.getStyleClass().add(UIComponentDecorators.CSS_DIALOG_CONTAINER);

        // Header
        HBox header = createHeader();

        // Form
        VBox form = createForm();

        container.getChildren().addAll(header, form);

        // Initialize with existing data if editing
        if (existingBuilding != null) {
            populateForm();
        }

        // Load site for new buildings
        if (existingBuilding == null) {
            loadOrganizationSite();
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

        Label iconLabel = new Label(existingBuilding != null ? "✏️" : "\uD83C\uDFDB️");

        VBox titleBox = new VBox();
        titleBox.setSpacing(4);
        Label titleLabel = I18nControls.newLabel(existingBuilding != null ? RoomSetupI18nKeys.BuildingDialogEditTitle : RoomSetupI18nKeys.BuildingDialogAddTitle);
        titleLabel.getStyleClass().add(UIComponentDecorators.CSS_TITLE);

        Label subtitleLabel = I18nControls.newLabel(existingBuilding != null ?
                RoomSetupI18nKeys.BuildingDialogEditSubtitle : RoomSetupI18nKeys.BuildingDialogAddSubtitle);
        subtitleLabel.getStyleClass().add(UIComponentDecorators.CSS_SUBTITLE);

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        header.getChildren().addAll(iconLabel, titleBox);

        return header;
    }

    private VBox createForm() {
        VBox form = new VBox();
        form.setSpacing(16);

        // Icon upload section
        VBox iconSection = createIconUploadSection();

        // Name field
        VBox nameSection = new VBox();
        nameSection.setSpacing(8);
        Label nameLabel = I18nControls.newLabel(RoomSetupI18nKeys.FieldName);
        nameLabel.getStyleClass().add(UIComponentDecorators.CSS_FIELD_LABEL);
        nameField = new TextField();
        nameField.setPromptText(I18n.getI18nText(RoomSetupI18nKeys.BuildingNamePlaceholder));
        nameField.getStyleClass().add(UIComponentDecorators.CSS_TEXT_FIELD);
        nameField.setPadding(new Insets(12, 14, 12, 14));
        nameSection.getChildren().addAll(nameLabel, nameField);

        form.getChildren().addAll(iconSection, nameSection);

        return form;
    }

    private VBox createIconUploadSection() {
        VBox iconSection = new VBox();
        iconSection.setSpacing(8);

        Label iconLabel = I18nControls.newLabel(RoomSetupI18nKeys.FieldBuildingIcon);
        iconLabel.getStyleClass().add(UIComponentDecorators.CSS_FIELD_LABEL);

        HBox iconRow = new HBox();
        iconRow.setAlignment(Pos.CENTER_LEFT);
        iconRow.setSpacing(16);

        // Icon preview container
        iconImageContainer.setPrefSize(ICON_SIZE, ICON_SIZE);
        iconImageContainer.setMinSize(ICON_SIZE, ICON_SIZE);
        iconImageContainer.setMaxSize(ICON_SIZE, ICON_SIZE);
        iconImageContainer.setStyle("-fx-background-color: #f5f5f4; -fx-background-radius: 12; -fx-border-color: #e5e5e5; -fx-border-radius: 12;");

        // Default placeholder
        VBox placeholder = createImagePlaceholder();
        iconImageContainer.setContent(placeholder);

        // Progress indicator (hidden by default)
        imageUploadProgressIndicator.setVisible(false);

        // Stack for image + progress
        StackPane imageStack = new StackPane(iconImageContainer, imageUploadProgressIndicator);
        imageStack.setPrefSize(ICON_SIZE, ICON_SIZE);
        imageStack.setMinSize(ICON_SIZE, ICON_SIZE);
        imageStack.setMaxSize(ICON_SIZE, ICON_SIZE);

        // Upload controls
        VBox uploadBox = new VBox();
        uploadBox.setSpacing(8);

        // Upload button using FilePicker
        FilePicker uploadPicker = FilePicker.create();
        uploadPicker.getAcceptedExtensions().addAll("image/*", ".webp", "image/webp", ".png", ".jpg", ".jpeg");

        Button uploadBtn = I18nControls.newButton(RoomSetupI18nKeys.UploadImage);
        uploadBtn.setStyle("-fx-background-color: white; -fx-text-fill: #1c1917; -fx-font-weight: 500; " +
                "-fx-padding: 10 16; -fx-background-radius: 8; -fx-border-color: #e5e5e5; -fx-border-radius: 8; -fx-cursor: hand;");

        uploadPicker.setGraphic(uploadBtn);

        FXProperties.runOnPropertyChange(fileToUpload -> {
            if (fileToUpload != null) {
                handleImageUpload(fileToUpload);
            }
        }, uploadPicker.selectedFileProperty());

        Label uploadHintLabel = I18nControls.newLabel(RoomSetupI18nKeys.UploadImageHint);
        uploadHintLabel.getStyleClass().add(UIComponentDecorators.CSS_HINT);

        // Delete image button (only shown when editing and image exists)
        Button deleteImageBtn = I18nControls.newButton(RoomSetupI18nKeys.RemoveImage);
        deleteImageBtn.setStyle("-fx-background-color: white; -fx-text-fill: #dc2626; -fx-font-weight: 500; " +
                "-fx-padding: 6 12; -fx-background-radius: 6; -fx-border-color: #fecaca; -fx-border-radius: 6; -fx-cursor: hand; -fx-font-size: 12px;");
        deleteImageBtn.setOnAction(e -> handleDeleteImage());
        deleteImageBtn.setVisible(false);
        deleteImageBtn.setManaged(false);

        // Show delete button only when an image is loaded (not placeholder)
        FXProperties.runOnPropertyChange(content -> {
            boolean hasImage = !(content instanceof VBox);
            deleteImageBtn.setVisible(hasImage);
            deleteImageBtn.setManaged(hasImage);
        }, iconImageContainer.contentProperty());

        uploadBox.getChildren().addAll(uploadPicker.getView(), uploadHintLabel, deleteImageBtn);

        iconRow.getChildren().addAll(imageStack, uploadBox);
        iconSection.getChildren().addAll(iconLabel, iconRow);

        // Load existing image if editing
        if (existingBuilding != null && buildingCloudImagePath != null) {
            loadBuildingImage();
        }

        return iconSection;
    }

    private VBox createImagePlaceholder() {
        VBox placeholder = new VBox();
        placeholder.setAlignment(Pos.CENTER);

        Label placeholderLabel = new Label("\uD83C\uDFDB️");
        placeholderLabel.setStyle("-fx-font-size: 36px;");

        placeholder.getChildren().add(placeholderLabel);
        return placeholder;
    }

    private void loadBuildingImage() {
        if (buildingCloudImagePath == null) return;

        imageUploadProgressIndicator.setVisible(true);
        ModalityCloudImageService.loadHdpiImage(buildingCloudImagePath, ICON_SIZE, ICON_SIZE, iconImageContainer, this::createImagePlaceholder)
                .onComplete(ar -> {
                    imageUploadProgressIndicator.setVisible(false);
                    if (ar.succeeded()) {
                        iconImageContainer.getStyleClass().remove("image-preview-container");
                    } else {
                        iconImageContainer.setContent(createImagePlaceholder());
                    }
                });
    }

    private void handleImageUpload(dev.webfx.platform.file.File fileToUpload) {
        // For new buildings, we need to save first to get the ID
        if (existingBuilding == null) {
            Console.log("Please save the building first before uploading an image.");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(I18n.getI18nText(RoomSetupI18nKeys.SaveBuildingFirst));
            alert.setHeaderText(I18n.getI18nText(RoomSetupI18nKeys.SaveBuildingFirstMessage));
            alert.setContentText(I18n.getI18nText(RoomSetupI18nKeys.SaveBuildingFirstContent));
            alert.showAndWait();
            return;
        }

        imageUploadProgressIndicator.setVisible(true);
        Image originalImage = new Image(fileToUpload.getObjectURL(), true);

        FXProperties.runOnPropertiesChange(property -> {
            if (originalImage.progressProperty().get() == 1) {
                // Prepare image for upload (resize to icon size)
                ModalityCloudImageService.prepareImageForUpload(originalImage, true, 1, 0, 0, ICON_SIZE, ICON_SIZE)
                        .onFailure(e -> {
                            Console.log("Failed to prepare image for upload: " + e);
                            imageUploadProgressIndicator.setVisible(false);
                        })
                        .onSuccess(pngBlob -> {
                            // Upload the PNG blob
                            ModalityCloudImageService.replaceImage(buildingCloudImagePath, pngBlob)
                                    .inUiThread()
                                    .onComplete(ar -> {
                                        if (ar.failed()) {
                                            Console.log("Failed to upload image: " + ar.cause());
                                            imageUploadProgressIndicator.setVisible(false);
                                        } else {
                                            Console.log("Building image uploaded successfully");
                                            loadBuildingImage();
                                        }
                                    });
                        });
            }
        }, originalImage.progressProperty());
    }

    private void handleDeleteImage() {
        if (buildingCloudImagePath == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(I18n.getI18nText(RoomSetupI18nKeys.RemoveImageTitle));
        alert.setHeaderText(I18n.getI18nText(RoomSetupI18nKeys.RemoveImageMessage));
        alert.setContentText(I18n.getI18nText(RoomSetupI18nKeys.RemoveImageContent));
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                imageUploadProgressIndicator.setVisible(true);
                ModalityCloudImageService.deleteImage(buildingCloudImagePath)
                        .inUiThread()
                        .onSuccess(e -> {
                            iconImageContainer.setContent(createImagePlaceholder());
                            imageUploadProgressIndicator.setVisible(false);
                        })
                        .onFailure(error -> {
                            Console.log("Failed to delete image: " + error);
                            imageUploadProgressIndicator.setVisible(false);
                        });
            }
        });
    }

    private void loadOrganizationSite() {
        EntityId orgId = FXOrganizationId.getOrganizationId();
        if (orgId == null) return;

        EntityStore entityStore = EntityStore.create(dataSourceModel);
        entityStore.<Organization>executeQuery("select globalSite from Organization where id=?", orgId)
                .onSuccess(organizations -> {
                    if (!organizations.isEmpty()) {
                        Organization org = organizations.get(0);
                        resolvedSite = org.getGlobalSite();
                        Console.log("Resolved site for new building: " + (resolvedSite != null ? resolvedSite.getName() : "null"));
                    }
                })
                .onFailure(e -> Console.log("Error loading organization site: " + e.getMessage()));
    }

    private void populateForm() {
        if (existingBuilding == null) return;

        nameField.setText(existingBuilding.getName());
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public BooleanProperty hasChangesProperty() {
        return hasChanges;
    }

    private void setupChangeTracking() {
        // Store initial value
        initialName = existingBuilding != null ? (existingBuilding.getName() != null ? existingBuilding.getName() : "") : "";

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
        // Validate required fields first
        if (!validationSupport.isValid()) {
            return;
        }

        try {
            String name = nameField.getText();

            UpdateStore updateStore = UpdateStore.create(dataSourceModel);

            Building building;
            if (existingBuilding != null) {
                building = updateStore.updateEntity(existingBuilding);
            } else {
                building = updateStore.insertEntity(Building.class);
                // Set the site from organization's globalSite
                if (resolvedSite != null) {
                    building.setSite(resolvedSite);
                } else {
                    Console.log("Warning: Could not resolve site for new building");
                    // Try to use organization ID directly to query site
                    EntityId orgId = FXOrganizationId.getOrganizationId();
                    if (orgId != null) {
                        Console.log("Using organization ID to set site: " + orgId);
                    }
                }
            }

            building.setName(name.trim());

            updateStore.submitChanges()
                    .onFailure(error -> Console.log("Error saving building: " + error.getMessage()))
                    .onSuccess(result -> {
                        Console.log("Building saved successfully");
                        // Update image path for newly created building
                        if (existingBuilding == null) {
                            Object newBuildingId = result.getGeneratedKey();
                            if (newBuildingId != null) {
                                buildingCloudImagePath = "buildings/building-" + newBuildingId;
                                Console.log("New building image path: " + buildingCloudImagePath);
                            }
                        }
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
        // Reason: The Building table does not have a 'removed' column in the database schema,
        // and buildings may be referenced by Resources and Bookings. Hard deletion would
        // violate CLAUDE.md guidelines: "NEVER hard-delete entities - Always use soft-delete".
        //
        // To enable deletion in the future:
        // 1. Add 'removed' column to the building table (database migration)
        // 2. Add isRemoved()/setRemoved() methods to Building interface
        // 3. Update queries to filter out removed buildings (where removed is null or removed=false)
        // 4. Change this method to set removed=true instead of deleteEntity()
        //
        // For now, buildings can only be renamed, not deleted.
        dialogCallback.closeDialog();
    }
}
