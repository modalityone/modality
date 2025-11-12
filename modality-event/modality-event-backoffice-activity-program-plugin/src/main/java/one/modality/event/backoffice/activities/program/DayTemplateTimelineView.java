package one.modality.event.backoffice.activities.program;

import dev.webfx.extras.panes.HPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.theme.shape.ShapeTheme;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Booleans;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.validation.ValidationSupport;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Timeline;

import java.time.LocalTime;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static one.modality.event.backoffice.activities.program.DatesToStringConversion.isLocalTimeTextValid;

/**
 * View component for a single timeline entry within a day template.
 * This view provides a complete form for editing one teaching session (timeline) in the program.
 * Each timeline represents when a teaching occurs during the day and what
 * recordings (audio/video) are available.
 *
 * <p><b>Item:</b> All timelines automatically use the hardcoded session program item (KnownItem.PROGRAM_SESSION).
 *
 * <p><b>Form Fields:</b>
 * <ul>
 *   <li><b>Start Time:</b> When the session begins (e.g., "9:00")</li>
 *   <li><b>End Time:</b> When the session ends (e.g., "12:00")</li>
 *   <li><b>Name:</b> Optional custom name for this timeline</li>
 *   <li><b>Audio Toggle:</b> Whether audio recordings are offered</li>
 *   <li><b>Video Toggle:</b> Whether video recordings are offered</li>
 *   <li><b>Duplicate Button:</b> Clone this timeline entry</li>
 *   <li><b>Delete Button:</b> Remove this timeline</li>
 * </ul>
 *
 * <p><b>Visual Layout:</b>
 * <pre>
 * [9:00] to [12:00] [Name] [🔊] [📹] [📋] [🗑️]
 * </pre>
 *
 * <p><b>Validation:</b>
 * The form includes real-time validation for:
 * <ul>
 *   <li>Start time format (must be valid HH:mm)</li>
 *   <li>End time format (must be valid HH:mm)</li>
 * </ul>
 *
 * <p><b>Bidirectional Sync:</b>
 * The view maintains bidirectional synchronization between UI and model:
 * <ul>
 *   <li>UI → Model: Changes are immediately saved to the Timeline entity</li>
 *   <li>Model → UI: Entity changes update the UI (e.g., when resetting)</li>
 * </ul>
 *
 * <p><b>Custom Layout:</b>
 * Uses a custom HPane with fixed-width columns for time fields and flexible-width
 * columns for item selector and name field, ensuring consistent alignment across
 * multiple timeline rows.
 *
 * @author David Hello
 * @author Bruno Salmon
 *
 * @see DayTemplateTimelineModel
 * @see Timeline
 * @see DayTemplateView
 */
final class DayTemplateTimelineView implements ButtonFactoryMixin {

    /**
     * The model managing the Timeline entity and providing validation support.
     */
    private final DayTemplateTimelineModel dayTemplateTimelineModel;

    /**
     * The root view node containing all form fields in a custom horizontal layout.
     */
    private final Region view;

    /**
     * Text field for entering the start time in HH:mm format (e.g., "9:00").
     */
    private final TextField fromTextField = new TextField();

    /**
     * Text field for entering the end time in HH:mm format (e.g., "12:00").
     */
    private final TextField untilTextField = new TextField();

    /**
     * Text field for entering an optional custom name for this timeline.
     */
    private final TextField nameTextField = new TextField();

    /**
     * Toggle button for audio availability with icon, strike-through, and color changes.
     */
    private final StackPane audioToggleButton;

    /**
     * Toggle button for video availability with icon, strike-through, and color changes.
     */
    private final StackPane videoToggleButton;

    /**
     * Constructs a new DayTemplateTimelineView for editing a timeline entry.
     * Initializes all form fields, sets up bidirectional bindings between UI and model,
     * and configures the audio/video toggle buttons.
     *
     * @param dayTemplateTimelineModel The model managing this timeline's data
     */
    DayTemplateTimelineView(DayTemplateTimelineModel dayTemplateTimelineModel) {
        this.dayTemplateTimelineModel = dayTemplateTimelineModel;
        // Register callback to sync UI when model changes (e.g., when resetting)
        dayTemplateTimelineModel.setSyncUiFromModelRunnable(this::syncUiFromModel);
        Timeline timeline = dayTemplateTimelineModel.getTimeline();
        // Create toggle buttons with custom styling
        audioToggleButton = createMediaToggleButton(true, timeline::isAudioOffered, timeline::setAudioOffered);
        videoToggleButton = createMediaToggleButton(false, timeline::isVideoOffered, timeline::setVideoOffered);
        view = buildUi();
    }

    /**
     * Returns the root JavaFX node for this view component.
     *
     * @return The Region containing all form fields in a custom horizontal layout
     */
    Node getView() {
        return view;
    }

    /**
     * Returns the Timeline entity being edited by this view.
     *
     * @return The Timeline entity managed by the model
     */
    private Timeline getTimeline() {
        return dayTemplateTimelineModel.getTimeline();
    }

    /**
     * Returns the validation support instance for form validation.
     *
     * @return ValidationSupport for validating form fields
     */
    private ValidationSupport getValidationSupport() {
        return dayTemplateTimelineModel.getValidationSupport();
    }

    /**
     * Synchronizes all UI fields from the model state.
     * Called when the model is reset (e.g., cancel changes or reload from database).
     * Updates all form fields to reflect the current values in the Timeline entity.
     */
    private void syncUiFromModel() {
        syncStartTimeUiFromModel();
        syncEndTimeUiFromModel();
        syncNameUiFromModel();
        syncAudioUiFromModel();
        syncVideoUiFromModel();
    }

    /**
     * Updates the start time text field to match the model's current start time.
     * Formats the time in HH:mm format (e.g., "09:00").
     */
    private void syncStartTimeUiFromModel() {
        LocalTime startTime = getTimeline().getStartTime();
        if (startTime != null)
            fromTextField.setText(startTime.format(DatesToStringConversion.TIME_FORMATTER));
    }

    /**
     * Updates the model's start time from the start time text field.
     * Only updates if the entered text is a valid time format.
     * Called automatically when the user changes the text.
     */
    private void syncStartTimeModelFromUi() {
        String text = fromTextField.getText();
        if (isLocalTimeTextValid(text)) {
            getTimeline().setStartTime(LocalTime.parse(text));
        }
    }

    /**
     * Updates the end time text field to match the model's current end time.
     * Formats the time in HH:mm format (e.g., "12:00").
     */
    private void syncEndTimeUiFromModel() {
        LocalTime endTime = getTimeline().getEndTime();
        if (endTime != null)
            untilTextField.setText(endTime.format(DatesToStringConversion.TIME_FORMATTER));
    }

    /**
     * Updates the model's end time from the end time text field.
     * Only updates if the entered text is a valid time format.
     * Called automatically when the user changes the text.
     */
    private void syncEndTimeModelFromUi() {
        String text = untilTextField.getText();
        if (isLocalTimeTextValid(text)) {
            getTimeline().setEndTime(LocalTime.parse(text));
        }
    }

    /**
     * Updates the name text field to match the model's current name.
     */
    private void syncNameUiFromModel() {
        String name = getTimeline().getName();
        if (name != null)
            nameTextField.setText(name);
    }

    /**
     * Updates the model's name from the name text field.
     * Called automatically when the user changes the text.
     */
    private void syncNameModelFromUi() {
        String name = nameTextField.getText();
        if (name != null)
            getTimeline().setName(name);
    }

    /**
     * Creates a media toggle button (audio or video) with icon, strike-through, and color styling.
     * When active: green (audio) or amber (video) icon with colored background and border.
     * When inactive: grey icon with red diagonal strike-through line.
     *
     * @param isAudio true for audio button, false for video button
     * @param stateGetter supplier to get current state
     * @param stateSetter consumer to set new state
     * @return StackPane containing the styled toggle button
     */
    private StackPane createMediaToggleButton(boolean isAudio, Supplier<Boolean> stateGetter, Consumer<Boolean> stateSetter) {
        // Create icon
        SVGPath icon = isAudio ? SvgIcons.createSoundIconPath() : SvgIcons.createVideoIconPath();
        icon.setFill(Color.web("#94a3b8")); // Default grey color

        // Create strike-through line (red diagonal)
        javafx.scene.shape.Line strikeLine = new javafx.scene.shape.Line(0, 0, 28, 0);
        strikeLine.setStroke(Color.web("#ef4444")); // Red color
        strikeLine.setStrokeWidth(2);
        strikeLine.getTransforms().add(new javafx.scene.transform.Rotate(-45, 14, 0));
        strikeLine.setVisible(false); // Initially hidden

        // Container for icon and strike line
        StackPane container = new StackPane(icon, strikeLine);
        container.setMinSize(36, 36);
        container.setMaxSize(36, 36);
        container.getStyleClass().add("timeline-container-default");
        container.setCursor(Cursor.HAND);

        // Update styling based on state
        Runnable updateStyling = () -> {
            boolean isActive = Booleans.isTrue(stateGetter.get());
            // Clear all style classes first
            container.getStyleClass().removeAll("timeline-container-default", "timeline-container-available", "timeline-container-partial");

            if (isActive) {
                if (isAudio) {
                    // Audio active: green
                    icon.setFill(Color.web("#10b981"));
                    container.getStyleClass().add("timeline-container-available");
                } else {
                    // Video active: amber/orange
                    icon.setFill(Color.web("#f59e0b"));
                    container.getStyleClass().add("timeline-container-partial");
                }
                strikeLine.setVisible(false);
            } else {
                // Inactive: grey with red strike
                icon.setFill(Color.web("#94a3b8"));
                container.getStyleClass().add("timeline-container-default");
                strikeLine.setVisible(true);
            }
        };

        // Initial state
        updateStyling.run();

        // Click handler to toggle state
        container.setOnMouseClicked(e -> {
            stateSetter.accept(!stateGetter.get());
            updateStyling.run();
        });

        return container;
    }

    /**
     * Updates the audio toggle button to match the model's audio offered state.
     * Shows green icon if audio is offered, grey with strike if not.
     */
    private void syncAudioUiFromModel() {
        updateMediaToggleButton(audioToggleButton, true, getTimeline().isAudioOffered());
    }

    /**
     * Updates the video toggle button to match the model's video offered state.
     * Shows amber icon if video is offered, grey with strike if not.
     */
    private void syncVideoUiFromModel() {
        updateMediaToggleButton(videoToggleButton, false, getTimeline().isVideoOffered());
    }

    /**
     * Updates a media toggle button's styling based on its state.
     *
     * @param button the toggle button to update
     * @param isAudio true for audio button, false for video button
     * @param isActive true if media is offered, false otherwise
     */
    private void updateMediaToggleButton(StackPane button, boolean isAudio, Boolean isActive) {
        if (button.getChildren().size() < 2) return;

        SVGPath icon = (SVGPath) button.getChildren().get(0);
        javafx.scene.shape.Line strikeLine = (javafx.scene.shape.Line) button.getChildren().get(1);

        // Clear all style classes first
        button.getStyleClass().removeAll("timeline-container-default", "timeline-container-available", "timeline-container-partial");

        if (Booleans.isTrue(isActive)) {
            if (isAudio) {
                // Audio active: green
                icon.setFill(Color.web("#10b981"));
                button.getStyleClass().add("timeline-container-available");
            } else {
                // Video active: amber/orange
                icon.setFill(Color.web("#f59e0b"));
                button.getStyleClass().add("timeline-container-partial");
            }
            strikeLine.setVisible(false);
        } else {
            // Inactive: grey with red strike
            icon.setFill(Color.web("#94a3b8"));
            button.getStyleClass().add("timeline-container-default");
            strikeLine.setVisible(true);
        }
    }

    /**
     * Builds the user interface for this timeline entry.
     * Creates a horizontal form layout with all fields aligned in columns.
     * Sets up bidirectional bindings, validation rules, and event handlers.
     *
     * <p><b>Layout Structure:</b>
     * <pre>
     * [9:00 (60px)] to [12:00 (60px)] [Name (flexible)] [🔊 (21px)] [📹 (24px)] [📋 (20px)] [🗑️ (20px)]
     * </pre>
     *
     * <p><b>Validation Rules:</b>
     * <ul>
     *   <li>Start time must be valid HH:mm format</li>
     *   <li>End time must be valid HH:mm format</li>
     * </ul>
     *
     * <p><b>Bidirectional Bindings:</b>
     * All fields are connected to the model with automatic synchronization:
     * <ul>
     *   <li>User edits → Immediately saved to Timeline entity</li>
     *   <li>Model changes → UI updated via registered callbacks</li>
     * </ul>
     *
     * @return Custom HPane with all form fields in fixed/flexible column layout
     */
    private Region buildUi() {
        // Set item to hardcoded session program item (KnownItem.PROGRAM_SESSION)
        if (getTimeline().getItem() == null) {
            Item sessionProgramItem = dayTemplateTimelineModel.getProgramModel().getSessionProgramItem();
            if (sessionProgramItem != null) {
                getTimeline().setItem(sessionProgramItem);
            }
        }

        // Configure start time field with validation
        fromTextField.setPromptText("8:46");  // Example format (not translated - shows expected format)
        fromTextField.setAlignment(Pos.CENTER);
        // Note: ValidationTimeFormatIncorrect key is shared with Recurring activity
        getValidationSupport().addValidationRule(fromTextField.textProperty().map(DatesToStringConversion::isLocalTimeTextValid),
            fromTextField,
            I18n.i18nTextProperty("ValidationTimeFormatIncorrect"));

        // Initialize start time from model and set up bidirectional sync
        syncStartTimeUiFromModel();
        FXProperties.runOnPropertyChange(this::syncStartTimeModelFromUi, fromTextField.textProperty());

        // "to" label between start and end times
        Label toLabel = I18nControls.newLabel(ProgramI18nKeys.To);

        // Configure end time field with validation
        untilTextField.setPromptText("13:00");  // Example format (not translated)
        untilTextField.setAlignment(Pos.CENTER);
        // Note: ValidationTimeFormatIncorrect key is shared with Recurring activity
        getValidationSupport().addValidationRule(untilTextField.textProperty().map(DatesToStringConversion::isLocalTimeTextValid),
            untilTextField,
            I18n.i18nTextProperty("ValidationTimeFormatIncorrect"));

        // Initialize end time from model and set up bidirectional sync
        syncEndTimeUiFromModel();
        FXProperties.runOnPropertyChange(this::syncEndTimeModelFromUi, untilTextField.textProperty());

        // Configure name field with localized prompt
        I18nControls.bindI18nPromptProperty(nameTextField, ProgramI18nKeys.NameThisLine);
        syncNameUiFromModel();
        FXProperties.runOnPropertyChange(this::syncNameModelFromUi, nameTextField.textProperty());

        // Create duplicate button (small SVG icon - two overlapping rectangles)
        SVGPath duplicateIcon = new SVGPath();
        duplicateIcon.setContent("M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z");
        duplicateIcon.setFill(Color.web("#6b7280")); // Gray color
        duplicateIcon.setScaleX(0.7); // Make it smaller
        duplicateIcon.setScaleY(0.7);
        MonoPane duplicateButton = SvgIcons.createButtonPane(duplicateIcon, dayTemplateTimelineModel::duplicateTimeline);
        ShapeTheme.createSecondaryShapeFacet(duplicateButton).style(); // Make it gray

        // Create gray delete button
        MonoPane trashButton = SvgIcons.createButtonPane(SvgIcons.createTrashSVGPath(), dayTemplateTimelineModel::removeTemplateTimeLine);
        ShapeTheme.createSecondaryShapeFacet(trashButton).style(); // Make it gray

        // Create custom HPane with precise column widths for alignment across multiple rows
        return new HPane(fromTextField, toLabel, untilTextField, nameTextField, audioToggleButton, videoToggleButton, duplicateButton, trashButton) {
            // Fixed widths for time fields and icons ensure alignment
            private static final double FROM_WIDTH = 60, TO_WIDTH = 20, UNTIL_WIDTH = 60, AUDIO_WIDTH = 36, VIDEO_WIDTH = 36, DUPLICATE_WIDTH = 20, TRASH_WIDTH = 20;
            private static final double HGAP = 5, TOTAL_HGAP = HGAP * 8;

            /**
             * Custom layout implementation with fixed and flexible columns.
             * Name field gets remaining space after fixed-width columns,
             * while time fields and icons have fixed widths for consistent alignment.
             */
            @Override
            protected void layoutChildren(double width, double height) {
                // Calculate remaining width after fixed-width columns
                double remainingWidth = Math.max(0, width - (FROM_WIDTH + TO_WIDTH + UNTIL_WIDTH + AUDIO_WIDTH + VIDEO_WIDTH + DUPLICATE_WIDTH + TRASH_WIDTH + TOTAL_HGAP));
                // Name field gets all remaining width
                double nameWidth = Math.max(100, remainingWidth);

                // Layout children left-to-right with precise positioning
                double x = 0;
                layoutInArea(fromTextField     ,  x +=              0 * HGAP, 0, FROM_WIDTH,      height, Pos.CENTER);
                layoutInArea(toLabel           ,  x += FROM_WIDTH + 1 * HGAP, 0, TO_WIDTH,        height, Pos.CENTER);
                layoutInArea(untilTextField    ,  x += TO_WIDTH   + 1 * HGAP, 0, UNTIL_WIDTH,     height, Pos.CENTER);
                layoutInArea(nameTextField     ,  x += UNTIL_WIDTH + 2 * HGAP, 0, nameWidth,       height, Pos.CENTER_LEFT);
                layoutInArea(audioToggleButton ,  x += nameWidth  + 1 * HGAP, 0, AUDIO_WIDTH,     height, Pos.CENTER);
                layoutInArea(videoToggleButton ,  x += AUDIO_WIDTH + 1 * HGAP, 0, VIDEO_WIDTH,     height, Pos.CENTER);
                layoutInArea(duplicateButton   ,  x += VIDEO_WIDTH + 1 * HGAP, 0, DUPLICATE_WIDTH, height, Pos.CENTER);
                layoutInArea(trashButton       , x + (DUPLICATE_WIDTH + 1 * HGAP), 0, TRASH_WIDTH, height, Pos.CENTER);
            }
        };
    }
}
