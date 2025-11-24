package one.modality.hotel.backoffice.activities.household.gantt.view;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.activities.household.gantt.adapter.EntityDataAdapter;
import one.modality.hotel.backoffice.activities.household.gantt.data.HouseholdGanttDataLoader;
import one.modality.hotel.backoffice.activities.household.gantt.model.GanttRoomData;
import one.modality.hotel.backoffice.activities.household.gantt.presenter.GanttPresenter;

import java.util.List;

/**
 * Gantt view facade for household/housekeeping calendar.
 *
 * REFACTORED ARCHITECTURE:
 * This class now serves as a lightweight facade/controller that:
 * - Initializes the presenter and view
 * - Loads data from the database via HouseholdGanttDataLoader
 * - Coordinates between data and view
 *
 * The complex logic has been extracted into:
 * - gantt/model/ - Data interfaces and value objects
 * - gantt/presenter/ - Business logic (aggregation, conflict detection, filtering, expand/collapse)
 * - gantt/view/ - UI components and layout
 * - gantt/renderer/ - Rendering strategies for booking bars
 * - EntityDataAdapter - Converts database entities to gantt model interfaces
 * - HouseholdGanttDataLoader - Loads data from database using reactive queries
 *
 * Benefits of this architecture:
 * - Single Responsibility: Each class has one clear purpose
 * - Open/Closed: Easy to add new data sources via adapters
 * - Testable: Business logic can be unit tested without UI
 * - Maintainable: Changes are localized to specific layers
 * - Extensible: New rendering strategies or cell types can be added easily
 *
 * @author Claude Code Assistant (Refactored)
 */
public final class HouseholdGanttView {

    private final GanttPresenter presenter;
    private final GanttTableView view;
    private final HouseholdGanttDataLoader dataLoader;

    // Listener references for cleanup to prevent memory leaks
    private final ListChangeListener<Object> resourceConfigListener;
    private final ListChangeListener<Object> attendanceListener;

    /**
     * Constructor initializes the Gantt view with database loading.
     *
     * INITIALIZATION FLOW:
     * 1. Creates the presenter (business logic layer)
     * 2. Creates the view (UI layer) with reference to presenter
     * 3. Creates the data loader with organization context
     * 4. Sets up navigation callback for date range changes
     * 5. Attaches listeners to react to data updates
     *
     * SEPARATION OF CONCERNS:
     * - Presenter: Handles aggregation, filtering, expand/collapse state
     * - View: Renders the table, columns, and booking bars
     * - DataLoader: Queries database reactively and manages ObservableLists
     * - This class: Coordinates between the three layers
     *
     * @param pm The presentation model (provides organization context for queries)
     */
    public HouseholdGanttView(AccommodationPresentationModel pm) {

        // Initialize presenter (handles business logic)
        // The presenter will:
        // - Group rooms by category/floor
        // - Detect booking conflicts (overlapping attendances)
        // - Manage expand/collapse state for room groups
        // - Filter rooms based on search criteria
        this.presenter = new GanttPresenter();

        // Initialize view (handles UI rendering)
        // The view will:
        // - Create the TableView with row/column headers
        // - Render booking bars using strategies (arrival, departure, occupied, vacant)
        // - Handle user interactions (expand/collapse, navigation buttons)
        // - Manage the visual layout and styling
        this.view = new GanttTableView(presenter);

        // Initialize data loader
        // The data loader will:
        // - Execute reactive DSQL queries when the organization changes
        // - Load ResourceConfiguration entities (room definitions)
        // - Load Attendance entities (booking occupancy records)
        // - Maintain ObservableLists that update when database changes
        this.dataLoader = new HouseholdGanttDataLoader(pm, presenter);

        // Set up navigation callback to reload data when date range changes
        // CASE 1: User clicks "Previous Week" button -> refreshData() called -> dataLoader.reload()
        // CASE 2: User clicks "Next Week" button -> refreshData() called -> dataLoader.reload()
        // CASE 3: User clicks "Today" button -> refreshData() called -> dataLoader.reload()
        // This ensures data is fetched for the new visible time window
        view.setOnNavigationCallback(this::refreshData);

        // Initialize listeners with stored references for cleanup
        // WHY STORE LISTENER REFERENCES?
        // To enable proper cleanup when the view is destroyed, preventing memory leaks.
        // Lambda listeners hold strong references to 'this', which prevents garbage collection
        // if the ObservableLists outlive this view instance.
        this.resourceConfigListener = c -> refreshDisplay();
        this.attendanceListener = c -> refreshDisplay();

        // Listen to data changes and update view
        // WHY TWO LISTENERS?
        // - ResourceConfigurations can change independently (room added/removed/renamed)
        // - Attendances can change independently (booking created/updated/cancelled)
        // - Both need to trigger a full refresh to ensure consistency
        //
        // REACTIVE PATTERN:
        // When EntityStore updates due to database changes, the ObservableLists
        // fire change events, which trigger refreshDisplay() to re-adapt and re-render
        dataLoader.getResourceConfigurations().addListener(resourceConfigListener);
        dataLoader.getAttendances().addListener(attendanceListener);
    }

    /**
     * Starts the data loading logic.
     * Must be called after construction to begin loading data.
     *
     * LIFECYCLE:
     * - Constructor: Sets up the architecture (presenter, view, data loader)
     * - startLogic(): Activates the reactive queries and begins loading
     *
     * WHY SEPARATE FROM CONSTRUCTOR?
     * The mixin object is part of WebFX's reactive framework lifecycle management.
     * It ensures that reactive chains are properly cleaned up when the view is
     * destroyed, preventing memory leaks from active database subscriptions.
     *
     * WHAT HAPPENS WHEN CALLED:
     * 1. DataLoader activates its reactive DSQL queries
     * 2. Queries execute and populate the ObservableLists
     * 3. List change listeners fire and call refreshDisplay()
     * 4. View renders with initial data
     *
     * IMPORTANT: This must be called exactly once, after construction, by the
     * parent activity that manages this view's lifecycle.
     *
     * @param mixin The mixin object for reactive chain lifecycle management
     */
    public void startLogic(Object mixin) {
        dataLoader.startLogic(mixin);
    }

    /**
     * Refreshes the data for the current time window.
     * Called after navigation (prev/next/today buttons).
     *
     * TRIGGER CASES:
     * 1. User clicks "Previous Week" - view updates time window, then calls this
     * 2. User clicks "Next Week" - view updates time window, then calls this
     * 3. User clicks "Today" - view resets to current date, then calls this
     *
     * EXECUTION FLOW:
     * 1. This method is called by the view via the navigation callback
     * 2. Delegates to dataLoader.reload()
     * 3. DataLoader re-executes DSQL queries with new time window parameters
     * 4. New data populates the ObservableLists
     * 5. List change listeners fire
     * 6. refreshDisplay() is called automatically
     * 7. View re-renders with new data
     *
     * WHY NOT CALL refreshDisplay() DIRECTLY?
     * Because the data needs to be reloaded from the database first.
     * The reactive listeners will trigger refreshDisplay() automatically
     * once the new data arrives.
     *
     * PERFORMANCE NOTE:
     * Only loads data for the visible time window (typically 7-14 days),
     * not the entire booking history, to keep queries fast.
     */
    public void refreshData() {
        // Reload data for new time window
        dataLoader.reload();
    }

    /**
     * Refreshes the display with current data.
     * Called when data changes.
     *
     * TRIGGER CASES:
     * 1. Initial load: startLogic() -> queries execute -> ObservableLists populate -> this method
     * 2. Navigation: refreshData() -> reload queries -> ObservableLists update -> this method
     * 3. Database changes: External update -> reactive query -> ObservableLists update -> this method
     *    (e.g., new booking created, room renamed, attendance modified)
     *
     * EXECUTION FLOW:
     * Step 1: LOG CURRENT STATE
     *   - Log that refresh is triggered (for debugging)
     *   - Log entity counts from data loader
     *
     * Step 2: ADAPT DATABASE ENTITIES TO GANTT MODEL
     *   - EntityDataAdapter.adaptRooms() converts:
     *     * ResourceConfiguration entities -> GanttRoomData objects
     *     * Attendance entities -> GanttBookingData objects
     *   - Groups attendances by room
     *   - Extracts relevant fields (name, category, dates, etc.)
     *   - Transforms database model to presentation model
     *
     * Step 3: LOG ADAPTED DATA
     *   - Log how many rooms were adapted
     *   - Log sample room details for verification
     *   - Helps identify if adaptation is working correctly
     *
     * Step 4: DISPLAY IN VIEW
     *   - Calls view.displayRooms() which:
     *     * Passes data to presenter for grouping/aggregation
     *     * Presenter groups rooms by category
     *     * View renders rows for each room/group
     *     * Booking bars are rendered using strategies
     *   - Wrapped in try-catch to handle any rendering errors
     *
     * WHY ADAPTER PATTERN?
     * Separating database entities from gantt model provides:
     * - Decoupling: View doesn't depend on database schema
     * - Flexibility: Can swap data sources (mock data, API, different DB)
     * - Testability: Can test view logic without database
     * - Single Responsibility: Each layer has one purpose
     *
     * ERROR HANDLING:
     * If view.displayRooms() throws an exception, it's caught and logged
     * to prevent the entire UI from crashing. This is important for
     * production stability when encountering unexpected data.
     */
    private void refreshDisplay() {
        // Step 1: Convert database entities to gantt model using adapter pattern
        // This transformation isolates the view from database schema changes
        List<GanttRoomData> rooms = EntityDataAdapter.adaptRooms(
            dataLoader.getResourceConfigurations(),
            dataLoader.getAttendances()
        );

        // Step 2: Display data in view with error handling
        // The view will delegate to the presenter for aggregation, then render
        try {
            view.displayRooms(rooms);
        } catch (Exception e) {
            // Catch any rendering errors to prevent UI crash
            // This is important for production stability
            System.err.println("[HouseholdGanttView] Error displaying rooms: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Returns the root container node for embedding in the UI.
     *
     * USAGE:
     * This is called by the parent activity (HouseholdActivity) to get the
     * JavaFX Node that should be added to the activity's scene graph.
     *
     * EXAMPLE:
     * <code>
     * HouseholdGanttView ganttView = new HouseholdGanttView(pm);
     * ganttView.startLogic(this);
     * BorderPane container = new BorderPane();
     * container.setCenter(ganttView.getNode()); // Embeds the gantt chart
     * </code>
     *
     * WHAT IT RETURNS:
     * The root node contains:
     * - Navigation toolbar (Previous/Today/Next buttons)
     * - Date range display
     * - TableView with room rows and day columns
     * - All rendered booking bars
     *
     * @return The root JavaFX Node containing the entire gantt chart UI
     */
    public Node getNode() {
        return view.getNode();
    }

    /**
     * Gets the presenter (for advanced customization if needed).
     *
     * WHEN TO USE:
     * This is primarily for testing or advanced customization scenarios.
     * Normal usage doesn't require direct access to the presenter.
     *
     * USE CASES:
     * 1. Testing: Mock the presenter to test view logic in isolation
     * 2. Filtering: Call presenter.setSearchFilter() to filter rooms
     * 3. State inspection: Check expand/collapse state for specific room groups
     * 4. Advanced features: Implement custom aggregation or conflict detection
     *
     * ARCHITECTURE NOTE:
     * In a pure MVC/MVP architecture, the view should communicate with the
     * presenter exclusively. This getter allows external components to interact
     * with the presenter if absolutely necessary, but use sparingly to maintain
     * separation of concerns.
     *
     * @return The GanttPresenter instance managing business logic
     */
    public GanttPresenter getPresenter() {
        return presenter;
    }

    /**
     * Cleanup method to remove listeners and prevent memory leaks.
     *
     * WHEN TO CALL:
     * This should be called by the parent activity when the view is being destroyed,
     * removed from the scene, or no longer needed.
     *
     * WHY NECESSARY?
     * The listeners attached to the data loader's ObservableLists hold strong
     * references to this view instance. If these listeners are not removed,
     * the view cannot be garbage collected even after it's no longer in use,
     * resulting in a memory leak.
     *
     * LIFECYCLE PATTERN:
     * 1. Constructor: Create view and attach listeners
     * 2. startLogic(mixin): Begin data loading
     * 3. [View is active and responding to data changes]
     * 4. cleanup(): Remove listeners when view is destroyed
     *
     * EXAMPLE USAGE:
     * <code>
     * // In parent activity's onDestroy() or similar lifecycle method:
     * if (ganttView != null) {
     *     ganttView.cleanup();
     * }
     * </code>
     */
    public void cleanup() {
        // Remove listeners from data loader's observable lists
        if (resourceConfigListener != null) {
            dataLoader.getResourceConfigurations().removeListener(resourceConfigListener);
        }
        if (attendanceListener != null) {
            dataLoader.getAttendances().removeListener(attendanceListener);
        }
    }
}
