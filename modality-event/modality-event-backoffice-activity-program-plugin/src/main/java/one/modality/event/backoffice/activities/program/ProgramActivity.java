package one.modality.event.backoffice.activities.program;


import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import one.modality.base.backoffice.mainframe.fx.FXEventSelector;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.event.client.event.fx.FXEvent;

/**
 * Activity for managing event programs and teaching schedules in the back office.
 * This activity provides the main entry point for the Program module, which allows
 * administrators to create and manage day templates and teaching timelines for events.
 *
 * <p><b>Key Features:</b>
 * <ul>
 *   <li>Create day templates with multiple teaching sessions</li>
 *   <li>Assign specific dates to each day template</li>
 *   <li>Define teaching items, start/end times, and audio/video availability</li>
 *   <li>Generate the complete program with scheduled items for all dates</li>
 *   <li>Support for day ticket configurations (teachings and audio recordings)</li>
 * </ul>
 *
 * <p><b>Navigation:</b>
 * The activity is accessible via the "/program" route and is typically linked from
 * the back office home page navigation menu.
 *
 * <p><b>Event Selection:</b>
 * When this activity is active, the event selector is shown in the header, allowing
 * users to switch between different events. The program data automatically reloads
 * when a different event is selected.
 *
 * <p><b>Architecture:</b>
 * <ul>
 *   <li>ProgramActivity (this class) - Activity lifecycle management</li>
 *   <li>{@link ProgramModel} - Business logic and data management</li>
 *   <li>{@link ProgramView} - User interface and interactions</li>
 *   <li>{@link DayTemplateModel} - Day template management</li>
 *   <li>{@link DayTemplateView} - Day template UI</li>
 * </ul>
 *
 * @author Bruno Salmon
 *
 * @see ProgramView
 * @see ProgramModel
 * @see ProgramRouting
 */
final class ProgramActivity extends ViewDomainActivityBase {

    /**
     * The business logic model managing program data and operations.
     * Initialized in {@link #startLogic()}.
     */
    private ProgramModel programModel;

    /**
     * The main view component for managing templates (used when program is not generated).
     * Initialized in {@link #startLogic()}.
     */
    private ProgramView programView;

    /**
     * The view component for displaying the generated program (Step 3 - used when program is generated).
     * Initialized in {@link #startLogic()}.
     */
    private ProgramStep3View step3View;

    /**
     * Property tracking whether initial data is still loading.
     * True during initialization, false once data has been loaded.
     */
    private final BooleanProperty loadingProperty = new SimpleBooleanProperty(true);

    /**
     * Property tracking whether the event configuration is valid.
     * True when both teaching and audio day tickets are enabled, false otherwise.
     */
    private final BooleanProperty configurationValidProperty = new SimpleBooleanProperty(false);

    /**
     * Called when the activity resumes (becomes visible to the user).
     * Shows the event selector in the header, allowing users to switch between events.
     * The program data will automatically reload when a different event is selected.
     */
    @Override
    public void onResume() {
        super.onResume();
        FXEventSelector.showEventSelector();
    }

    /**
     * Called when the activity pauses (becomes hidden from the user).
     * Resets the event selector to its default state (hiding it).
     */
    @Override
    public void onPause() {
        FXEventSelector.resetToDefault();
        super.onPause();
    }

    /**
     * Initializes the business logic and data model for this activity.
     * Creates the {@link ProgramModel} configured for TEACHING items,
     * the {@link ProgramView} for managing templates, and the {@link ProgramStep3View}
     * for displaying the generated program. The view's logic is started to begin loading
     * data and setting up reactive bindings.
     *
     * The loading indicator is shown initially and hidden once the program state is determined.
     */
    @Override
    protected void startLogic() {
        Console.log("ProgramActivity: startLogic() called");

        // Create model configured for teaching items
        programModel = new ProgramModel(KnownItemFamily.TEACHING, getDataSourceModel());

        // Create template management view (used when program is not generated)
        programView = new ProgramView(programModel);
        programView.startLogic();

        // Create Step 3 view (generated program - used when program is generated)
        step3View = new ProgramStep3View(programModel);

        // Load initial event data if available
        if (FXEvent.getEvent() != null) {
            Console.log("ProgramActivity: Loading initial event: " + FXEvent.getEvent().getName());
            step3View.setEvent(FXEvent.getEvent());
            // Explicitly trigger initial load and hide loading indicator when complete
            programModel.reloadProgramFromSelectedEvent(FXEvent.getEvent())
                .inUiThread()
                .onComplete(result -> {
                    Console.log("ProgramActivity: Initial data load complete, hiding loading indicator");
                    loadingProperty.set(false);
                });
        } else {
            Console.log("ProgramActivity: No initial event, hiding loading indicator");
            loadingProperty.set(false);
        }

        // Also hide loading indicator when program generated state changes (for subsequent reloads)
        FXProperties.runOnPropertyChange(programGenerated -> {
            Console.log("ProgramActivity: programGenerated changed to " + programGenerated);
            loadingProperty.set(false);
        }, programModel.programGeneratedProperty());

        // Track event configuration validity
        FXProperties.runNowAndOnPropertyChange(event -> {
            if (event != null) {
                boolean teachingEnabled = event.isTeachingsDayTicket();
                boolean audioEnabled = event.isAudioRecordingsDayTicket();
                boolean isValid = teachingEnabled && audioEnabled;
                Console.log("ProgramActivity: Configuration valid = " + isValid + " (teaching=" + teachingEnabled + ", audio=" + audioEnabled + ")");
                configurationValidProperty.set(isValid);
            } else {
                configurationValidProperty.set(false);
            }
        }, FXEvent.eventProperty());
    }

    /**
     * Builds a simple configuration warning view to display when event configuration is invalid.
     * Shown when either teaching day ticket or audio recording day ticket is disabled.
     * Shows the program title, an error icon, and a danger message directing users to contact their administrator.
     * This provides a clean, minimal UI when the event is not properly configured.
     *
     * @return The configuration warning view node
     */
    private Node buildConfigurationWarningView() {
        // Main container
        VBox container = new VBox(48);
        container.setPadding(new Insets(32));
        container.setAlignment(Pos.TOP_CENTER);
        container.setMaxWidth(1186); // MAX_WIDTH (1122) + padding (64)

        // Header with title
        Label title = I18nControls.newLabel(ProgramI18nKeys.ProgramTitle);
        title.setContentDisplay(ContentDisplay.TOP);
        title.setPadding(new Insets(30));
        title.setGraphicTextGap(30);
        title.getStyleClass().add(Bootstrap.H2);
        TextTheme.createPrimaryTextFacet(title).style();

        VBox headerBox = new VBox(title);
        headerBox.setAlignment(Pos.CENTER);

        // Error icon (circle with exclamation mark)
        SVGPath errorIcon = new SVGPath();
        errorIcon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z");
        errorIcon.setFill(javafx.scene.paint.Color.web("#dc2626"));
        errorIcon.setScaleX(1.5);
        errorIcon.setScaleY(1.5);

        StackPane iconContainer = new StackPane(errorIcon);
        iconContainer.setMinSize(24, 24);
        iconContainer.setAlignment(Pos.CENTER);

        // Alert title
        Label alertTitle = Bootstrap.strong(I18nControls.newLabel(ProgramI18nKeys.ConfigurationRequired));
        alertTitle.getStyleClass().add("program-alert-title");

        // Alert message
        Label alertMessage = I18nControls.newLabel(ProgramI18nKeys.ConfigurationRequiredMessage);
        alertMessage.setWrapText(true);
        alertMessage.getStyleClass().add("program-alert-message");

        // Alert content
        VBox alertContent = new VBox(4, alertTitle, alertMessage);

        // Alert box
        HBox alertBox = new HBox(12, iconContainer, alertContent);
        alertBox.setAlignment(Pos.TOP_LEFT);
        alertBox.getStyleClass().add("program-alert-box");
        alertBox.setPadding(new Insets(16, 20, 16, 20));
        alertBox.setMaxWidth(1122);

        HBox alertLine = new HBox(alertBox);
        alertLine.setAlignment(Pos.CENTER);

        container.getChildren().addAll(headerBox, alertLine);
        return container;
    }

    /**
     * Builds the user interface for this activity.
     * Creates a layout that automatically switches between four states:
     * <ul>
     *   <li>Loading View: Shows a spinner while initial data is being loaded</li>
     *   <li>Configuration Warning View: When event configuration is invalid (teaching or audio day ticket disabled) - shows simple danger message</li>
     *   <li>Template View (ProgramView): When configuration is valid - allows creating/editing templates and generating preliminaries</li>
     *   <li>Generated Program View (ProgramStep3View): When program is generated - displays actual schedule</li>
     * </ul>
     *
     * The view switching is reactive and based on the loadingProperty, configurationValidProperty, dayTicketPreliminaryScheduledItemProperty, and programGeneratedProperty.
     *
     * @return The root JavaFX node for this activity's UI
     */
    @Override
    public Node buildUi() {
        // Create a StackPane to hold all views (only one visible at a time)
        StackPane viewContainer = new StackPane();

        // Create loading indicator
        StackPane loadingPane = new StackPane();
        loadingPane.setAlignment(Pos.CENTER);
        loadingPane.setPadding(new Insets(50));
        ProgressIndicator loadingSpinner = Controls.createProgressIndicator(50);
        loadingPane.getChildren().add(loadingSpinner);

        // Create configuration warning view
        Node configWarningNode = buildConfigurationWarningView();

        // Add all views to the container
        Node templateViewNode = programView.getView();
        Node generatedViewNode = step3View.getView();

        viewContainer.getChildren().addAll(loadingPane, configWarningNode, templateViewNode, generatedViewNode);

        // Reactive binding: Update event in Step 3 view when event changes
        FXProperties.runNowAndOnPropertyChange(event -> {
            if (event != null) {
                step3View.setEvent(event);
            }
        }, FXEvent.eventProperty());

        // Reactive binding: Toggle visibility based on loading state, configuration validity, preliminary generation, and programGeneratedProperty
        // Priority: Loading view > Config warning view (if config invalid) > Generated view > Template view
        FXProperties.runNowAndOnPropertiesChange(() -> {
            boolean isLoading = loadingProperty.get();
            boolean configValid = configurationValidProperty.get();
            boolean preliminariesGenerated = Boolean.TRUE.equals(programModel.getDayTicketPreliminaryScheduledItemProperty().getValue());
            boolean isGenerated = Boolean.TRUE.equals(programModel.programGeneratedProperty().getValue());

            Console.log("ProgramActivity: Loading state: " + isLoading);
            Console.log("ProgramActivity: Configuration valid: " + configValid);
            Console.log("ProgramActivity: Preliminaries generated: " + preliminariesGenerated);
            Console.log("ProgramActivity: Program generated state: " + isGenerated);

            if (isLoading) {
                // Show loading indicator, hide all other views
                loadingPane.setVisible(true);
                loadingPane.setManaged(true);
                configWarningNode.setVisible(false);
                configWarningNode.setManaged(false);
                templateViewNode.setVisible(false);
                templateViewNode.setManaged(false);
                generatedViewNode.setVisible(false);
                generatedViewNode.setManaged(false);
                Console.log("ProgramActivity: Showing loading indicator");
            } else if (!configValid) {
                // Show configuration warning if config is invalid (regardless of preliminaries), hide all other views
                loadingPane.setVisible(false);
                loadingPane.setManaged(false);
                configWarningNode.setVisible(true);
                configWarningNode.setManaged(true);
                templateViewNode.setVisible(false);
                templateViewNode.setManaged(false);
                generatedViewNode.setVisible(false);
                generatedViewNode.setManaged(false);
                Console.log("ProgramActivity: Showing configuration warning (config invalid)");
            } else if (preliminariesGenerated) {
                // Config is valid and preliminaries are generated - show appropriate view based on program state
                loadingPane.setVisible(false);
                loadingPane.setManaged(false);
                configWarningNode.setVisible(false);
                configWarningNode.setManaged(false);

                // Show/hide template view (visible when NOT generated)
                templateViewNode.setVisible(!isGenerated);
                templateViewNode.setManaged(!isGenerated);

                // Show/hide generated program view (visible when generated)
                generatedViewNode.setVisible(isGenerated);
                generatedViewNode.setManaged(isGenerated);

                Console.log("ProgramActivity: Template view visible: " + !isGenerated);
                Console.log("ProgramActivity: Generated view visible: " + isGenerated);
            } else {
                // Config is valid but preliminaries NOT generated - show template view
                loadingPane.setVisible(false);
                loadingPane.setManaged(false);
                configWarningNode.setVisible(false);
                configWarningNode.setManaged(false);
                templateViewNode.setVisible(true);
                templateViewNode.setManaged(true);
                generatedViewNode.setVisible(false);
                generatedViewNode.setManaged(false);
                Console.log("ProgramActivity: Showing template view (preliminaries not generated but config valid)");
            }
        }, loadingProperty, configurationValidProperty, programModel.getDayTicketPreliminaryScheduledItemProperty(), programModel.programGeneratedProperty());

        // Wrap in vertical scroll pane to handle overflow content
        return Controls.createVerticalScrollPane(viewContainer);
    }
}

