package one.modality.event.backoffice.activities.program;


import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import one.modality.base.backoffice.mainframe.fx.FXEventSelector;
import one.modality.base.shared.knownitems.KnownItemFamily;

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
     * The main view component for this activity.
     * Initialized in {@link #startLogic()}.
     */
    private ProgramView programView;

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
     * Creates the {@link ProgramModel} configured for TEACHING items and
     * the {@link ProgramView} that provides the user interface. The view's
     * logic is started to begin loading data and setting up reactive bindings.
     */
    @Override
    protected void startLogic() {
        // Create model configured for teaching items
        programView = new ProgramView(new ProgramModel(KnownItemFamily.TEACHING, getDataSourceModel()));
        // Start the view's logic (data loading, reactive bindings)
        programView.startLogic();
    }

    /**
     * Builds the user interface for this activity.
     * Creates a scrollable layout with:
     * <ul>
     *   <li>Title: "Program & Timeline" with calendar icon (localized)</li>
     *   <li>Main content: The program view with day templates and timelines</li>
     *   <li>Vertical scrollbar when content exceeds viewport height</li>
     * </ul>
     *
     * @return The root JavaFX node for this activity's UI
     */
    @Override
    public Node buildUi() {
        // Create localized title label with icon
        Label title = I18nControls.newLabel(ProgramI18nKeys.ProgramTitle);
        title.setContentDisplay(ContentDisplay.TOP);  // Icon above text
        title.setPadding(new Insets(30));
        title.setGraphicTextGap(30);  // Space between icon and text
        title.getStyleClass().add(Bootstrap.H2);  // H2 heading style
        TextTheme.createPrimaryTextFacet(title).style();  // Primary theme color

        // Create main layout with program view as center content
        BorderPane mainFrame = new BorderPane(programView.getView());

        // Position title at top center
        BorderPane.setAlignment(title, Pos.CENTER);
        mainFrame.setTop(title);
        mainFrame.setPadding(new Insets(0, 20, 30, 20));  // Horizontal and bottom padding

        // Wrap in vertical scroll pane to handle overflow content
        return Controls.createVerticalScrollPane(mainFrame);
    }
}

