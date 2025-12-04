package one.modality.hotel.backoffice.activities.roomsetup.dialog;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelector;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.shared.entities.EventType;
import one.modality.base.shared.entities.Pool;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.hotel.backoffice.activities.roomsetup.RoomSetupI18nKeys;
import one.modality.hotel.backoffice.activities.roomsetup.util.UIComponentDecorators;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;

/**
 * Dialog for creating and editing pools (both source pools and category pools).
 * Features:
 * - Name field (required)
 * - Description field (optional, stored in name for now)
 * - Icon selector (SVG paths)
 * - Color picker (8 predefined colors)
 * - "Allows Bookings" toggle (only for category/event pools)
 *
 * @author Claude Code
 */
public class PoolDialog implements DialogManager.ManagedDialog {

    // SVG icon paths for pool icons
    public static final String[][] POOL_ICONS = {
        // Home/Residents
        {"M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z", "Home"},
        // Family
        {"M16 4c0-1.11-.89-2-2-2s-2 .89-2 2 .89 2 2 2 2-.89 2-2zm-5 0c0-1.11-.89-2-2-2s-2 .89-2 2 .89 2 2 2 2-.89 2-2zm9 17v-5h1c.55 0 1-.45 1-1v-4c0-1.11-.89-2-2-2h-4c-.45 0-.85.15-1.19.38-.08.06-.16.13-.23.2l-.53.53c-.4.4-.4 1.05 0 1.44l.88.88-.88.88c-.4.4-.4 1.05 0 1.44l1.06 1.06c.4.4 1.05.4 1.44 0l.44-.44V21h4zM7 21v-5.15l-.88.88c-.4.4-1.05.4-1.44 0l-1.06-1.06c-.4-.4-.4-1.05 0-1.44l.88-.88-.88-.88c-.4-.4-.4-1.05 0-1.44l.53-.53c.07-.07.15-.14.23-.2C4.85 10.15 5.25 10 5.7 10h4c1.11 0 2 .89 2 2v4c0 .55-.45 1-1 1h-1v5H7z", "Family"},
        // Building/Guests
        {"M12 7V3H2v18h20V7H12zM6 19H4v-2h2v2zm0-4H4v-2h2v2zm0-4H4V9h2v2zm0-4H4V5h2v2zm4 12H8v-2h2v2zm0-4H8v-2h2v2zm0-4H8V9h2v2zm0-4H8V5h2v2zm10 12h-8v-2h2v-2h-2v-2h2v-2h-2V9h8v10zm-2-8h-2v2h2v-2zm0 4h-2v2h2v-2z", "Building"},
        // Handshake/Volunteers
        {"M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z", "Checkmark"},
        // Refresh/Overflow
        {"M17.65 6.35A7.958 7.958 0 0012 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08A5.99 5.99 0 0112 18c-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z", "Refresh"},
        // Teacher/Graduation
        {"M5 13.18v4L12 21l7-3.82v-4L12 17l-7-3.82zM12 3L1 9l11 6 9-4.91V17h2V9L12 3z", "Teacher"},
        // Staff/Briefcase
        {"M20 7h-4V5c0-1.1-.9-2-2-2h-4c-1.1 0-2 .9-2 2v2H4c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V9c0-1.1-.9-2-2-2zm-6 0h-4V5h4v2z", "Briefcase"},
        // Ticket/General
        {"M22 10V6c0-1.1-.9-2-2-2H4c-1.1 0-1.99.9-1.99 2v4c1.1 0 1.99.9 1.99 2s-.89 2-2 2v4c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2v-4c-1.1 0-2-.9-2-2s.9-2 2-2z", "Ticket"},
        // Block/Unavailable
        {"M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zM4 12c0-4.42 3.58-8 8-8 1.85 0 3.55.63 4.9 1.69L5.69 16.9A7.902 7.902 0 014 12zm8 8c-1.85 0-3.55-.63-4.9-1.69L18.31 7.1A7.902 7.902 0 0120 12c0 4.42-3.58 8-8 8z", "Block"},
        // Star
        {"M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z", "Star"},
        // Heart
        {"M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z", "Heart"},
        // People
        {"M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z", "People"}
    };

    // Color options with hex and name
    public static final String[][] COLOR_OPTIONS = {
        {"#d97706", "Amber"},
        {"#dc2626", "Red"},
        {"#db2777", "Pink"},
        {"#7c3aed", "Purple"},
        {"#2563eb", "Blue"},
        {"#0891b2", "Cyan"},
        {"#059669", "Green"},
        {"#475569", "Slate"}
    };

    private final DataSourceModel dataSourceModel;
    private final Pool existingPool;
    private final boolean isEventPool; // false = source pool, true = category/event pool

    // Form fields
    private TextField nameField;
    private TextArea descriptionArea;
    private final StringProperty selectedIconPath = new SimpleStringProperty(POOL_ICONS[0][0]);
    private final StringProperty selectedColor = new SimpleStringProperty(COLOR_OPTIONS[0][0]);
    private CheckBox allowsBookingsCheckBox;
    private EntityButtonSelector<EventType> eventTypeSelector;

    private final UpdateStore updateStore;
    private final Pool pool; // The pool entity being edited in the UpdateStore
    private EventType resolvedEventType;
    private Runnable onSaveCallback;
    private final ValidationSupport validationSupport = new ValidationSupport();
    private final BooleanProperty hasChanges = new SimpleBooleanProperty(false);

    public PoolDialog(DataSourceModel dataSourceModel, Pool existingPool, boolean isEventPool) {
        this.dataSourceModel = dataSourceModel;
        this.existingPool = existingPool;
        this.isEventPool = isEventPool;

        // Create the UpdateStore and pool entity immediately
        updateStore = UpdateStore.create(dataSourceModel);
        if (existingPool != null) {
            pool = updateStore.updateEntity(existingPool);
            // Initialize UI properties from existing pool data
            if (existingPool.getGraphic() != null && !existingPool.getGraphic().isEmpty()) {
                selectedIconPath.set(existingPool.getGraphic());
            }
            if (existingPool.getWebColor() != null && !existingPool.getWebColor().isEmpty()) {
                selectedColor.set(existingPool.getWebColor());
            }
        } else {
            pool = updateStore.insertEntity(Pool.class);
            pool.setEventPool(isEventPool);
            // Set default values for new pool
            pool.setWebColor(selectedColor.get());
            pool.setGraphic(selectedIconPath.get());
            if (isEventPool) {
                pool.setBookable(true); // Default to bookable for new category pools
            }
        }
    }

    public Node buildView() {
        VBox container = new VBox();
        container.setSpacing(20);
        container.setPadding(new Insets(24));
        container.setMinWidth(480);
        container.getStyleClass().add(UIComponentDecorators.CSS_DIALOG_CONTAINER);

        // Header
        HBox header = createHeader();

        // Form
        VBox form = createForm();

        container.getChildren().addAll(header, form);

        // Initialize with existing data if editing
        if (existingPool != null) {
            populateForm();
        }

        // Load event type for new pools
        if (existingPool == null) {
            loadOrganizationEventType();
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

        // Icon preview based on selected settings
        StackPane iconPreview = createIconPreview(32);

        VBox titleBox = new VBox();
        titleBox.setSpacing(4);
        Object titleKey = isEventPool ?
                (existingPool != null ? RoomSetupI18nKeys.CategoryPoolDialogEditTitle : RoomSetupI18nKeys.CategoryPoolDialogAddTitle) :
                (existingPool != null ? RoomSetupI18nKeys.SourcePoolDialogEditTitle : RoomSetupI18nKeys.SourcePoolDialogAddTitle);
        Label titleLabel = I18nControls.newLabel(titleKey);
        titleLabel.getStyleClass().add(UIComponentDecorators.CSS_TITLE);

        Label subtitleLabel = I18nControls.newLabel(isEventPool ?
                RoomSetupI18nKeys.CategoryPoolSubtitle :
                RoomSetupI18nKeys.SourcePoolSubtitle);
        subtitleLabel.getStyleClass().add(UIComponentDecorators.CSS_SUBTITLE);

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        header.getChildren().addAll(iconPreview, titleBox);

        return header;
    }

    private VBox createForm() {
        VBox form = new VBox();
        form.setSpacing(16);

        // Name field
        VBox nameSection = new VBox();
        nameSection.setSpacing(8);
        Label nameLabel = I18nControls.newLabel(RoomSetupI18nKeys.FieldPoolNameRequired);
        nameLabel.getStyleClass().add(UIComponentDecorators.CSS_FIELD_LABEL);
        nameField = new TextField();
        nameField.setPromptText(I18n.getI18nText(RoomSetupI18nKeys.PoolNamePlaceholder));
        nameField.getStyleClass().add(UIComponentDecorators.CSS_TEXT_FIELD);
        nameField.setPadding(new Insets(12, 14, 12, 14));
        nameSection.getChildren().addAll(nameLabel, nameField);

        // Description field
        VBox descriptionSection = new VBox();
        descriptionSection.setSpacing(8);
        Label descriptionLabel = I18nControls.newLabel(RoomSetupI18nKeys.FieldDescription);
        descriptionLabel.getStyleClass().add(UIComponentDecorators.CSS_FIELD_LABEL);
        descriptionArea = new TextArea();
        descriptionArea.setPromptText(I18n.getI18nText(RoomSetupI18nKeys.PoolDescriptionPlaceholder));
        descriptionArea.getStyleClass().add(UIComponentDecorators.CSS_TEXT_AREA);
        descriptionArea.setPrefRowCount(2);
        descriptionArea.setWrapText(true);
        descriptionSection.getChildren().addAll(descriptionLabel, descriptionArea);

        // EventType selector (only for source pools - optional)
        VBox eventTypeSection = null;
        if (!isEventPool) {
            eventTypeSection = createEventTypeSelector();
        }

        // Icon selector
        VBox iconSection = createIconSelector();

        // Color picker
        VBox colorSection = createColorPicker();

        // Allows Bookings toggle (only for category/event pools)
        VBox bookingsSection = null;
        if (isEventPool) {
            bookingsSection = createAllowsBookingsToggle();
        }

        form.getChildren().addAll(nameSection, descriptionSection);
        if (eventTypeSection != null) {
            form.getChildren().add(eventTypeSection);
        }
        form.getChildren().addAll(iconSection, colorSection);
        if (bookingsSection != null) {
            form.getChildren().add(bookingsSection);
        }

        return form;
    }

    private VBox createEventTypeSelector() {
        VBox section = new VBox();
        section.setSpacing(8);
        Label label = I18nControls.newLabel(RoomSetupI18nKeys.FieldEventType);
        label.getStyleClass().add(UIComponentDecorators.CSS_FIELD_LABEL);

        Object orgId = FXOrganizationId.getOrganizationId();
        String eventTypeQuery = "{class: 'EventType', columns: 'name', where: '" +
                (orgId != null ? "organization=" + orgId : "1=1") + "', orderBy: 'name'}";

        eventTypeSelector = new EntityButtonSelector<>(
                eventTypeQuery,
                new ButtonFactoryMixin() {}, FXMainFrameDialogArea::getDialogArea, dataSourceModel
        );
        eventTypeSelector.setShowMode(ButtonSelector.ShowMode.DROP_DOWN);
        eventTypeSelector.setSearchEnabled(false);

        Node selectorButton = eventTypeSelector.getButton();
        selectorButton.getStyleClass().add(UIComponentDecorators.CSS_TEXT_FIELD);

        // Set initial value if editing an existing pool
        if (existingPool != null && existingPool.getEventType() != null) {
            eventTypeSelector.setSelectedItem(existingPool.getEventType());
        }

        section.getChildren().addAll(label, selectorButton);
        return section;
    }

    private VBox createIconSelector() {
        VBox iconSection = new VBox();
        iconSection.setSpacing(8);

        Label iconLabel = I18nControls.newLabel(RoomSetupI18nKeys.FieldIcon);
        iconLabel.getStyleClass().add(UIComponentDecorators.CSS_FIELD_LABEL);

        // Current icon preview + grid
        HBox iconRow = new HBox();
        iconRow.setSpacing(16);
        iconRow.setAlignment(Pos.CENTER_LEFT);

        // Large preview
        StackPane previewPane = createIconPreview(48);
        previewPane.setStyle("-fx-background-color: #f5f5f4; -fx-background-radius: 12; -fx-border-color: #e5e5e5; -fx-border-radius: 12;");

        // Icon grid
        FlowPane iconGrid = new FlowPane();
        iconGrid.setHgap(8);
        iconGrid.setVgap(8);
        iconGrid.setPrefWrapLength(300);

        for (String[] iconData : POOL_ICONS) {
            String path = iconData[0];
            String name = iconData[1];

            StackPane iconCell = new StackPane();
            iconCell.setPrefSize(36, 36);
            iconCell.setMinSize(36, 36);

            SVGPath svg = new SVGPath();
            svg.setContent(path);
            svg.setScaleX(0.7);
            svg.setScaleY(0.7);

            iconCell.getChildren().add(svg);
            iconCell.setStyle(UIComponentDecorators.getIconCellUnselectedStyle());

            // Selection highlight
            selectedIconPath.addListener((obs, oldVal, newVal) -> {
                if (path.equals(newVal)) {
                    iconCell.setStyle(UIComponentDecorators.getIconCellSelectedStyle(selectedColor.get()));
                    svg.setFill(Color.web(selectedColor.get()));
                } else {
                    iconCell.setStyle(UIComponentDecorators.getIconCellUnselectedStyle());
                    svg.setFill(Color.web("#78716c"));
                }
            });

            // Update on color change too
            selectedColor.addListener((obs, oldVal, newVal) -> {
                if (path.equals(selectedIconPath.get())) {
                    iconCell.setStyle(UIComponentDecorators.getIconCellSelectedStyle(newVal));
                    svg.setFill(Color.web(newVal));
                }
            });

            // Initial state
            if (path.equals(selectedIconPath.get())) {
                iconCell.setStyle(UIComponentDecorators.getIconCellSelectedStyle(selectedColor.get()));
                svg.setFill(Color.web(selectedColor.get()));
            } else {
                svg.setFill(Color.web("#78716c"));
            }

            // GWT-compatible tooltip: create a label that shows on hover
            Label iconTooltip = new Label(name);
            iconTooltip.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-padding: 4 8; " +
                    "-fx-background-radius: 4; -fx-font-size: 11px;");
            iconTooltip.setVisible(false);
            iconTooltip.setManaged(false);
            iconTooltip.setTranslateY(36);
            iconCell.getChildren().add(iconTooltip);

            iconCell.setOnMouseEntered(e -> {
                iconTooltip.setVisible(true);
                iconTooltip.setManaged(true);
            });
            iconCell.setOnMouseExited(e -> {
                iconTooltip.setVisible(false);
                iconTooltip.setManaged(false);
            });

            iconCell.setOnMouseClicked(e -> selectedIconPath.set(path));

            iconGrid.getChildren().add(iconCell);
        }

        iconRow.getChildren().addAll(previewPane, iconGrid);
        iconSection.getChildren().addAll(iconLabel, iconRow);

        return iconSection;
    }

    private StackPane createIconPreview(double size) {
        StackPane previewPane = new StackPane();
        previewPane.setPrefSize(size, size);
        previewPane.setMinSize(size, size);
        previewPane.setMaxSize(size, size);

        SVGPath previewSvg = new SVGPath();
        double scale = size / 24.0 * 0.6;
        previewSvg.setScaleX(scale);
        previewSvg.setScaleY(scale);

        // Bind to selected values
        selectedIconPath.addListener((obs, oldVal, newVal) -> previewSvg.setContent(newVal));
        selectedColor.addListener((obs, oldVal, newVal) -> {
            previewSvg.setFill(Color.web(newVal));
            previewPane.setStyle(UIComponentDecorators.getIconPreviewStyle(newVal));
        });

        // Initial state
        previewSvg.setContent(selectedIconPath.get());
        previewSvg.setFill(Color.web(selectedColor.get()));
        previewPane.setStyle(UIComponentDecorators.getIconPreviewStyle(selectedColor.get()));

        previewPane.getChildren().add(previewSvg);
        return previewPane;
    }

    private VBox createColorPicker() {
        VBox colorSection = new VBox();
        colorSection.setSpacing(8);

        Label colorLabel = I18nControls.newLabel(RoomSetupI18nKeys.FieldColor);
        colorLabel.getStyleClass().add(UIComponentDecorators.CSS_FIELD_LABEL);

        HBox colorRow = new HBox();
        colorRow.setSpacing(10);
        colorRow.setAlignment(Pos.CENTER_LEFT);

        for (String[] colorData : COLOR_OPTIONS) {
            String hex = colorData[0];
            String name = colorData[1];

            StackPane colorCell = new StackPane();
            colorCell.setPrefSize(36, 36);
            colorCell.setMinSize(36, 36);

            Region colorCircle = new Region();
            colorCircle.setPrefSize(24, 24);
            colorCircle.setMaxSize(24, 24);
            colorCircle.setStyle("-fx-background-color: " + hex + "; -fx-background-radius: 12;");

            colorCell.getChildren().add(colorCircle);
            colorCell.setStyle(UIComponentDecorators.getColorCellUnselectedStyle());

            // Selection highlight
            selectedColor.addListener((obs, oldVal, newVal) -> {
                if (hex.equals(newVal)) {
                    colorCell.setStyle(UIComponentDecorators.getColorCellSelectedStyle(hex));
                } else {
                    colorCell.setStyle(UIComponentDecorators.getColorCellUnselectedStyle());
                }
            });

            // Initial state
            if (hex.equals(selectedColor.get())) {
                colorCell.setStyle(UIComponentDecorators.getColorCellSelectedStyle(hex));
            }

            // GWT-compatible tooltip: create a label that shows on hover
            Label colorTooltip = new Label(name);
            colorTooltip.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-padding: 4 8; " +
                    "-fx-background-radius: 4; -fx-font-size: 11px;");
            colorTooltip.setVisible(false);
            colorTooltip.setManaged(false);
            colorTooltip.setTranslateY(28);
            colorCell.getChildren().add(colorTooltip);

            colorCell.setOnMouseEntered(e -> {
                colorTooltip.setVisible(true);
                colorTooltip.setManaged(true);
            });
            colorCell.setOnMouseExited(e -> {
                colorTooltip.setVisible(false);
                colorTooltip.setManaged(false);
            });

            colorCell.setOnMouseClicked(e -> selectedColor.set(hex));

            colorRow.getChildren().add(colorCell);
        }

        colorSection.getChildren().addAll(colorLabel, colorRow);
        return colorSection;
    }

    private VBox createAllowsBookingsToggle() {
        VBox toggleSection = new VBox();
        toggleSection.setSpacing(8);

        allowsBookingsCheckBox = new CheckBox(I18n.getI18nText(RoomSetupI18nKeys.AllowsBookings));
        allowsBookingsCheckBox.setSelected(true); // Default to true for new pools

        // Update styling based on checked state
        allowsBookingsCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> updateAllowsBookingsStyle(toggleSection, newVal));

        Label hintLabel = new Label();
        hintLabel.setWrapText(true);
        hintLabel.getStyleClass().add(UIComponentDecorators.CSS_SMALL);

        allowsBookingsCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                hintLabel.setText(I18n.getI18nText(RoomSetupI18nKeys.AllowsBookingsHint));
            } else {
                hintLabel.setText(I18n.getI18nText(RoomSetupI18nKeys.DoesNotAllowBookingsHint));
            }
        });

        // Initial hint text
        hintLabel.setText(I18n.getI18nText(RoomSetupI18nKeys.AllowsBookingsHint));

        toggleSection.getChildren().addAll(allowsBookingsCheckBox, hintLabel);
        updateAllowsBookingsStyle(toggleSection, true);

        return toggleSection;
    }

    private void updateAllowsBookingsStyle(VBox container, boolean allowsBookings) {
        if (allowsBookings) {
            container.setStyle("-fx-padding: 14 16; -fx-background-color: #f0fdf4; -fx-background-radius: 10; -fx-border-color: #bbf7d0; -fx-border-radius: 10;");
        } else {
            container.setStyle("-fx-padding: 14 16; -fx-background-color: #fef2f2; -fx-background-radius: 10; -fx-border-color: #fecaca; -fx-border-radius: 10;");
        }
    }

    private void loadOrganizationEventType() {
        EntityId orgId = FXOrganizationId.getOrganizationId();
        if (orgId == null) return;

        EntityStore entityStore = EntityStore.create(dataSourceModel);
        entityStore.<EventType>executeQuery("select id,name,organization from EventType where organization=? limit 1", orgId)
                .onSuccess(eventTypes -> {
                    if (!eventTypes.isEmpty()) {
                        resolvedEventType = eventTypes.get(0);
                        Console.log("Resolved event type for new pool: " + resolvedEventType.getName());
                    }
                })
                .onFailure(e -> Console.log("Error loading event type: " + e.getMessage()));
    }

    private void populateForm() {
        if (existingPool == null) return;

        nameField.setText(existingPool.getName());

        if (existingPool.getDescription() != null) {
            descriptionArea.setText(existingPool.getDescription());
        }

        if (existingPool.getGraphic() != null && !existingPool.getGraphic().isEmpty()) {
            selectedIconPath.set(existingPool.getGraphic());
        }

        if (existingPool.getWebColor() != null && !existingPool.getWebColor().isEmpty()) {
            selectedColor.set(existingPool.getWebColor());
        }

        // For category pools, load the bookable setting from the pool
        if (allowsBookingsCheckBox != null) {
            Boolean bookable = existingPool.isBookable();
            // Default to true if not set
            allowsBookingsCheckBox.setSelected(bookable == null || bookable);
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public BooleanProperty hasChangesProperty() {
        return hasChanges;
    }

    private void updateHasChanges() {
        hasChanges.set(updateStore.hasChanges());
    }

    private void setupChangeTracking() {
        // Listen to form field changes and update the pool entity directly
        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            pool.setName(newVal != null ? newVal.trim() : null);
            updateHasChanges();
        });

        descriptionArea.textProperty().addListener((obs, oldVal, newVal) -> {
            pool.setDescription(newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null);
            updateHasChanges();
        });

        selectedColor.addListener((obs, oldVal, newVal) -> {
            pool.setWebColor(newVal);
            updateHasChanges();
        });

        selectedIconPath.addListener((obs, oldVal, newVal) -> {
            pool.setGraphic(newVal);
            updateHasChanges();
        });

        if (allowsBookingsCheckBox != null) {
            allowsBookingsCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                pool.setBookable(newVal);
                updateHasChanges();
            });
        }
    }

    private void setupValidation() {
        validationSupport.addRequiredInput(nameField);
    }

    public boolean shouldSave() {
        return true;
    }

    public void save(DialogCallback dialogCallback) {
        // Validate required fields first
        if (!validationSupport.isValid()) {
            return;
        }

        try {
            // Handle event type for source pools
            if (!isEventPool && eventTypeSelector != null) {
                EventType selectedEventType = eventTypeSelector.getSelectedItem();
                pool.setEventType(selectedEventType); // Can be null (optional)
            } else if (existingPool == null) {
                // For new pools, set the event type
                if (resolvedEventType != null) {
                    pool.setEventType(resolvedEventType);
                } else {
                    Console.log("Warning: Could not resolve event type for new pool");
                }
            }

            updateStore.submitChanges()
                    .onFailure(error -> Console.log("Error saving pool: " + error.getMessage()))
                    .onSuccess(result -> {
                        Console.log("Pool saved successfully");
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
        // Reason: The Pool table does not have a 'removed' column in the database schema,
        // and pools may be referenced by PoolAllocations and event configurations.
        // Hard deletion would violate CLAUDE.md guidelines:
        // "NEVER hard-delete entities - Always use soft-delete by setting removed=true"
        //
        // To enable deletion in the future:
        // 1. Add 'removed' column to the pool table (database migration)
        // 2. Add isRemoved()/setRemoved() methods to Pool interface
        // 3. Update queries to filter out removed pools (where removed is null or removed=false)
        // 4. Change this method to set removed=true instead of deleteEntity()
        //
        // For now, pools can only be renamed or modified, not deleted.
        dialogCallback.closeDialog();
    }
}
