package one.modality.event.backoffice.activities.program;

import dev.webfx.extras.panes.HPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.theme.shape.ShapeTheme;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Booleans;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelector;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.validation.ValidationSupport;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Timeline;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

import java.time.LocalTime;
import java.util.Objects;

import static one.modality.event.backoffice.activities.program.DatesToStringConversion.isLocalTimeTextValid;

/**
 * View component for a single timeline entry within a day template.
 * This view provides a complete form for editing one teaching session (timeline) in the program.
 * Each timeline represents when a teaching occurs during the day, what is taught, and what
 * recordings (audio/video) are available.
 *
 * <p><b>Form Fields:</b>
 * <ul>
 *   <li><b>Item Selector:</b> Choose the teaching item (e.g., "Heart Jewel", "Modern Buddhism")</li>
 *   <li><b>Start Time:</b> When the session begins (e.g., "9:00")</li>
 *   <li><b>End Time:</b> When the session ends (e.g., "12:00")</li>
 *   <li><b>Name:</b> Optional custom name for this timeline</li>
 *   <li><b>Audio Toggle:</b> Whether audio recordings are offered</li>
 *   <li><b>Video Toggle:</b> Whether video recordings are offered</li>
 *   <li><b>Delete Button:</b> Remove this timeline</li>
 * </ul>
 *
 * <p><b>Visual Layout:</b>
 * <pre>
 * [Item Selector▼] [9:00] to [12:00] [Name] [🔊] [📹] [🗑️]
 * </pre>
 *
 * <p><b>Validation:</b>
 * The form includes real-time validation for:
 * <ul>
 *   <li>Item selection (must not be null)</li>
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
     * Button selector for choosing the teaching item (e.g., "Heart Jewel").
     * Filtered to show only teaching family items for the current organization.
     */
    private ButtonSelector<Item> itemSelector;

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
     * Green sound icon displayed when audio recordings are offered.
     */
    private final SVGPath audioAvailableIcon = SvgIcons.setSVGPathFill(SvgIcons.createSoundIconPath(), Color.GREEN);

    /**
     * Red (inactive) sound icon displayed when audio recordings are not offered.
     */
    private final SVGPath audioUnavailableIcon = SvgIcons.setSVGPathFill(SvgIcons.createSoundIconInactivePath(), Color.RED);

    /**
     * Toggle button for audio availability, switches between green and red icons.
     */
    private final MonoPane audioToggleButton;

    /**
     * Green video icon displayed when video recordings are offered.
     */
    private final SVGPath videoAvailableIcon = SvgIcons.setSVGPathFill(SvgIcons.createVideoIconPath(), Color.GREEN);

    /**
     * Red (inactive) video icon displayed when video recordings are not offered.
     */
    private final SVGPath videoUnavailableIcon = SvgIcons.setSVGPathFill(SvgIcons.createVideoIconInactivePath(), Color.RED);

    /**
     * Toggle button for video availability, switches between green and red icons.
     */
    private final MonoPane videoToggleButton;

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
        // Create toggle buttons that directly modify the timeline entity
        audioToggleButton = SvgIcons.createToggleButtonPane(audioAvailableIcon, audioUnavailableIcon, timeline::isAudioOffered, timeline::setAudioOffered);
        videoToggleButton = SvgIcons.createToggleButtonPane(videoAvailableIcon, videoUnavailableIcon, timeline::isVideoOffered, timeline::setVideoOffered);
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
        syncItemUiFromModel();
        syncStartTimeUiFromModel();
        syncEndTimeUiFromModel();
        syncNameUiFromModel();
        syncAudioUiFromModel();
        syncVideoUiFromModel();
    }

    /**
     * Updates the item selector UI to match the model's current item.
     */
    private void syncItemUiFromModel() {
        itemSelector.setSelectedItem(getTimeline().getItem());
    }

    /**
     * Updates the model's item from the item selector UI selection.
     * Called automatically when the user selects a different item.
     */
    private void syncItemModelFromUi() {
        getTimeline().setItem(itemSelector.getSelectedItem());
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
     * Updates the audio toggle button icon to match the model's audio offered state.
     * Shows green icon if audio is offered, red icon if not.
     */
    private void syncAudioUiFromModel() {
        audioToggleButton.setContent(Booleans.isTrue(getTimeline().isAudioOffered()) ? audioAvailableIcon : audioUnavailableIcon);
    }

    /**
     * Updates the video toggle button icon to match the model's video offered state.
     * Shows green icon if video is offered, red icon if not.
     */
    private void syncVideoUiFromModel() {
        videoToggleButton.setContent(Booleans.isTrue(getTimeline().isVideoOffered()) ? videoAvailableIcon : videoUnavailableIcon);
    }

    /**
     * Builds the user interface for this timeline entry.
     * Creates a horizontal form layout with all fields aligned in columns.
     * Sets up bidirectional bindings, validation rules, and event handlers.
     *
     * <p><b>Layout Structure:</b>
     * <pre>
     * [Item Selector (flexible)]  [9:00 (60px)] to [12:00 (60px)] [Name (flexible)] [🔊 (21px)] [📹 (24px)] [🗑️ (20px)]
     * </pre>
     *
     * <p><b>Validation Rules:</b>
     * <ul>
     *   <li>Item must be selected (not null)</li>
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
        // Create item selector filtered to teaching items for current organization
        itemSelector = new EntityButtonSelector<Item>( // language=JSON5
            "{class: 'Item', alias: 's', where: 'family.code=`teach`', orderBy :'name'}",
            this, FXMainFrameDialogArea.getDialogArea(), getTimeline().getStore().getDataSourceModel()
        )
            .always(FXOrganization.organizationProperty(), o -> DqlStatement.where("organization=?", o));

        // Initialize item selector from model and set up bidirectional sync
        syncItemUiFromModel();
        FXProperties.runOnPropertyChange(this::syncItemModelFromUi, itemSelector.selectedItemProperty());

        // Add validation: item must not be null
        Button itemButton = itemSelector.getButton();
        getValidationSupport().addValidationRule(itemSelector.selectedItemProperty().map(Objects::nonNull),
            itemButton,
            I18n.i18nTextProperty(ProgramI18nKeys.ItemSelectedShouldntBeNull));

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

        // Create gray delete button
        MonoPane trashButton = SvgIcons.createButtonPane(SvgIcons.createTrashSVGPath(), dayTemplateTimelineModel::removeTemplateTimeLine);
        ShapeTheme.createSecondaryShapeFacet(trashButton).style(); // Make it gray

        // Create custom HPane with precise column widths for alignment across multiple rows
        return new HPane(itemButton, fromTextField, toLabel, untilTextField, nameTextField, audioToggleButton, videoToggleButton, trashButton) {
            // Fixed widths for time fields and icons ensure alignment
            private static final double FROM_WIDTH = 60, TO_WIDTH = 20, UNTIL_WIDTH = 60, AUDIO_WIDTH = 21, VIDEO_WIDTH = 24, TRASH_WIDTH = 20;
            private static final double HGAP = 5, TOTAL_HGAP = HGAP * 9;

            /**
             * Custom layout implementation with fixed and flexible columns.
             * Item selector and name field share remaining space (40%/60%),
             * while time fields and icons have fixed widths for consistent alignment.
             */
            @Override
            protected void layoutChildren(double width, double height) {
                // Calculate remaining width after fixed-width columns
                double remainingWidth = Math.max(0, width - (FROM_WIDTH + TO_WIDTH + UNTIL_WIDTH + AUDIO_WIDTH + VIDEO_WIDTH + TRASH_WIDTH + TOTAL_HGAP));
                // Distribute remaining width: 40% for item, 60% for name
                double itemWidth = Math.max(100, remainingWidth * 0.4);
                double nameWidth = remainingWidth - itemWidth;

                // Layout children left-to-right with precise positioning
                double x = 0;
                layoutInArea(itemButton        ,  x +=              0 * HGAP, 0, itemWidth,   height, Pos.CENTER_LEFT);
                layoutInArea(fromTextField     ,  x += itemWidth  + 2 * HGAP, 0, FROM_WIDTH,  height, Pos.CENTER);
                layoutInArea(toLabel           ,  x += FROM_WIDTH + 1 * HGAP, 0, TO_WIDTH,    height, Pos.CENTER);
                layoutInArea(untilTextField    ,  x += TO_WIDTH   + 1 * HGAP, 0, UNTIL_WIDTH, height, Pos.CENTER);
                layoutInArea(nameTextField     ,  x += UNTIL_WIDTH + 2 * HGAP, 0, nameWidth,  height, Pos.CENTER_LEFT);
                layoutInArea(audioToggleButton ,  x += nameWidth  + 1 * HGAP, 0, AUDIO_WIDTH, height, Pos.CENTER);
                layoutInArea(videoToggleButton ,  x += AUDIO_WIDTH + 1 * HGAP, 0, VIDEO_WIDTH, height, Pos.CENTER);
                layoutInArea(trashButton       , x + (VIDEO_WIDTH + 1 * HGAP), 0, TRASH_WIDTH, height, Pos.CENTER);
            }
        };
    }
}
