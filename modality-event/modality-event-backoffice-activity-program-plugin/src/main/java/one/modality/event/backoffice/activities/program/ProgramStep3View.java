package one.modality.event.backoffice.activities.program;

import dev.webfx.extras.filepicker.FilePicker;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.cloudinary.ModalityCloudImageService;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.client.util.dialog.ModalityDialog;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.knownitems.KnownItemFamily;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

/**
 * UI component for Step 3 of the program setup: Program Validation & Finalization.
 * This step displays when the program has been generated (validated) and shows:
 * - Step progress indicator (All steps complete, Step 3 active)
 * - Generated program timetable with actual scheduled items
 * - Teaching sessions grouped by date with audio/video badges and participant counts
 * - Ability to edit, delete, cancel/uncancel sessions
 *
 * <p><b>UI Structure:</b>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────┐
 * │ [Step Indicator: 1✓  2✓  3 Active]                         │
 * ├─────────────────────────────────────────────────────────────┤
 * │ Monday - 10 April 2023                          [4 sessions]│
 * │   ┌──────────────────────────────────────────────────────┐  │
 * │   │ 9:30 - 10:30 | Introduction          [Audio] [Video] │  │
 * │   │                                       [145] [✓] [...]│  │
 * │   └──────────────────────────────────────────────────────┘  │
 * │   [+ Add Session]                                            │
 * └─────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * @author David Hello
 */
final class ProgramStep3View {

    private static final double MAX_WIDTH = 1122;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy");

    private final EntityStore entityStore;
    private final ProgramModel programModel;
    private final ObjectProperty<Event> eventProperty = new SimpleObjectProperty<>();
    private final VBox mainContainer;
    private final HBox stepIndicatorContainer;
    private final VBox titleContainer;
    private final VBox daysContainer;
    private final ProgressIndicator loadingIndicator;

    // Maps to store attendance and media counts per scheduled item
    private final Map<Object, Integer> attendanceCountsByScheduledItemId = new HashMap<>();
    private final Map<Object, Boolean> audioOfferedByScheduledItemId = new HashMap<>();
    private final Map<Object, Integer> audioMediaCountsByScheduledItemId = new HashMap<>();
    private final Map<Object, Boolean> videoOfferedByScheduledItemId = new HashMap<>();
    private final Map<Object, Integer> videoMediaCountsByScheduledItemId = new HashMap<>();

    // Event image upload components
    private static final double EVENT_IMAGE_SIZE = 300; // 1:1 aspect ratio (square)
    private final MonoPane eventImageContainer = new MonoPane();
    private final ProgressIndicator imageUploadProgressIndicator = Controls.createProgressIndicator(40);
    private final StackPane imageOverlay = new StackPane();
    private String eventCoverCloudImagePath;

    /**
     * Creates the Step 3 view.
     *
     * @param programModel The program model containing business logic
     */
    ProgramStep3View(ProgramModel programModel) {
        this.programModel = programModel;
        this.entityStore = programModel.getEntityStore();

        // Build step indicator
        this.stepIndicatorContainer = buildStepIndicator();

        // Build title
        this.titleContainer = buildTitle();

        // Build program schedule UI
        daysContainer = new VBox(32);
        daysContainer.setAlignment(Pos.TOP_CENTER);

        // Create loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(60, 60);

        mainContainer = buildUi();

        // Reactive loading: reload program when event changes OR when program becomes generated
        FXProperties.runOnPropertiesChange(this::loadProgramData, eventProperty, programModel.programGeneratedProperty());

        // Reactive loading: reload event image when event changes
        FXProperties.runOnPropertyChange(this::updateEventCoverPath, eventProperty);
    }

    /**
     * Returns the root UI node for Step 3.
     */
    Node getView() {
        return mainContainer;
    }

    /**
     * Sets the event to display the program for.
     */
    void setEvent(Event event) {
        eventProperty.set(event);
    }

    /**
     * Builds the step indicator showing all steps complete, Step 3 active.
     */
    private HBox buildStepIndicator() {
        BooleanProperty programGeneratedProperty = programModel.programGeneratedProperty();

        // Step 1 Circle (completed - green check)
        Label step1CheckCompleteStep3 = new Label("✓");
        step1CheckCompleteStep3.getStyleClass().add("program-step-check-completed");
        StackPane step1CircleCompleteStep3 = new StackPane(step1CheckCompleteStep3);
        step1CircleCompleteStep3.getStyleClass().add("program-step-circle-completed");
        step1CircleCompleteStep3.setMinSize(40, 40);
        step1CircleCompleteStep3.setMaxSize(40, 40);

        Label step1LabelCompleteStep3 = I18nControls.newLabel(ProgramI18nKeys.GeneratePreliminaryBookableSI);
        step1LabelCompleteStep3.getStyleClass().add("program-step-label-completed");
        step1LabelCompleteStep3.setWrapText(true);
        step1LabelCompleteStep3.setMaxWidth(150);
        step1LabelCompleteStep3.setAlignment(Pos.CENTER);
        step1LabelCompleteStep3.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox step1BoxCompleteStep3 = new VBox(12, step1CircleCompleteStep3, step1LabelCompleteStep3);
        step1BoxCompleteStep3.setAlignment(Pos.CENTER);

        // Connector line (green - completed)
        Region connector3 = new Region();
        connector3.getStyleClass().add("program-step-connector-completed");
        connector3.setPrefHeight(2);
        connector3.setMaxHeight(2);
        connector3.setMinWidth(80);
        connector3.setPrefWidth(120);
        HBox.setHgrow(connector3, Priority.ALWAYS);

        // Step 2 Circle (completed - green check)
        Label step2CheckComplete = new Label("✓");
        step2CheckComplete.getStyleClass().add("program-step-check-completed");
        StackPane step2CircleComplete = new StackPane(step2CheckComplete);
        step2CircleComplete.getStyleClass().add("program-step-circle-completed");
        step2CircleComplete.setMinSize(40, 40);
        step2CircleComplete.setMaxSize(40, 40);

        Label step2LabelComplete = I18nControls.newLabel(ProgramI18nKeys.GenerateProgramSchedule);
        step2LabelComplete.getStyleClass().add("program-step-label-completed");
        step2LabelComplete.setWrapText(true);
        step2LabelComplete.setMaxWidth(150);
        step2LabelComplete.setAlignment(Pos.CENTER);
        step2LabelComplete.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox step2BoxComplete = new VBox(12, step2CircleComplete, step2LabelComplete);
        step2BoxComplete.setAlignment(Pos.CENTER);

        // Connector line (green - completed)
        Region connector3b = new Region();
        connector3b.getStyleClass().add("program-step-connector-completed");
        connector3b.setPrefHeight(2);
        connector3b.setMaxHeight(2);
        connector3b.setMinWidth(80);
        connector3b.setPrefWidth(120);
        HBox.setHgrow(connector3b, Priority.ALWAYS);

        // Step 3 Circle (active)
        Label step3NumberActive = new Label("3");
        step3NumberActive.getStyleClass().add("program-step-number-active");
        StackPane step3CircleActive = new StackPane(step3NumberActive);
        step3CircleActive.getStyleClass().add("program-step-circle-active");
        step3CircleActive.setMinSize(40, 40);
        step3CircleActive.setMaxSize(40, 40);

        Label step3LabelActive = I18nControls.newLabel(ProgramI18nKeys.ValidateProgram);
        step3LabelActive.getStyleClass().add("program-step-label-active");
        step3LabelActive.setWrapText(true);
        step3LabelActive.setMaxWidth(150);
        step3LabelActive.setAlignment(Pos.CENTER);
        step3LabelActive.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox step3BoxActive = new VBox(12, step3CircleActive, step3LabelActive);
        step3BoxActive.setAlignment(Pos.CENTER);

        // Steps container for Step 3 view
        HBox stepsRowStep3 = new HBox(20);
        stepsRowStep3.setAlignment(Pos.CENTER);
        stepsRowStep3.getChildren().addAll(step1BoxCompleteStep3, connector3, step2BoxComplete, connector3b, step3BoxActive);
        stepsRowStep3.setMaxWidth(800);
        stepsRowStep3.setPadding(new Insets(30, 20, 30, 20));

        HBox container = new HBox(stepsRowStep3);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(0, 20, 24, 20));

        // Show step 3 indicator when program is generated
        container.visibleProperty().bind(programGeneratedProperty);
        container.managedProperty().bind(programGeneratedProperty);

        return container;
    }

    /**
     * Builds the title for Step 3.
     */
    private VBox buildTitle() {
        BooleanProperty programGeneratedProperty = programModel.programGeneratedProperty();

        Label title = I18nControls.newLabel(ProgramI18nKeys.ProgramTitle);
        title.setContentDisplay(javafx.scene.control.ContentDisplay.LEFT);
        title.setPadding(new Insets(30));
        title.setGraphicTextGap(30);
        title.getStyleClass().add(Bootstrap.H2);
        dev.webfx.extras.theme.text.TextTheme.createPrimaryTextFacet(title).style();

        VBox titleBox = new VBox(title);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.visibleProperty().bind(programGeneratedProperty);
        titleBox.managedProperty().bind(programGeneratedProperty);

        return titleBox;
    }

    /**
     * Builds a warning message shown when the event has no short description.
     * The short description is required for displaying event information on the user frontend.
     */
    private VBox buildShortDescriptionWarning() {
        BooleanProperty programGeneratedProperty = programModel.programGeneratedProperty();

        // Main container
        VBox warningContainer = new VBox();
        warningContainer.setAlignment(Pos.CENTER);
        warningContainer.setPadding(new Insets(0, 20, 24, 20));

        // Warning icon
        SVGPath warningIcon = new SVGPath();
        warningIcon.setContent("M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z");
        warningIcon.setFill(javafx.scene.paint.Color.web("#d97706"));
        warningIcon.setScaleX(1.2);
        warningIcon.setScaleY(1.2);

        // Title
        Label warningTitle = Bootstrap.strong(I18nControls.newLabel(ProgramI18nKeys.EventShortDescriptionRequired));
        warningTitle.getStyleClass().add("program-warning-title-yellow");

        VBox warningTitleBox = new VBox(warningTitle);

        // Content
        Label warningMessage = I18nControls.newLabel(ProgramI18nKeys.EventShortDescriptionRequiredMessage);
        warningMessage.setWrapText(true);
        warningMessage.getStyleClass().add("program-warning-message-yellow");

        VBox warningContent = new VBox(8, warningTitleBox, warningMessage);

        HBox warningHeader = new HBox(12, warningIcon, warningContent);
        warningHeader.setAlignment(Pos.TOP_LEFT);

        VBox warningBox = new VBox(warningHeader);
        warningBox.getStyleClass().add("program-warning-box-yellow");
        warningBox.setPadding(new Insets(20));
        warningBox.setMaxWidth(MAX_WIDTH);

        warningContainer.getChildren().add(warningBox);

        // Show warning when program is generated AND event has no short description or short description label
        // Use programModel's loadedEventProperty to ensure the event has these fields loaded from the database
        BooleanBinding hasNoShortDescription = javafx.beans.binding.Bindings.createBooleanBinding(() -> {
            Event event = programModel.getLoadedEvent();
            if (event == null) {
                return false;
            }
            String shortDescription = event.getShortDescription();
            one.modality.base.shared.entities.Label shortDescriptionLabel = event.getShortDescriptionLabel();

            boolean shortDescEmpty = shortDescription == null || shortDescription.trim().isEmpty();
            boolean shortDescLabelEmpty = shortDescriptionLabel == null;

            return shortDescEmpty && shortDescLabelEmpty;
        }, programModel.loadedEventProperty());

        BooleanBinding shouldShowWarning = programGeneratedProperty.and(hasNoShortDescription);
        warningContainer.visibleProperty().bind(shouldShowWarning);
        warningContainer.managedProperty().bind(shouldShowWarning);

        return warningContainer;
    }

    /**
     * Builds the event image upload section following the HTML template design.
     */
    private VBox buildEventImageUpload() {
        BooleanProperty programGeneratedProperty = programModel.programGeneratedProperty();

        // Main section container
        VBox uploadSection = new VBox(16);
        uploadSection.setAlignment(Pos.CENTER);
        uploadSection.setMaxWidth(MAX_WIDTH);
        uploadSection.getStyleClass().add("event-image-upload-section");
        uploadSection.setPadding(new Insets(24));

        // Header with gradient icon and title
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // Gradient icon container (40×40px with rounded corners)
        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(40, 40);
        iconContainer.setMinSize(40, 40);
        iconContainer.setMaxSize(40, 40);
        iconContainer.getStyleClass().add("image-upload-icon-container");

        // Image icon (white stroke)
        SVGPath imageIcon = new SVGPath();
        imageIcon.setContent("M3 3h18v18H3z M8.5 8.5 A1.5 1.5 0 0 1 8.5 11.5 A1.5 1.5 0 0 1 8.5 8.5 M21 15 L16 10 L5 21");
        imageIcon.setFill(javafx.scene.paint.Color.TRANSPARENT);
        imageIcon.setStroke(javafx.scene.paint.Color.WHITE);
        imageIcon.setStrokeWidth(2);
        imageIcon.setScaleX(0.8);
        imageIcon.setScaleY(0.8);
        iconContainer.getChildren().add(imageIcon);

        // Title
        Label title = I18nControls.newLabel(ProgramI18nKeys.EventImage);
        title.getStyleClass().add("image-upload-title");

        header.getChildren().addAll(iconContainer, title);
        uploadSection.getChildren().add(header);

        // Content area: 2-column grid layout with image preview + info section
        GridPane contentBox = new GridPane();
        contentBox.setHgap(32); // 2rem gap
        contentBox.setAlignment(Pos.TOP_CENTER);

        // Configure columns: equal width (1fr 1fr)
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        col2.setHgrow(Priority.ALWAYS);
        contentBox.getColumnConstraints().addAll(col1, col2);

        // Left: Image preview container (square 1:1 aspect ratio)
        VBox imagePreviewContainer = buildImagePreviewContainer();

        // Right: Info section with card and buttons (vertically centered)
        VBox infoSection = buildInfoSection();
        infoSection.setAlignment(Pos.CENTER_LEFT);

        contentBox.add(imagePreviewContainer, 0, 0);
        contentBox.add(infoSection, 1, 0);
        uploadSection.getChildren().add(contentBox);

        // Show upload section when program is generated
        uploadSection.visibleProperty().bind(programGeneratedProperty);
        uploadSection.managedProperty().bind(programGeneratedProperty);

        return uploadSection;
    }

    /**
     * Builds the image preview container with overlay buttons and placeholder.
     */
    private VBox buildImagePreviewContainer() {
        VBox container = new VBox();
        container.setPrefSize(EVENT_IMAGE_SIZE, EVENT_IMAGE_SIZE);
        container.setMinSize(EVENT_IMAGE_SIZE, EVENT_IMAGE_SIZE);
        container.setMaxSize(EVENT_IMAGE_SIZE, EVENT_IMAGE_SIZE);

        // Image preview with dashed border and placeholder
        eventImageContainer.setPrefSize(EVENT_IMAGE_SIZE, EVENT_IMAGE_SIZE);
        eventImageContainer.setMinSize(EVENT_IMAGE_SIZE, EVENT_IMAGE_SIZE);
        eventImageContainer.setMaxSize(EVENT_IMAGE_SIZE, EVENT_IMAGE_SIZE);
        eventImageContainer.getStyleClass().add("image-preview-container");

        // Create placeholder content
        VBox placeholder = createImagePlaceholder();
        eventImageContainer.setContent(placeholder);

        // Overlay with circular action buttons (view, replace, delete)
        imageOverlay.setAlignment(Pos.CENTER);
        imageOverlay.getStyleClass().add("image-overlay");
        imageOverlay.setVisible(false);
        imageOverlay.setOpacity(0);
        imageOverlay.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 0.6);"
        );

        HBox overlayButtons = new HBox(8);
        overlayButtons.setAlignment(Pos.CENTER);

        // View button (circular, 40px) - magnifying glass/zoom icon
        SVGPath viewIcon = SvgIcons.createZoomIconPath();
        viewIcon.setFill(javafx.scene.paint.Color.web("#374151"));
        viewIcon.setScaleX(1.2);
        viewIcon.setScaleY(1.2);
        Button viewBtn = createOverlayButton(viewIcon, this::handleViewFullSizeImage);

        // Replace button (circular, 40px) - pen icon
        FilePicker replacePicker = FilePicker.create();
        replacePicker.getAcceptedExtensions().addAll("image/*", ".webp", "image/webp");
        SVGPath replaceIcon = SvgIcons.createPenPath();
        replaceIcon.setStroke(javafx.scene.paint.Color.web("#374151"));
        replaceIcon.setFill(javafx.scene.paint.Color.TRANSPARENT);
        replaceIcon.setStrokeWidth(20);
        replaceIcon.setScaleX(0.04);
        replaceIcon.setScaleY(0.04);
        Button replaceBtn = createOverlayButton(replaceIcon, () -> {});
        replacePicker.setGraphic(replaceBtn);

        FXProperties.runOnPropertyChange(fileToUpload -> {
            if (fileToUpload != null) {
                handleImageUpload(fileToUpload);
            }
        }, replacePicker.selectedFileProperty());

        // Delete button (circular, 40px, red icon)
        SVGPath trashIcon = SvgIcons.createTrashSVGPath();
        trashIcon.setStroke(javafx.scene.paint.Color.web("#dc2626"));
        trashIcon.setFill(javafx.scene.paint.Color.TRANSPARENT);
        trashIcon.setStrokeWidth(2);
        trashIcon.setScaleX(0.7);
        trashIcon.setScaleY(0.7);
        Button deleteBtn = createOverlayButton(trashIcon, this::handleDeleteEventImage);

        overlayButtons.getChildren().addAll(viewBtn, replacePicker.getView(), deleteBtn);
        imageOverlay.getChildren().add(overlayButtons);

        // Progress indicator
        imageUploadProgressIndicator.setVisible(false);

        // Stack: image + overlay + progress
        StackPane imageStack = new StackPane(eventImageContainer, imageOverlay, imageUploadProgressIndicator);
        imageStack.setPrefSize(EVENT_IMAGE_SIZE, EVENT_IMAGE_SIZE);
        imageStack.setMinSize(EVENT_IMAGE_SIZE, EVENT_IMAGE_SIZE);
        imageStack.setMaxSize(EVENT_IMAGE_SIZE, EVENT_IMAGE_SIZE);

        // Show overlay on hover when image exists
        imageStack.setOnMouseEntered(e -> {
            if (eventImageContainer.getContent() != null && !(eventImageContainer.getContent() instanceof VBox)) {
                imageOverlay.setVisible(true);
                imageOverlay.setOpacity(1);
            }
            // CSS :hover pseudo-class handles placeholder hover effect
        });
        imageStack.setOnMouseExited(e -> {
            imageOverlay.setVisible(false);
            imageOverlay.setOpacity(0);
        });

        // Click on placeholder triggers file picker
        imageStack.setOnMouseClicked(e -> {
            if (eventImageContainer.getContent() instanceof VBox) {
                // Trigger main upload picker
                handleViewFullSizeImage(); // This will be overridden to trigger upload when no image
            }
        });

        container.getChildren().add(imageStack);
        return container;
    }

    /**
     * Creates the placeholder shown when no image is uploaded.
     */
    private VBox createImagePlaceholder() {
        VBox placeholder = new VBox(12);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setPadding(new Insets(32));

        // Image icon (larger, gray)
        SVGPath imageIcon = new SVGPath();
        imageIcon.setContent("M3 3 L21 3 L21 21 L3 21 Z M8.5 8.5 A1.5 1.5 0 0 1 8.5 11.5 A1.5 1.5 0 0 1 8.5 8.5 M21 15 L16 10 L5 21");
        imageIcon.setStroke(javafx.scene.paint.Color.web("#d1d5db"));
        imageIcon.setFill(javafx.scene.paint.Color.TRANSPARENT);
        imageIcon.setStrokeWidth(1.5);
        imageIcon.setScaleX(2.0);
        imageIcon.setScaleY(2.0);

        Label noImageLabel = I18nControls.newLabel(ProgramI18nKeys.NoImageSelected);
        noImageLabel.getStyleClass().add("image-placeholder-label");

        Label formatHintLabel = I18nControls.newLabel(ProgramI18nKeys.ImageFormatHint);
        formatHintLabel.getStyleClass().add("image-placeholder-hint");

        placeholder.getChildren().addAll(imageIcon, noImageLabel, formatHintLabel);
        return placeholder;
    }

    /**
     * Creates a circular overlay button (40px diameter) with hover effect.
     */
    private Button createOverlayButton(SVGPath icon, Runnable action) {
        Button btn = new Button();
        btn.setGraphic(icon);
        btn.getStyleClass().add("image-overlay-btn");
        btn.setPrefSize(40, 40);
        btn.setMinSize(40, 40);
        btn.setMaxSize(40, 40);

        btn.setOnAction(e -> action.run());
        return btn;
    }

    /**
     * Builds the info section with card and action buttons.
     */
    private VBox buildInfoSection() {
        VBox infoSection = new VBox(16);
        infoSection.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(infoSection, Priority.ALWAYS);

        // Info card with light blue background
        VBox infoCard = new VBox(8);
        infoCard.getStyleClass().add("image-info-card");
        infoCard.setPadding(new Insets(16));

        // Info card header with icon
        HBox infoCardHeader = new HBox(8);
        infoCardHeader.setAlignment(Pos.CENTER_LEFT);

        SVGPath infoIcon = new SVGPath();
        infoIcon.setContent("M12 2 A10 10 0 0 1 12 22 A10 10 0 0 1 12 2 M12 16 L12 12 M12 8 L12.01 8");
        infoIcon.setStroke(javafx.scene.paint.Color.web("#0096d6"));
        infoIcon.setFill(javafx.scene.paint.Color.TRANSPARENT);
        infoIcon.setStrokeWidth(2);
        infoIcon.setScaleX(0.6);
        infoIcon.setScaleY(0.6);

        Label infoTitle = I18nControls.newLabel(ProgramI18nKeys.EventImageUsage);
        infoTitle.getStyleClass().add("image-info-card-title");

        infoCardHeader.getChildren().addAll(infoIcon, infoTitle);

        Label infoText = I18nControls.newLabel(ProgramI18nKeys.EventImageUsageText);
        infoText.setWrapText(true);
        infoText.getStyleClass().add("image-info-card-text");

        infoCard.getChildren().addAll(infoCardHeader, infoText);

        // Action buttons
        HBox actionButtons = new HBox(12);
        actionButtons.setAlignment(Pos.CENTER_LEFT);

        // Primary upload button with gradient and dynamic text
        FilePicker uploadPicker = FilePicker.create();
        uploadPicker.getAcceptedExtensions().addAll("image/*", ".webp", "image/webp");

        Button uploadButton = new Button();
        uploadButton.textProperty().bind(FXProperties.compute(
            eventImageContainer.contentProperty(),
            content -> (content instanceof VBox)
                ? I18n.getI18nText(ProgramI18nKeys.UploadImage)
                : I18n.getI18nText(ProgramI18nKeys.ReplaceImage)
        ));

        // Upload icon
        SVGPath uploadIcon = new SVGPath();
        uploadIcon.setContent("M21 15 L21 19 A2 2 0 0 1 19 21 L5 21 A2 2 0 0 1 3 19 L3 15 M17 8 L12 3 L7 8 M12 3 L12 15");
        uploadIcon.setStroke(javafx.scene.paint.Color.WHITE);
        uploadIcon.setFill(javafx.scene.paint.Color.TRANSPARENT);
        uploadIcon.setStrokeWidth(2);
        uploadIcon.setScaleX(0.7);
        uploadIcon.setScaleY(0.7);

        uploadButton.setGraphic(uploadIcon);
        uploadButton.getStyleClass().add("btn-upload-primary");
        uploadButton.setPadding(new Insets(10, 20, 10, 20));
        uploadButton.setMinWidth(200);
        uploadButton.setGraphicTextGap(8);

        uploadPicker.setGraphic(uploadButton);

        FXProperties.runOnPropertyChange(fileToUpload -> {
            if (fileToUpload != null) {
                handleImageUpload(fileToUpload);
            }
        }, uploadPicker.selectedFileProperty());

        // Secondary delete button (shows only when image exists)
        Button deleteButton = new Button();
        deleteButton.textProperty().set(I18n.getI18nText(ProgramI18nKeys.DeleteImage));

        SVGPath deleteIcon = SvgIcons.createTrashSVGPath();
        deleteIcon.setStroke(javafx.scene.paint.Color.web("#374151"));
        deleteIcon.setFill(javafx.scene.paint.Color.TRANSPARENT);
        deleteIcon.setStrokeWidth(2);
        deleteIcon.setScaleX(0.7);
        deleteIcon.setScaleY(0.7);

        deleteButton.setGraphic(deleteIcon);
        deleteButton.getStyleClass().add("btn-upload-secondary");
        deleteButton.setPadding(new Insets(10, 20, 10, 20));
        deleteButton.setGraphicTextGap(8);

        deleteButton.setOnAction(e -> handleDeleteEventImage());

        // Show delete button only when image exists (not placeholder)
        deleteButton.visibleProperty().bind(FXProperties.compute(
            eventImageContainer.contentProperty(),
            content -> !(content instanceof VBox)
        ));
        deleteButton.managedProperty().bind(deleteButton.visibleProperty());

        actionButtons.getChildren().addAll(uploadPicker.getView(), deleteButton);

        infoSection.getChildren().addAll(infoCard, actionButtons);
        return infoSection;
    }

    /**
     * Updates the event cover path when the event changes.
     */
    private void updateEventCoverPath(Event event) {
        if (event == null) {
            eventCoverCloudImagePath = null;
            eventImageContainer.setContent(null);
            return;
        }

        eventCoverCloudImagePath = ModalityCloudImageService.eventCoverImagePath(event, null);
        loadEventCoverImage();
    }

    /**
     * Loads the event cover image from Cloudinary.
     */
    private void loadEventCoverImage() {
        if (eventCoverCloudImagePath == null) {
            return;
        }

        ModalityCloudImageService.loadHdpiImage(eventCoverCloudImagePath, EVENT_IMAGE_SIZE, EVENT_IMAGE_SIZE, eventImageContainer, this::createImagePlaceholder)
            .onComplete(ar -> {
                imageUploadProgressIndicator.setVisible(false);
                if (ar.succeeded()) {
                    // Switch to has-image styling (solid border)
                    eventImageContainer.getStyleClass().remove("image-preview-container");
                    eventImageContainer.getStyleClass().add("image-preview-has-image");
                } else {
                    // Keep placeholder styling
                    eventImageContainer.setContent(createImagePlaceholder());
                }
            });
    }

    /**
     * Handles image upload.
     */
    private void handleImageUpload(dev.webfx.platform.file.File fileToUpload) {
        if (eventCoverCloudImagePath == null) {
            Console.log("Event cover image path is null, cannot upload");
            return;
        }

        imageUploadProgressIndicator.setVisible(true);
        // Load the image from the uploaded file
        javafx.scene.image.Image originalImage = new javafx.scene.image.Image(fileToUpload.getObjectURL(), true);
        FXProperties.runOnPropertiesChange(property -> {
            if (originalImage.progressProperty().get() == 1) {
                // Convert the image to PNG format before uploading
                ModalityCloudImageService.prepareImageForUpload(originalImage, false, 1, 0, 0, EVENT_IMAGE_SIZE, EVENT_IMAGE_SIZE)
                    .onFailure(e -> {
                        Console.log("Failed to prepare image for upload: " + e);
                        imageUploadProgressIndicator.setVisible(false);
                    })
                    .onSuccess(pngBlob -> {
                        // Upload the PNG blob
                        ModalityCloudImageService.replaceImage(eventCoverCloudImagePath, pngBlob)
                            .inUiThread()
                            .onComplete(ar -> {
                                if (ar.failed()) {
                                    Console.log("Failed to upload image: " + ar.cause());
                                    imageUploadProgressIndicator.setVisible(false);
                                } else {
                                    Console.log("Image uploaded successfully");
                                    loadEventCoverImage();
                                }
                            });
                    });
            }
        }, originalImage.progressProperty());
    }

    /**
     * Handles viewing the full-size image.
     */
    private void handleViewFullSizeImage() {
        if (eventCoverCloudImagePath == null) {
            return;
        }

        // Create a dialog to show the full-size image
        VBox dialogContent = new VBox(12);
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(24));

        // Container for the image - square size to preserve 1:1 aspect ratio
        // Reduced size to account for dialog padding (48px), VBox spacing (12px), and button (~50px) = ~110px total
        MonoPane fullImageContainer = new MonoPane();
        fullImageContainer.setMinSize(300, 300);
        fullImageContainer.setPrefSize(500, 500);
        fullImageContainer.setMaxSize(500, 500);

        ProgressIndicator dialogProgress = new ProgressIndicator();
        dialogProgress.setPrefSize(60, 60);

        // Stack pane to overlay progress indicator - white background container
        StackPane imagePane = new StackPane(fullImageContainer, dialogProgress);
        imagePane.setMinSize(300, 300);
        imagePane.setPrefSize(500, 500);
        imagePane.setMaxSize(500, 500);

        dialogContent.getChildren().add(imagePane);

        Button closeButton = Bootstrap.primaryButton(I18nControls.newButton("close"));
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogContent, FXMainFrameDialogArea.getDialogArea());
        closeButton.setOnAction(e -> dialogCallback.closeDialog());
        dialogContent.getChildren().add(closeButton);

        // Load the full-size image with square dimensions to preserve 1:1 aspect ratio
        // The image will be scaled to fit within 500x500 while preserving proportions
        ModalityCloudImageService.loadHdpiImage(eventCoverCloudImagePath, 500, 500, fullImageContainer, null)
            .onComplete(ar -> dialogProgress.setVisible(false));
    }

    /**
     * Handles deleting the event image.
     */
    private void handleDeleteEventImage() {
        if (eventCoverCloudImagePath == null) {
            return;
        }

        String confirmationMessage = I18n.getI18nText(ProgramI18nKeys.DeleteImageConfirmation);
        ModalityDialog.showConfirmationDialog(confirmationMessage, () -> {
            imageUploadProgressIndicator.setVisible(true);
            ModalityCloudImageService.deleteImage(eventCoverCloudImagePath)
                .inUiThread()
                .onSuccess(e -> {
                    // Reset to placeholder
                    eventImageContainer.setContent(createImagePlaceholder());
                    // Switch back to placeholder styling (dashed border)
                    eventImageContainer.getStyleClass().remove("image-preview-has-image");
                    eventImageContainer.getStyleClass().add("image-preview-container");
                    imageUploadProgressIndicator.setVisible(false);
                })
                .onFailure(error -> {
                    Console.log("Failed to delete image: " + error);
                    imageUploadProgressIndicator.setVisible(false);
                });
        });
    }

    /**
     * Loads program data from database and builds the UI.
     * First queries all teaching scheduled items, then loads attendance counts
     * and actual media file counts (from Media table) for all scheduled items.
     */
    private void loadProgramData() {
        Event event = eventProperty.get();
        if (event == null) {
            daysContainer.getChildren().clear();
            return;
        }

        // Don't load data if program hasn't been generated yet
        if (!Boolean.TRUE.equals(programModel.programGeneratedProperty().getValue())) {
            Console.log("ProgramStep3View: Program not generated yet, skipping data load");
            daysContainer.getChildren().clear();
            return;
        }

        // Show loading indicator
        showLoadingIndicator();

        Console.log("Loading program data for event: " + event.getName());

        // Query: Get all program scheduled items
        entityStore.<ScheduledItem>executeQuery(
            "select name, label.(en,fr,es,pt,de,zhs,zht,el,vi), date, startTime, endTime, " +
            "timeline..(startTime,endTime,item.name), item.name, cancelled, bookableScheduledItem " +
            "from ScheduledItem si " +
            "where event=? and item.family.code=? and bookableScheduledItem!=si " +
            "order by date, coalesce(timeline..startTime, startTime)",
            event, KnownItemFamily.TEACHING.getCode()
        )
        .inUiThread()
        .onFailure(error -> {
            Console.log("Query failed: " + error);
            buildDaySections(EntityList.create(null, entityStore));
        })
        .onSuccess(scheduledItemsList -> {
            Console.log("Query succeeded, found " + scheduledItemsList.size() + " items");

            if (scheduledItemsList.isEmpty()) {
                buildDaySections(scheduledItemsList);
                return;
            }

            // Load counts using ProgramModel helper method
            programModel.loadScheduledItemCounts(scheduledItemsList)
                .inUiThread()
                .onSuccess(counts -> {
                    // Update local count maps
                    attendanceCountsByScheduledItemId.clear();
                    attendanceCountsByScheduledItemId.putAll(counts.attendanceCounts);

                    audioOfferedByScheduledItemId.clear();
                    audioOfferedByScheduledItemId.putAll(counts.audioOffered);

                    audioMediaCountsByScheduledItemId.clear();
                    audioMediaCountsByScheduledItemId.putAll(counts.audioCounts);

                    videoOfferedByScheduledItemId.clear();
                    videoOfferedByScheduledItemId.putAll(counts.videoOffered);

                    videoMediaCountsByScheduledItemId.clear();
                    videoMediaCountsByScheduledItemId.putAll(counts.videoCounts);

                    buildDaySections(scheduledItemsList);
                })
                .onFailure(error -> {
                    Console.log(error);
                    buildDaySections(scheduledItemsList);
                });
        });
    }

    /**
     * Builds the day sections from scheduled items.
     */
    private void buildDaySections(EntityList scheduledItems) {
        daysContainer.getChildren().clear();

        if (scheduledItems.isEmpty()) {
            Label noDataLabel = I18nControls.newLabel(ProgramI18nKeys.NoScheduledItemsFound);
            noDataLabel.getStyleClass().add("no-data-message");
            noDataLabel.setPadding(new Insets(50));
            VBox noDataBox = new VBox(noDataLabel);
            noDataBox.setAlignment(Pos.CENTER);
            daysContainer.getChildren().add(noDataBox);
            return;
        }

        // Group scheduled items by date
        Map<LocalDate, List<ScheduledItem>> itemsByDate = new TreeMap<>();
        for (Object scheduledItem : scheduledItems) {
            ScheduledItem si = (ScheduledItem) scheduledItem;
            LocalDate date = si.getDate();
            itemsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(si);
        }

        // Create a section for each day
        for (Map.Entry<LocalDate, List<ScheduledItem>> entry : itemsByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<ScheduledItem> dayItems = entry.getValue();

            VBox daySection = createDaySection(date, dayItems);
            daysContainer.getChildren().add(daySection);
        }
    }

    /**
     * Creates a day section with all sessions for that date.
     */
    private VBox createDaySection(LocalDate date, List<ScheduledItem> scheduledItems) {
        VBox daySection = new VBox(24);
        daySection.getStyleClass().add("day-section");
        daySection.setPadding(new Insets(24));
        daySection.setMaxWidth(MAX_WIDTH);

        // Day header
        HBox dayHeader = createDayHeader(date, scheduledItems.size());
        daySection.getChildren().add(dayHeader);

        // Session cards
        VBox sessionsContainer = new VBox(12);
        for (ScheduledItem item : scheduledItems) {
            HBox sessionCard = createSessionCard(item);
            sessionsContainer.getChildren().add(sessionCard);
        }
        daySection.getChildren().add(sessionsContainer);

        // Add session button
        Button addSessionButton = ModalityStyle.primaryButtonWithIcon(
            I18nControls.newButton(ProgramI18nKeys.AddSession),
            SvgIcons.createPlusIconPath()
        );
        addSessionButton.setOnAction(e -> handleAddSession(date));
        addSessionButton.getStyleClass().add("add-session-btn");
        daySection.getChildren().add(addSessionButton);

        return daySection;
    }

    /**
     * Creates the day header with day name, date, and session count.
     */
    private HBox createDayHeader(LocalDate date, int sessionCount) {
        HBox dayHeader = new HBox();
        dayHeader.setAlignment(Pos.CENTER_LEFT);
        dayHeader.setSpacing(12);

        // Day icon
        StackPane dayIcon = new StackPane();
        dayIcon.setPrefSize(48, 48);
        dayIcon.getStyleClass().add("day-icon");
        SVGPath calendarIcon = SvgIcons.createCalendarPath();
        calendarIcon.setFill(javafx.scene.paint.Color.WHITE);
        calendarIcon.setScaleX(1.5);
        calendarIcon.setScaleY(1.5);
        dayIcon.getChildren().add(calendarIcon);

        // Day details
        VBox dayDetails = new VBox(4);

        String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
        Label dayLabel = Bootstrap.h4(new Label(dayName));
        Bootstrap.strong(dayLabel);

        Label dateLabel = new Label(DATE_FORMATTER.format(date));
        dateLabel.getStyleClass().add("day-status-label");

        dayDetails.getChildren().addAll(dayLabel, dateLabel);

        // Session count
        VBox dayStatus = new VBox();
        dayStatus.setAlignment(Pos.CENTER_RIGHT);
        Label countLabel = Bootstrap.strong(new Label());
        Object sessionKey = sessionCount == 1 ? ProgramI18nKeys.Session : ProgramI18nKeys.Sessions;
        countLabel.textProperty().bind(I18n.i18nTextProperty(sessionKey, sessionCount));
        dayStatus.getChildren().add(countLabel);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        dayHeader.getChildren().addAll(dayIcon, dayDetails, spacer, dayStatus);
        return dayHeader;
    }

    /**
     * Creates a session card for a scheduled item.
     */
    private HBox createSessionCard(ScheduledItem scheduledItem) {
        HBox sessionCard = new HBox(16);
        sessionCard.setAlignment(Pos.CENTER_LEFT);
        sessionCard.setPadding(new Insets(17));
        sessionCard.getStyleClass().add("session-card");

        // Time range
        HBox timeRange = createTimeRange(scheduledItem);

        // Session title
        String title = scheduledItem.getName();
        if (title == null || title.isEmpty()) {
            title = scheduledItem.getTimeline() != null && scheduledItem.getTimeline().getItem() != null
                ? scheduledItem.getTimeline().getItem().getName()
                : I18n.getI18nText(ProgramI18nKeys.UnnamedSession);
        }
        Label sessionTitle = Bootstrap.h5(new Label(title));
        sessionTitle.setWrapText(true);
        sessionTitle.setMaxWidth(300);

        // Strike through title if session is cancelled
        boolean isCancelled = Boolean.TRUE.equals(scheduledItem.isCancelled());
        if (isCancelled) {
            sessionTitle.getStyleClass().add("session-title-cancelled");
        } else {
            sessionTitle.getStyleClass().add("session-title");
        }

        // Cancelled badge
        Label cancelledBadge = null;
        if (isCancelled) {
            cancelledBadge = I18nControls.newLabel(ProgramI18nKeys.CancelledBadge);
            Bootstrap.dangerBadge(cancelledBadge);
            cancelledBadge.setMinWidth(85);
            cancelledBadge.setAlignment(Pos.CENTER);
            cancelledBadge.setPadding(new Insets(4, 8, 4, 8));
        }

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Get counts
        Object bookableScheduledItemId = scheduledItem.getBookableScheduledItem() != null
            ? scheduledItem.getBookableScheduledItem().getPrimaryKey()
            : null;
        int attendanceCount = bookableScheduledItemId != null
            ? attendanceCountsByScheduledItemId.getOrDefault(bookableScheduledItemId, 0)
            : 0;

        Object scheduledItemId = scheduledItem.getPrimaryKey();
        boolean audioOffered = scheduledItemId != null
            && audioOfferedByScheduledItemId.getOrDefault(scheduledItemId, false);
        int audioCount = scheduledItemId != null
            ? audioMediaCountsByScheduledItemId.getOrDefault(scheduledItemId, 0)
            : 0;
        boolean videoOffered = scheduledItemId != null
            && videoOfferedByScheduledItemId.getOrDefault(scheduledItemId, false);
        int videoCount = scheduledItemId != null
            ? videoMediaCountsByScheduledItemId.getOrDefault(scheduledItemId, 0)
            : 0;

        // Displays
        HBox participants = createParticipantsDisplay(attendanceCount);
        HBox audioDisplay = createAudioMediaDisplay(audioCount, audioOffered);
        HBox videoDisplay = createVideoMediaDisplay(videoCount, videoOffered);
        HBox actions = createActionButtons(scheduledItem);

        // Build card
        if (cancelledBadge != null) {
            sessionCard.getChildren().addAll(timeRange, sessionTitle, cancelledBadge, spacer);
        } else {
            sessionCard.getChildren().addAll(timeRange, sessionTitle, spacer);
        }
        sessionCard.getChildren().addAll(participants, audioDisplay, videoDisplay, actions);
        return sessionCard;
    }

    /**
     * Creates the time range display (start - end).
     */
    private HBox createTimeRange(ScheduledItem scheduledItem) {
        HBox timeRange = new HBox(8);
        timeRange.setAlignment(Pos.CENTER);
        timeRange.setMinWidth(146);

        String startTime = "??:??";
        String endTime = "??:??";

        if (scheduledItem.getStartTime() != null) {
            startTime = formatTime(scheduledItem.getStartTime().toString());
        } else if (scheduledItem.getTimeline() != null && scheduledItem.getTimeline().getStartTime() != null) {
            startTime = formatTime(scheduledItem.getTimeline().getStartTime().toString());
        }

        if (scheduledItem.getEndTime() != null) {
            endTime = formatTime(scheduledItem.getEndTime().toString());
        } else if (scheduledItem.getTimeline() != null && scheduledItem.getTimeline().getEndTime() != null) {
            endTime = formatTime(scheduledItem.getTimeline().getEndTime().toString());
        }

        Label startLabel = new Label(startTime);
        startLabel.getStyleClass().add("time-slot");
        startLabel.setPadding(new Insets(10, 13, 10, 13));

        Label separator = new Label("-");
        separator.getStyleClass().add("time-separator");

        Label endLabel = new Label(endTime);
        endLabel.getStyleClass().add("time-slot");
        endLabel.setPadding(new Insets(10, 13, 10, 13));

        timeRange.getChildren().addAll(startLabel, separator, endLabel);
        return timeRange;
    }

    /**
     * Creates participants count display.
     */
    private HBox createParticipantsDisplay(int count) {
        HBox participants = new HBox(8);
        participants.setAlignment(Pos.CENTER_RIGHT);
        participants.setPadding(new Insets(9, 13, 9, 13));

        SVGPath icon = SvgIcons.createUserSVGPath();
        icon.setFill(javafx.scene.paint.Color.web("#0891b2"));
        icon.setScaleX(0.8);
        icon.setScaleY(0.8);

        Label countLabel = new Label(String.valueOf(count));
        countLabel.getStyleClass().add("participant-count");

        participants.getChildren().addAll(icon, countLabel);
        return participants;
    }

    /**
     * Creates audio recordings count display with microphone icon (same as Step 2).
     * Shows green icon with count when offered, gray strikethrough icon when not offered.
     */
    private HBox createAudioMediaDisplay(int count, boolean isOffered) {
        HBox audioMedia = new HBox(6);
        audioMedia.setAlignment(Pos.CENTER_RIGHT);
        audioMedia.setPadding(new Insets(9, 13, 9, 13));
        audioMedia.setMinWidth(45); // Fixed minimum width for alignment (compact for single digits)

        // Use the same microphone icon as Step 2 with same styling
        SVGPath icon = new SVGPath();
        icon.setContent("M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3zm5.91-3c-.49 0-.9.36-.98.85C16.52 14.21 14.47 16 12 16s-4.52-1.79-4.93-4.15c-.08-.49-.49-.85-.98-.85-.61 0-1.09.54-1 1.14.49 3 2.89 5.35 5.91 5.78V20c0 .55.45 1 1 1s1-.45 1-1v-2.08c3.02-.43 5.42-2.78 5.91-5.78.1-.6-.39-1.14-1-1.14z");
        icon.setScaleX(0.9);
        icon.setScaleY(0.9);

        if (isOffered) {
            icon.setFill(javafx.scene.paint.Color.web("#059669"));
            Label countLabel = new Label(String.valueOf(count));
            countLabel.getStyleClass().add("media-count-available");
            countLabel.setMinWidth(12); // Compact width for single digit
            countLabel.setAlignment(Pos.CENTER_RIGHT);
            audioMedia.getChildren().addAll(icon, countLabel);
        } else {
            // Gray icon with strikethrough when not offered (like template)
            icon.setFill(javafx.scene.paint.Color.web("#94a3b8"));

            // Create strikethrough line
            Line strikethrough = new Line(-10, -10, 10, 10);
            strikethrough.setStroke(javafx.scene.paint.Color.web("#94a3b8"));
            strikethrough.setStrokeWidth(2);

            StackPane iconWithStrike = new StackPane(icon, strikethrough);

            // Add empty label for consistent spacing
            Label emptyLabel = new Label("");
            emptyLabel.setMinWidth(12);
            audioMedia.getChildren().addAll(iconWithStrike, emptyLabel);
        }

        return audioMedia;
    }

    /**
     * Creates video recordings count display with video camera icon (consistent with Step 2 style).
     * Shows green icon with count when offered, gray strikethrough icon when not offered.
     */
    private HBox createVideoMediaDisplay(int count, boolean isOffered) {
        HBox videoMedia = new HBox(6);
        videoMedia.setAlignment(Pos.CENTER_RIGHT);
        videoMedia.setPadding(new Insets(9, 13, 9, 13));
        videoMedia.setMinWidth(45); // Fixed minimum width for alignment (compact for single digits)

        // Use consistent video camera icon with same styling as Step 2
        SVGPath icon = new SVGPath();
        icon.setContent("M17 10.5V7c0-.55-.45-1-1-1H4c-.55 0-1 .45-1 1v10c0 .55.45 1 1 1h12c.55 0 1-.45 1-1v-3.5l4 4v-11l-4 4z");
        icon.setScaleX(0.9);
        icon.setScaleY(0.9);

        if (isOffered) {
            icon.setFill(javafx.scene.paint.Color.web("#059669"));
            Label countLabel = new Label(String.valueOf(count));
            countLabel.getStyleClass().add("media-count-available");
            countLabel.setMinWidth(12); // Compact width for single digit
            countLabel.setAlignment(Pos.CENTER_RIGHT);
            videoMedia.getChildren().addAll(icon, countLabel);
        } else {
            // Gray icon with strikethrough when not offered (like template)
            icon.setFill(javafx.scene.paint.Color.web("#94a3b8"));

            // Create strikethrough line
            Line strikethrough = new Line(-10, -10, 10, 10);
            strikethrough.setStroke(javafx.scene.paint.Color.web("#94a3b8"));
            strikethrough.setStrokeWidth(2);

            StackPane iconWithStrike = new StackPane(icon, strikethrough);

            // Add empty label for consistent spacing
            Label emptyLabel = new Label("");
            emptyLabel.setMinWidth(12);
            videoMedia.getChildren().addAll(iconWithStrike, emptyLabel);
        }

        return videoMedia;
    }

    /**
     * Creates action buttons (edit, delete, cancel/uncancel).
     */
    private HBox createActionButtons(ScheduledItem scheduledItem) {
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button editButton = new Button();
        ModalityStyle.outlinePrimaryEditButton(editButton);
        editButton.setOnAction(e -> handleEditSession(scheduledItem));

        Object scheduledItemId = scheduledItem.getPrimaryKey();
        int audioCount = scheduledItemId != null
            ? audioMediaCountsByScheduledItemId.getOrDefault(scheduledItemId, 0)
            : 0;
        int videoCount = scheduledItemId != null
            ? videoMediaCountsByScheduledItemId.getOrDefault(scheduledItemId, 0)
            : 0;

        Object bookableScheduledItemId = scheduledItem.getBookableScheduledItem() != null
            ? scheduledItem.getBookableScheduledItem().getPrimaryKey()
            : null;
        int attendanceCount = bookableScheduledItemId != null
            ? attendanceCountsByScheduledItemId.getOrDefault(bookableScheduledItemId, 0)
            : 0;

        boolean hasMediaOrUsers = (audioCount > 0) || (videoCount > 0) || (attendanceCount > 0);

        boolean isCancelled = Boolean.TRUE.equals(scheduledItem.isCancelled());
        if (isCancelled) {
            Button uncancelButton = new Button();
            ModalityStyle.outlineSuccessButtonWithIcon(uncancelButton, createUncancelIcon());
            uncancelButton.setOnAction(e -> handleUncancelSession(scheduledItem));

            if (hasMediaOrUsers) {
                actions.getChildren().addAll(editButton, uncancelButton);
            } else {
                Button deleteButton = new Button();
                ModalityStyle.outlineDangerDeleteButton(deleteButton);
                deleteButton.setOnAction(e -> handleDeleteSession(scheduledItem));
                actions.getChildren().addAll(editButton, deleteButton, uncancelButton);
            }
        } else {
            Button cancelButton = new Button();
            ModalityStyle.outlineWarningButtonWithIcon(cancelButton, createCancelIcon());
            cancelButton.setOnAction(e -> handleCancelSession(scheduledItem));

            if (hasMediaOrUsers) {
                actions.getChildren().addAll(editButton, cancelButton);
            } else {
                Button deleteButton = new Button();
                ModalityStyle.outlineDangerDeleteButton(deleteButton);
                deleteButton.setOnAction(e -> handleDeleteSession(scheduledItem));
                actions.getChildren().addAll(editButton, deleteButton, cancelButton);
            }
        }

        return actions;
    }

    /**
     * Creates the cancel icon (X).
     */
    private SVGPath createCancelIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M 3 3 L 13 13 M 13 3 L 3 13");
        icon.setStroke(javafx.scene.paint.Color.web("#f59e0b"));
        icon.setStrokeWidth(2);
        icon.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        return icon;
    }

    /**
     * Creates the uncancel icon (checkmark).
     */
    private SVGPath createUncancelIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M 3 8 L 6 11 L 13 4");
        icon.setStroke(javafx.scene.paint.Color.web("#059669"));
        icon.setStrokeWidth(2);
        icon.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        icon.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        return icon;
    }

    /**
     * Formats a time string (HH:mm).
     */
    private String formatTime(String time) {
        if (time == null) return "??:??";
        return time.substring(0, Math.min(5, time.length()));
    }

    /**
     * Builds the main UI structure.
     */
    private VBox buildUi() {
        VBox container = new VBox(32);
        container.setAlignment(Pos.TOP_CENTER);

        // Title at top
        container.getChildren().add(titleContainer);

        // Step indicator
        container.getChildren().add(stepIndicatorContainer);

        // Short description warning (shown when event has no short description)
        container.getChildren().add(buildShortDescriptionWarning());

        // Event image upload
        container.getChildren().add(buildEventImageUpload());

        // Program schedule below
        daysContainer.setFillWidth(true);
        VBox.setVgrow(daysContainer, Priority.ALWAYS);
        container.getChildren().add(daysContainer);

        return container;
    }

    /**
     * Shows the loading indicator.
     */
    private void showLoadingIndicator() {
        daysContainer.getChildren().clear();
        VBox loadingBox = new VBox(loadingIndicator);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(50));
        daysContainer.getChildren().add(loadingBox);
    }

    // Event handlers

    private void handleAddSession(LocalDate date) {
        SessionDialog.show(null, date, entityStore, programModel, this::loadProgramData);
    }

    private void handleEditSession(ScheduledItem scheduledItem) {
        SessionDialog.show(scheduledItem, null, entityStore, programModel, this::loadProgramData);
    }

    private void handleDeleteSession(ScheduledItem scheduledItem) {
        String confirmationMessage = I18n.getI18nText(ProgramI18nKeys.DeleteSessionConfirmation);
        ModalityDialog.showConfirmationDialog(confirmationMessage, () -> deleteSession(scheduledItem));
    }

    private void deleteSession(ScheduledItem scheduledItem) {
        // Create local UpdateStore for immediate save operation (not part of template editing workflow)
        UpdateStore updateStore = UpdateStore.createAbove(entityStore);
        ScheduledItem itemToDelete = updateStore.updateEntity(scheduledItem);
        updateStore.deleteEntity(itemToDelete);

        updateStore.submitChanges()
            .onSuccess(result -> loadProgramData())
            .onFailure(Console::log);
    }

    private void handleCancelSession(ScheduledItem scheduledItem) {
        Object bookableScheduledItemId = scheduledItem.getBookableScheduledItem() != null
            ? scheduledItem.getBookableScheduledItem().getPrimaryKey()
            : null;
        int attendanceCount = bookableScheduledItemId != null
            ? attendanceCountsByScheduledItemId.getOrDefault(bookableScheduledItemId, 0)
            : 0;

        String sessionName = scheduledItem.getName() != null
            ? scheduledItem.getName()
            : I18n.getI18nText(ProgramI18nKeys.UnnamedSession);
        String confirmationMessage = I18n.getI18nText(
            ProgramI18nKeys.CancelSessionConfirmation,
            sessionName
        ) + "\n\n" + (attendanceCount > 0
            ? I18n.getI18nText(ProgramI18nKeys.CancelSessionWarning, attendanceCount)
            : I18n.getI18nText(ProgramI18nKeys.SessionMarkedCancelled));

        ModalityDialog.showConfirmationDialog(confirmationMessage, () -> cancelSession(scheduledItem));
    }

    private void cancelSession(ScheduledItem scheduledItem) {
        // Create local UpdateStore for immediate save operation (not part of template editing workflow)
        UpdateStore updateStore = UpdateStore.createAbove(entityStore);
        ScheduledItem itemToCancel = updateStore.updateEntity(scheduledItem);
        itemToCancel.setCancelled(true);

        updateStore.submitChanges()
            .onSuccess(result -> loadProgramData())
            .onFailure(Console::log);
    }

    private void handleUncancelSession(ScheduledItem scheduledItem) {
        Object bookableScheduledItemId = scheduledItem.getBookableScheduledItem() != null
            ? scheduledItem.getBookableScheduledItem().getPrimaryKey()
            : null;
        int attendanceCount = bookableScheduledItemId != null
            ? attendanceCountsByScheduledItemId.getOrDefault(bookableScheduledItemId, 0)
            : 0;

        String sessionName = scheduledItem.getName() != null
            ? scheduledItem.getName()
            : I18n.getI18nText(ProgramI18nKeys.UnnamedSession);
        String confirmationMessage = I18n.getI18nText(
            ProgramI18nKeys.UncancelSessionConfirmation,
            sessionName
        ) + "\n\n" + (attendanceCount > 0
            ? I18n.getI18nText(ProgramI18nKeys.UncancelSessionMessage, attendanceCount)
            : I18n.getI18nText(ProgramI18nKeys.SessionMarkedActive));

        ModalityDialog.showConfirmationDialog(confirmationMessage, () -> uncancelSession(scheduledItem));
    }

    private void uncancelSession(ScheduledItem scheduledItem) {
        // Create local UpdateStore for immediate save operation (not part of template editing workflow)
        UpdateStore updateStore = UpdateStore.createAbove(entityStore);
        ScheduledItem itemToUncancel = updateStore.updateEntity(scheduledItem);
        itemToUncancel.setCancelled(false);

        updateStore.submitChanges()
            .onSuccess(result -> loadProgramData())
            .onFailure(Console::log);
    }
}
