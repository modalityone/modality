package one.modality.event.backoffice.activities.program;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.pickers.DatePicker;
import dev.webfx.extras.time.pickers.DatePickerOptions;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.*;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * View component for managing a single day template.
 * This view provides the complete user interface for creating and editing a day template,
 * which consists of:
 * <ul>
 *   <li><b>Template Name:</b> A descriptive name (e.g., "Morning Session", "Evening Teachings")</li>
 *   <li><b>Timeline Entries:</b> Multiple teaching sessions with items, times, and audio/video flags</li>
 *   <li><b>Selected Dates:</b> The specific dates this template applies to</li>
 * </ul>
 *
 * <p><b>Visual Layout:</b>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────┐
 * │ [Template Name TextField]                              [📋] │
 * │ ─────────────────────────────────────────────────────────── │
 * │                                                               │
 * │ Timeline 1: [9:00] to [12:00] [Name] [🔊] [📹] [🗑️]         │
 * │ Timeline 2: [14:00] to [17:00] [Name] [🔊] [📹] [🗑️]        │
 * │ [+ Add Timeline]                                              │
 * │                                                               │
 * │ [Delete this template]                                        │
 * │ ─────────────────────────────────────────────────────────── │
 * │                                                               │
 * │ Assign dates:                                                 │
 * │ [  Calendar  ]  |  [Jan 15]                                  │
 * │ [  Date Picker]  |  [Jan 22]                                  │
 * │                  |  [Jan 29]                                  │
 * └─────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p><b>Key Features:</b>
 * <ul>
 *   <li><b>Timeline Management:</b> Add, edit, delete timeline entries</li>
 *   <li><b>Date Selection:</b> Visual calendar with multiple date selection</li>
 *   <li><b>Template Duplication:</b> Clone entire template with all timelines</li>
 *   <li><b>Template Deletion:</b> Remove template and all its data</li>
 *   <li><b>Real-time Updates:</b> All changes immediately reflected in model</li>
 * </ul>
 *
 * <p><b>Day Ticket Restrictions:</b>
 * When day tickets are enabled for the event, the date picker only allows selection
 * of dates that have pre-created bookable scheduled items. This ensures program
 * consistency with the day ticket configuration.
 *
 * <p><b>Bidirectional Sync:</b>
 * <ul>
 *   <li>Template name changes are immediately saved to the model</li>
 *   <li>Date additions/removals update the DayTemplate's date string</li>
 *   <li>Timeline views are automatically created/removed as timelines change</li>
 * </ul>
 *
 * @author David Hello
 * @author Bruno Salmon
 *
 * @see DayTemplateModel
 * @see DayTemplateTimelineView
 * @see DayTemplateDateView
 */
final class DayTemplateView {

    /**
     * The model managing the day template data and business logic.
     */
    private final DayTemplateModel dayTemplateModel;

    /**
     * Observable list of timeline view components.
     * Automatically synchronized with the model's timeline list.
     */
    private final ObservableList<DayTemplateTimelineView> workingDayTemplateTimelineViews = FXCollections.observableArrayList();

    /**
     * Date picker component for selecting multiple dates.
     * Configured to:
     * <ul>
     *   <li>Allow multiple date selection</li>
     *   <li>Prevent past date selection (future dates only)</li>
     *   <li>Sort selected dates chronologically</li>
     *   <li>Disable default border styling (custom styling applied)</li>
     * </ul>
     */
    private final DatePicker datePicker = new DatePicker(new DatePickerOptions()
        .setMultipleSelectionAllowed(true)
        // Note: Set to false as program dates should typically be future dates.
        // If past dates need to be selected (for editing historical programs), this can be changed to true.
        // Previously there was a rendering issue where selected past dates weren't visually displayed
        // when this was false - verify this has been resolved in the DatePicker component.
        .setPastDatesSelectionAllowed(false)
        .setApplyBorderStyle(false)
        .setApplyMaxSize(false)
        .setSortSelectedDates(true)
    );

    /**
     * Observable list of date view components (one per selected date).
     * Automatically synchronized with the date picker's selected dates.
     */
    private final ObservableList<DayTemplateDateView> workingDayTemplateDateViews = FXCollections.observableArrayList();
    {
        // Automatic conversion: LocalDate → DayTemplateDateView with delete button
        ObservableLists.bindConvertedOptimized(workingDayTemplateDateViews, datePicker.getSelectedDates(), date -> new DayTemplateDateView(date, datePicker));
    }

    /**
     * The root container for this day template's UI.
     * Contains template name, timelines, and date selection.
     */
    private final BorderPane mainContainer;

    /**
     * Text field for editing the template name.
     * Changes are immediately saved to the model.
     */
    private final TextField templateNameTextField = new TextField();

    /**
     * Constructs a new DayTemplateView for managing a day template.
     * Initializes all UI components, sets up bidirectional data bindings,
     * and configures the date picker for the event's date range.
     *
     * <p><b>Initialization Steps:</b>
     * <ol>
     *   <li>Connect selected dates to model</li>
     *   <li>Register UI sync callbacks</li>
     *   <li>Bind timeline views to timeline models</li>
     *   <li>Build the user interface</li>
     *   <li>Set up form validation</li>
     *   <li>Configure date picker for event start month</li>
     *   <li>Apply day ticket restrictions if enabled</li>
     * </ol>
     *
     * @param dayTemplateModel The model managing this template's data
     */
    DayTemplateView(DayTemplateModel dayTemplateModel) {
        this.dayTemplateModel = dayTemplateModel;
        // Connect date picker's selected dates to model
        dayTemplateModel.setSelectedDates(datePicker.getSelectedDates());
        // Register callbacks for model-to-UI synchronization
        dayTemplateModel.setSyncUiFromModelRunnable(this::syncUiFromModel);
        dayTemplateModel.setInitFormValidationRunnable(this::initFormValidation);
        // Bind timeline views to timeline models
        ObservableLists.bindConverted(workingDayTemplateTimelineViews, dayTemplateModel.getWorkingDayTemplateTimelines(), DayTemplateTimelineView::new);
        // Build the UI
        mainContainer = buildUi();
        // Set up validation rules
        initFormValidation();
        // Initialize date picker to show event's start month
        LocalDate eventStartDate = getDayTemplate().getEvent().getStartDate();
        datePicker.setDisplayedYearMonth(YearMonth.of(eventStartDate.getYear(), eventStartDate.getMonth()));

        // Apply day ticket restrictions if enabled
        if (dayTemplateModel.getProgramModel().getLoadedEvent().isTeachingsDayTicket())
            // Only allow selection of dates that have bookable teaching scheduled items
            datePicker.setIsDateSelectableFunction(date ->
                dayTemplateModel.getProgramModel().getTeachingsBookableScheduledItems().stream()
                    .anyMatch(item -> item.getDate().equals(date))
            );
    }

    /**
     * Returns the root UI panel for this day template view.
     *
     * @return BorderPane containing the complete template UI
     */
    BorderPane getPanel() {
        return mainContainer;
    }

    /**
     * Returns the DayTemplate entity managed by this view.
     *
     * @return The DayTemplate entity
     */
    public DayTemplate getDayTemplate() {
        return dayTemplateModel.getDayTemplate();
    }

    /**
     * Synchronizes all UI components from the model state.
     * Called when the model is reset or reloaded from database.
     */
    private void syncUiFromModel() {
        syncTemplateNameUiFromModel();
        syncSelectedDatesUiFromModel();
    }

    /**
     * Updates the template name text field from the model.
     */
    private void syncTemplateNameUiFromModel() {
        templateNameTextField.setText(getDayTemplate().getName());
    }

    /**
     * Updates the model's template name from the text field.
     * Called automatically when the user types in the field.
     */
    private void syncTemplateNameModelFromUi() {
        getDayTemplate().setName(templateNameTextField.getText());
    }

    /**
     * Updates the date picker's selected dates from the model.
     * Parses the comma-separated date string stored in the DayTemplate entity.
     */
    private void syncSelectedDatesUiFromModel() {
        if (getDayTemplate().getDates() != null) {
            datePicker.setSelectedDates(DatesToStringConversion.getDateList(getDayTemplate().getDates()));
        }
    }


    /**
     * Builds the complete user interface for this day template.
     * Creates a BorderPane with three main sections:
     * <ul>
     *   <li><b>Top:</b> Template name field and duplicate button</li>
     *   <li><b>Center:</b> Timeline entries list with add/delete controls</li>
     *   <li><b>Bottom:</b> Date picker calendar and selected dates list</li>
     * </ul>
     *
     * All UI components are reactively bound to the model for automatic updates.
     *
     * @return BorderPane containing the complete day template UI
     */
    private BorderPane buildUi() {
        //========== TIMELINES SECTION ==========//
        // Container for all timeline entries (teaching sessions)
        VBox timelinesContainer = new VBox(5);
        timelinesContainer.setFillWidth(true);
        // Automatically populate with timeline views as timelines are added/removed
        ObservableLists.bindConverted(timelinesContainer.getChildren(), workingDayTemplateTimelineViews, DayTemplateTimelineView::getView);

        //========== SELECTED DATES LIST ==========//
        // Container for list of selected dates with delete buttons
        VBox listOfSelectedDatesVBox = new VBox(10);
        listOfSelectedDatesVBox.setAlignment(Pos.CENTER);
        // Automatically populate with date views as dates are selected/deselected
        ObservableLists.bindConverted(listOfSelectedDatesVBox.getChildren(), workingDayTemplateDateViews, DayTemplateDateView::getView);

        //========== TOP SECTION: Template Name & Duplicate ==========//
        // Duplicate button (SVG icon - two overlapping rectangles)
        SVGPath duplicateIcon = new SVGPath();
        duplicateIcon.setContent("M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z");
        duplicateIcon.setFill(Color.web("#6b7280")); // Gray color
        MonoPane duplicateButton = SvgIcons.createButtonPane(duplicateIcon, dayTemplateModel::duplicate);

        // Separator between header and content
        Separator topSeparator = new Separator();
        topSeparator.setPadding(new Insets(10, 0, 10, 0));

        // Template name text field with localized prompt
        I18nControls.bindI18nPromptProperty(templateNameTextField, ProgramI18nKeys.TemplateNamePrompt);
        HBox.setHgrow(templateNameTextField, Priority.ALWAYS); // Expand to fill space
        syncTemplateNameUiFromModel(); // Initialize from model
        FXProperties.runOnPropertyChange(this::syncTemplateNameModelFromUi, templateNameTextField.textProperty()); // Sync changes to model

        // Top line: name field and duplicate button
        HBox topLine = new HBox(20, templateNameTextField, duplicateButton);

        // Unused vertical line (kept for potential future use)
        Line verticalLine = new Line();
        verticalLine.setStartY(0);
        verticalLine.setEndY(180);
        verticalLine.setStroke(Color.LIGHTGRAY);

        //========== CENTER SECTION: Timelines & Controls ==========//
        BorderPane centerBorderPane = new BorderPane();
        centerBorderPane.setTop(timelinesContainer);

        // Add timeline button (blue plus icon)
        SVGPath addIcon = SvgIcons.setSVGPathFill(SvgIcons.createPlusPath(), Color.web("#0096D6"));
        MonoPane addButton = SvgIcons.createButtonPane(addIcon, dayTemplateModel::addTemplateTimeline);
        addButton.setPadding(new Insets(10, 0, 0, 0));

        centerBorderPane.setCenter(addButton);
        BorderPane.setAlignment(addButton, Pos.TOP_LEFT);

        // Delete template button (red text with icon)
        Label deleteButton = Bootstrap.small(Bootstrap.textDanger(I18nControls.newLabel(ProgramI18nKeys.DeleteDayTemplate)));
        SvgIcons.armButton(deleteButton, dayTemplateModel::deleteDayTemplate);
        deleteButton.setPadding(new Insets(30, 0, 0, 0));

        // Separator before bottom section
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        VBox bottomVBox = new VBox(deleteButton, separator);
        centerBorderPane.setBottom(bottomVBox);
        BorderPane.setAlignment(bottomVBox, Pos.BOTTOM_LEFT);
        centerBorderPane.setMaxHeight(Region.USE_PREF_SIZE);

        //========== BOTTOM SECTION: Date Picker & Selected Dates ==========//
        BorderPane bottomBorderPane = new BorderPane();
        BorderPane.setAlignment(bottomBorderPane, Pos.CENTER);
        bottomBorderPane.setMaxWidth(600);

        // "Assign dates" label
        Label assignDateLabel = I18nControls.newLabel(ProgramI18nKeys.AssignDay);
        TextTheme.createPrimaryTextFacet(assignDateLabel).style();
        assignDateLabel.getStyleClass().add(Bootstrap.SMALL);
        assignDateLabel.setPadding(new Insets(5, 0, 10, 0));

        // Scrollable list of selected dates
        ScrollPane listOfSelectedDatesVBoxScrollPane = new ScrollPane(listOfSelectedDatesVBox);
        listOfSelectedDatesVBoxScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        listOfSelectedDatesVBoxScrollPane.setMaxHeight(150);
        listOfSelectedDatesVBoxScrollPane.setMinWidth(120);

        // Layout: [Label]
        //         [Calendar] | [Selected Dates List]
        bottomBorderPane.setTop(assignDateLabel);
        BorderPane.setAlignment(assignDateLabel, Pos.CENTER);
        bottomBorderPane.setCenter(datePicker.getView());
        BorderPane.setAlignment(datePicker.getView(), Pos.CENTER);

        // Vertical separator between calendar and selected dates list
        Separator verticalSeparator = new Separator(Orientation.VERTICAL);
        verticalSeparator.setPadding(new Insets(0, 0, 0, 40));
        HBox listOfDatesHBox = new HBox(verticalSeparator, listOfSelectedDatesVBoxScrollPane);
        listOfDatesHBox.setSpacing(40);
        listOfDatesHBox.setAlignment(Pos.CENTER);
        bottomBorderPane.setRight(listOfDatesHBox);
        BorderPane.setAlignment(listOfDatesHBox, Pos.CENTER);

        //========== DATE PICKER CHANGE LISTENER ==========//
        // Listen for date selection/deselection and update the model's date string
        datePicker.getSelectedDates().addListener((ListChangeListener<LocalDate>) change -> {
            DayTemplate dayTemplate = getDayTemplate();
            while (change.next()) {
                if (change.wasAdded()) {
                    // User selected new date(s) - add to comma-separated string
                    for (LocalDate date : change.getAddedSubList()) {
                        dayTemplate.setDates(DatesToStringConversion.addDate(dayTemplate.getDates(), date));
                    }
                }
                if (change.wasRemoved()) {
                    // User deselected date(s) - remove from comma-separated string
                    for (LocalDate date : change.getRemoved()) {
                        dayTemplate.setDates(DatesToStringConversion.removeDate(dayTemplate.getDates(), date));
                    }
                }
            }
        });

        //========== MAIN CONTAINER ==========//
        BorderPane mainContainer = new BorderPane();
        // Rounded border around entire template
        mainContainer.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY,
            BorderStrokeStyle.SOLID, new CornerRadii(10), BorderWidths.DEFAULT)));
        mainContainer.setPadding(new Insets(10, 10, 10, 10));
        mainContainer.getStyleClass().add("day-template-panel");
        // Set all width constraints to match ColumnsPane column width (550px)
        mainContainer.setMinWidth(550);
        mainContainer.setPrefWidth(550);
        mainContainer.setMaxWidth(550);
        mainContainer.setTop(new VBox(topLine, topSeparator));
        mainContainer.setCenter(centerBorderPane);
        mainContainer.setMaxHeight(Region.USE_PREF_SIZE);
        mainContainer.setBottom(bottomBorderPane);

        return mainContainer;
    }

    /**
     * Initializes form validation rules for this day template.
     * Currently validates:
     * <ul>
     *   <li>Template name is required (must not be empty)</li>
     * </ul>
     *
     * Note: Timeline validation is handled separately in {@link DayTemplateTimelineView}.
     */
    void initFormValidation() {
        dayTemplateModel.getValidationSupport().addRequiredInput(templateNameTextField);
    }

}
